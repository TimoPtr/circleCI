/*
 * Copyright (c) 2018 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 * Proprietary and confidential
 */

package com.kolibree.sdkws.appdata

import androidx.annotation.Keep
import io.reactivex.Completable
import io.reactivex.Maybe

/**
 * A value class representing arbitrary data that is associated with a profile.
 *
 * The Kolibree SDK and platform allow you to associate arbitrary data with a profile. This is
 * intended to facilitate deployment of games and apps using the Kolibree toothbrush. By using this
 * system to save your game progress or app data, you get synchronization to all devices on which
 * the user has installed your app and logged into their account, for free. The only restriction on
 * the data you save is that it must have JSON format.
 *
 * <h3> Saving data </h3>
 * To associate data to a profile, you should create an instance of this class using one of the
 * provided init methods, then use it as argument to the [AppDataManager]'s saveAppData
 * method.
 *
 * <h3> Synchronization </h3>
 * The SDK automatically handles synchronization for you. This means that if you are offline at the
 * time you save associated data, your changes will be synchronized when the link goes back up.
 * Also, if data on the server has been updated from another device, changes will be pulled in.
 *
 * There is one case however where your knowledge of the data's structure is required to perform
 * synchronization :
 * when the server and local versions have diverged. This means that the local database has saved
 * changes that have not yet been pushed to the server, and the server has changes from another
 * device that have not yet been pulled to this device. In this case, the
 * [AppDataConflictSolver] that you provided to the [AppDataManager] will be used.
 * In most cases, it is recommended to have a custom conflict resolution strategy. This means that
 * when a conflict is detected, the onAppDataConflict method is called so that you may resolve the
 * conflict. For instance, if you have a field in your JSON object which represents the last level
 * reached by the player of your game, you probably want to keep the maximum value for this field
 * between the server and local versions when they have diverged. If you have another field which
 * saves a configuration setting like the character to play with, you probably want to keep the
 * version from the most recently saved revision of the two.
 *
 * <h3> Maintaining forwards and backwards compatibility </h3>
 *
 * The model that you use for your JSON objects will likely need to change over time. Newer versions
 * of your app will need to add new features, that will need new data to be stored. New code should
 * be able to work with old data, and old code should be able to work with new data. In particular,
 * old code should be careful not to lose any information that might be present in new data even
 * though it doesn't know the new model, because all updates, even those made by old code, will be
 * pushed to the server and then synchronized to all apps including the ones that are more up to
 * date.
 *
 * As a rule of thumb to make forwards and backwards compatibility easier, we suggest you only ever
 * add fields to your models and never remove them. This is the easiest way to ensure that old code
 * will not be broken when seeing new data, but over a long product life it can lead to big, clunky
 * models where only a few of the properties are actually useful to new code. So even if you intend
 * to follow this rule of thumb, it is better to make sure that old code can cope with fields being
 * absent (by providing sensible defaults, or failing gracefully such as with a user error message),
 * so that you may choose to break backwards compatibility in the future.
 *
 * You should also be wary of changing the range of values that can be encoded in one field.
 * For instance, if you are adding constants to an enum type and have a model field of this enum
 * type, you should refrain from storing the new enum values in the old field, unless you have
 * planned for such a possibility in old code for instance by providing sensible defaults if the
 * field can't be properly decoded into a known enum constant. Depending on the particular data you
 * are storing, it could make more sense in this case to add a new field to encode the full new
 * range of values, while keeping the old field and assigning it either the actual value if it is in
 * the old range, or a default one. New code would then rely on the new field, and old code would
 * keep working reasonably well (again depending on the particular use case).
 *
 * You should remember that due to the synchronization mechanism, the data that is in the database
 * does not necessarily conform to the model version that your app uses. The most recent changes are
 * automatically pulled into the local database, even if they were made by older versions of your
 * app. For this reason, we recommend you use the modelVersion property of [AppData]. You
 * should set this to 1 for all data that is written with the first version of your app, and later
 * versions of your app that change the model should increment it. This way you can know if the data
 * that is present in the database has been written by an older or newer version of the app.
 *
 * To ensure that old code does not lose or corrupt any new data, it should always leave as-is all
 * parts of the data that it doesn't understand, and only change the fields that it knows about.
 *
 * To sum up, here is a simple set of rules you can follow to ensure compatibility :
 *
 *  Never remove fields from models
 *  Never change the range of values that can be encoded in one particular field
 *  If one expected field is not present when reading data, provide a default value or fail
 * gracefully if that is not possible
 *  When writing data, only update the fields you know about and leave other data as is. Don't
 * truncate data just because you don't know all fields.
 *
 *
 */
@Keep
interface AppDataManager {

    /**
     * Get the app data for a user
     *
     * @param profileId profile ID
     * @return null if the user has no data, non null [AppData] [Maybe] otherwise
     */
    fun getAppData(profileId: Long): Maybe<AppData>

    /**
     * Save the app progression for a user
     *
     * @param appData non null [AppData]
     */
    fun saveAppData(appData: AppData): Completable

    /**
     * Synchronize app progression for a user
     *
     * @param profileId profile ID
     */
    fun synchronize(profileId: Long): Completable

    /**
     * This method is called when a data version conflict is experienced by the synchronization
     * mechanism
     *
     * See the class documentation for more information
     * @param conflictSolver non null [AppDataConflictSolver]
     */
    fun setAppDataConflictSolver(conflictSolver: AppDataConflictSolver)
}
