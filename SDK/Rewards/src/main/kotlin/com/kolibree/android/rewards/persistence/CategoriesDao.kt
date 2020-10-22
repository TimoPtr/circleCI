package com.kolibree.android.rewards.persistence

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Transaction
import com.kolibree.android.rewards.models.CategoryEntity

@Dao
internal abstract class CategoriesDao {
    lateinit var challengesDao: ChallengesDao

    @Transaction
    open fun replace(categoryInternal: List<CategoryEntity>) {
        truncate()

        insertAll(categoryInternal)

        insertChallengesWithCategoryAsForeignKey(categoryInternal)
    }

    private fun insertChallengesWithCategoryAsForeignKey(categories: List<CategoryEntity>) {
        challengesDao.insertAll(categories
            .map { category ->
                category.challenges.map { challenge ->
                    challenge.internalCategory = category.name

                    challenge
                }
            }
            .flatten())
    }

    @Query("DELETE FROM categories")
    abstract fun truncate()

    @Insert
    abstract fun insertAll(categories: List<CategoryEntity>)
}
