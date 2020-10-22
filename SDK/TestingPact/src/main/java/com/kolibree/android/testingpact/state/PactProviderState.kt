/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.testingpact.state

/**
 * Eligible PACT provider states.
 *
 * Every PACT consumer (client) test can use only one provider (backend) test. Those states are
 * tightly coupled with the state of the backend database.
 *
 * For reference, please check https://kolibree.atlassian.net/wiki/spaces/SOF/pages/8257537/Provider+states
 *
 * In case you need a new provided state for your PACT, it needs to be added on the backend side,
 * along with database initialisation. See:
 * https://github.com/kolibree-git/kolibree-backend/blob/master/services/core/kolibree_core_API/verify_pacts.py
 */
sealed class PactProviderState(val backendName: String) {

    val headerLanguage = HEADER_LANGUAGE

    val creationDateRegexp = CREATION_DATE_REGEXP

    val creationTimeShortRegexp = CREATION_TIME_SHORT_REGEXP

    val creationTimeRegexp = CREATION_TIME_REGEXP

    object FirmwareGruAndHwPresent :
        PactProviderState("fw 1.7.1, Gru 1.0.2 and hw 2.4 for kltb002 are present")

    object NoTestAdultAccount :
        PactProviderState("No account exist for email testadult@email.com")

    object NoTestChildAccount :
        PactProviderState("No account exist for email testchild@email.com")

    abstract class BaseAccountState(backendName: String) : PactProviderState(backendName) {

        val accountId = DEFAULT_ACCOUNT_ID

        val profileId = DEFAULT_PROFILE_ID
    }

    object TestAccountExists : BaseAccountState("Test account exists")

    object TestProfileExists : BaseAccountState("Test profile exists")

    object TestProfileHasRefreshToken :
        BaseAccountState("Test account has refresh token")

    object TestProfileWithSmilesExists : BaseAccountState("Test profile with 1000 smiles exists") {

        const val ownerProfileSmiles = SMILES_FOR_OWNER_PROFILE
    }

    object TestProfileWithPersonalChallenge :
        BaseAccountState("Test profile with active PersonalChallenge(streak, easy, 7days), progress 25%")

    object TwoProfileAccountExists :
        BaseAccountState("Account with 2 profiles, one with 1000 smiles, exists") {

        const val secondProfileId = SECOND_PROFILE_ID

        const val ownerProfileSmiles = SMILES_FOR_OWNER_PROFILE
    }

    object TwoProfileAccountWithSmilesHistoryExists :
        BaseAccountState("Account with 2 profiles, one with 1000 smiles and history, exists") {

        const val secondProfileId = SECOND_PROFILE_ID

        val ownerProfileSmiles = SMILES_FOR_OWNER_PROFILE
    }

    object AnonymousAccountExists :
        PactProviderState("Anonymous account with appid exist")

    object DemoAccountExists :
        PactProviderState("Account kolibreedemo@gmail.com with r@nd0mP@ssw0rd password exist")

    object DemoNonBetaAccountExists :
        PactProviderState("demo account is not beta")

    object SmilesNotificationSent :
        PactProviderState("Smiles notification sent")

    object TestProfileWithQuestionOfTheDay :
        BaseAccountState("Test account with question of the day")

    /**
     * "there’s a pact provider called add_brush_head for your use with tests where the state needs
     * a BrushHead to exist"
     *
     * https://kolibree.slack.com/archives/C01899Q4XRV/p1598845078003300
     */
    object BrushHeadExists : BaseAccountState("Test account with brush head replaced") {
        /**
         * https://kolibree.slack.com/archives/C01899Q4XRV/p1598880157027200?thread_ts=1598845078.003300&cid=C01899Q4XRV
         */
        const val macAddress = "acde48001122"
        const val serialNumber = "123abc"
    }

    private companion object {

        const val DEFAULT_ACCOUNT_ID = 1L

        const val DEFAULT_PROFILE_ID = 1L

        const val SECOND_PROFILE_ID = 2L

        const val SMILES_FOR_OWNER_PROFILE = 1000

        const val HEADER_LANGUAGE = "en-EN"

        const val CREATION_DATE_REGEXP =
            "\\d{4}-(?:0[1-9]|1[012])-(?:0[1-9]|[1-2]\\d|3[01])"

        const val CREATION_TIME_SHORT_REGEXP =
            "${CREATION_DATE_REGEXP}T(?:[01]\\d|2[0-3]):(?:[0-5]\\d):(?:[0-5]\\d)[+-](?:0[0-9]|1[012])00"

        const val CREATION_TIME_REGEXP =
            "${CREATION_DATE_REGEXP}T(?:[01]\\d|2[0-3]):(?:[0-5]\\d):(?:[0-5]\\d)\\.[0-9]{6}[+-](?:0[0-9]|1[012]):00"
    }
}
