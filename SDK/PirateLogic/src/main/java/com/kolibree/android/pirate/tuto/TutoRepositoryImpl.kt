package com.kolibree.android.pirate.tuto

import com.kolibree.android.pirate.tuto.persistence.dao.TutorialDao
import com.kolibree.android.pirate.tuto.persistence.model.Tutorial
import io.reactivex.Completable
import javax.inject.Inject

internal class TutoRepositoryImpl
@Inject internal constructor(private val tutorialDao: TutorialDao) : TutoRepository {

    override fun hasSeenPirateTuto(profileId: Long): Boolean {
        return hasSeen(profileId, Tutorial.FIELD_HAS_SEEN_PIRATE_TUTO)
    }

    override fun setHasSeenPirateTuto(profileId: Long) {
        setSeen(profileId, Tutorial.FIELD_HAS_SEEN_PIRATE_TUTO)
    }

    override fun hasSeenPirateTrailer(profileId: Long): Boolean {
        return hasSeen(profileId, Tutorial.FIELD_HAS_SEEN_PIRATE_TRAILER)
    }

    override fun setHasSeenPirateTrailer(profileId: Long) {
        setSeen(profileId, Tutorial.FIELD_HAS_SEEN_PIRATE_TRAILER)
    }

    override fun hasSeenPirateCompleteTrailer(profileId: Long): Boolean {
        return hasSeen(profileId, Tutorial.FIELD_HAS_SEEN_PIRATE_COMPLETE_TRAILER)
    }

    override fun setHasSeenPirateCompleteTrailer(profileId: Long) {
        setSeen(profileId, Tutorial.FIELD_HAS_SEEN_PIRATE_COMPLETE_TRAILER)
    }

    override fun hasSeenBreeFirstMessage(profileId: Long): Boolean {
        return hasSeen(profileId, Tutorial.FIELD_HAS_SEEN_BREE_FIRST_MESSAGE)
    }

    override fun setHasSeenBreeFirstMessage(profileId: Long) {
        setSeen(profileId, Tutorial.FIELD_HAS_SEEN_BREE_FIRST_MESSAGE)
    }

    override fun gotABadgeWithLastBrushing(profileId: Long): Boolean {
        return hasSeen(profileId, Tutorial.FIELD_GOT_A_BADGE_WITH_LAST_BRUSHING)
    }

    override fun setGotABadgeWithLastBrushing(profileId: Long, gotABadge: Boolean) {
        tutorialDao.insert(Tutorial(profileId = profileId, gotABadgeWithLastBrushing = gotABadge))
    }

    override fun truncate(): Completable = Completable.fromCallable { tutorialDao.deleteAll() }

    private fun hasSeen(profileId: Long, which: String): Boolean {
        return tutorialDao.hasSeen(profileId)?.getValue(which) ?: false
    }

    private fun setSeen(profileId: Long, which: String) {

        val res = tutorialDao.hasSeen(profileId)

        // check if a tutorial already exist for that user, if not, create a new tutorial, otherwise
        // use the previous one
        tutorialDao.insert(
                res?.let {
                    it.seen(which)
                } ?: Tutorial.create(profileId, which)
        )
    }
}
