package com.kolibree.android.questionoftheday.data.room.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import com.kolibree.android.questionoftheday.data.room.dao.model.AnswerEntity
import com.kolibree.android.questionoftheday.data.room.dao.model.QuestionEntity
import io.reactivex.Completable
import io.reactivex.Flowable
import io.reactivex.Observable.fromIterable
import io.reactivex.Single

@Suppress("MaxLineLength")
@Dao
internal abstract class QuestionDao {

    //region Internal

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    protected abstract fun insertQuestionWithAnswersInternal(
        question: QuestionEntity,
        answers: List<AnswerEntity>
    )

    @Query("SELECT * FROM question_of_the_day WHERE question_profile_id = :profileId")
    protected abstract fun getFlowableQuestionInternal(profileId: Long): Flowable<QuestionEntity>

    @Query("SELECT * FROM question_of_the_day WHERE question_profile_id = :profileId")
    protected abstract fun getSingleQuestions(profileId: Long): Single<List<QuestionEntity>>

    @Query("SELECT * FROM answer WHERE answer_question_id=:questionId AND answer_profile_id=:profileId")
    protected abstract fun getAnswersInternal(questionId: Long, profileId: Long): Single<List<AnswerEntity>>

    @Query("DELETE FROM question_of_the_day WHERE question_profile_id = :profileId")
    protected abstract fun deleteQuestionsInternal(profileId: Long)

    //endregion Internal

    @Transaction
    open fun insertQuestionWithAnswers(question: QuestionEntity, answers: List<AnswerEntity>) {
        deleteQuestionsInternal(question.questionProfileId)
        insertQuestionWithAnswersInternal(question, answers)
    }

    /**
     * Obtain a well formed [QuestionEntity] [Single] list aggregated with their relationship
     */
    fun getQuestionSingle(profileId: Long): Single<List<Pair<QuestionEntity, List<AnswerEntity>>>> =
        getSingleQuestions(profileId)
            .flatMapObservable { fromIterable(it) }
            .flatMapSingle(::getAnswersSingle)
            .toList()

    /**
     * Obtain a well formed [QuestionEntity] stream aggregated with their relationship
     */
    fun getQuestionFlowable(profileId: Long): Flowable<Pair<QuestionEntity, List<AnswerEntity>>> =
        getFlowableQuestionInternal(profileId).flatMapSingle(::getAnswersSingle)

    @Query("UPDATE question_of_the_day SET question_answered=1 WHERE question_id=:questionId AND question_profile_id=:profileId")
    abstract fun updateAnswered(questionId: Long, profileId: Long): Completable

    private fun getAnswersSingle(questionEntity: QuestionEntity) =
        getAnswersInternal(questionEntity.questionId, questionEntity.questionProfileId)
            .map { answers -> questionEntity to answers }
}
