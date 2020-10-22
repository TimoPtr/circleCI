package com.kolibree.android.synchronizator

import androidx.annotation.VisibleForTesting
import com.kolibree.android.accountinternal.persistence.repo.AccountDatastore
import com.kolibree.android.synchronizator.models.SynchronizeAccountKey
import com.kolibree.android.synchronizator.network.SynchronizeAccountApi
import com.kolibree.android.synchronizator.network.SynchronizeAccountRequestBody
import com.kolibree.android.synchronizator.network.SynchronizeAccountResponse
import java.io.IOException
import javax.inject.Inject
import retrofit2.HttpException
import retrofit2.Response
import timber.log.Timber

/**
 * Creates a List of BundleConsumable to be consumed in the scope of a synchronization
 */
internal interface BundleConsumableBuilder {
    fun buildBundleConsumables(): List<BundleConsumable>
}

internal class BundleConsumableBuilderImpl
@Inject constructor(
    private val synchronizeAccountApi: SynchronizeAccountApi,
    private val accountDatastore: AccountDatastore,
    private val bundleConsumableVisitor: BundleConsumableVisitor
) : BundleConsumableBuilder {
    /**
     * Executes a SynchronizeAccountRequest and returns a List of BundleConsumable given the Response and the
     * registered Bundles.
     *
     * The ConsumableBundles only make sense in the context of one SynchronizeAccountResponse, and they should be
     * disposed of as soon as we exit the scope of a synchronization
     *
     * There will be issues if a new Bundle is registered while we perform/process the request, so it might make sense
     * to copy the Bundle Set at the start of the method
     *
     * @throws HttpException if the call to the backend fails
     */
    override fun buildBundleConsumables(): List<BundleConsumable> {
        val syncAccountRequestBody = buildSynchronizeAccountRequestBody()

        try {
            val account = accountDatastore.getAccountMaybe().blockingGet()

            account?.let {
                val syncAccountResponse =
                    synchronizeAccountApi.synchronizationInfo(it.id, syncAccountRequestBody.toMap())

                return processSyncAccountResponse(syncAccountResponse.execute())
            }
        } catch (e: IOException) {
            Timber.e(e)
        }

        return listOf()
    }

    @VisibleForTesting
    fun buildSynchronizeAccountRequestBody(): SynchronizeAccountRequestBody {
        val synchronizeAccountKeys = hashSetOf<SynchronizeAccountKey>()
        SynchronizationBundles.bundles.forEach { bundle ->
            synchronizeAccountKeys.add(bundle.synchronizeAccountKey())
        }

        return SynchronizeAccountRequestBody(synchronizeAccountKeys.toSet())
    }

    @VisibleForTesting
    fun processSyncAccountResponse(response: Response<SynchronizeAccountResponse>): List<BundleConsumable> {
        if (!response.isSuccessful) {
            Timber.e("Request not successful %s (%s)", response.code(), response.message())

            return listOf()
        }

        return response.body()?.let { processResponseBody(it) } ?: listOf()
    }

    /**
     * Processes a response to a SynchronizeAccountRequest. It assumes that the server won't return dictionary keys for
     * those items that have the same versions. e.g. We send "brushings" { "version" : 5}, if backend version is 5, the
     * response won't contain a "brushings" key
     *
     * Implements Visitor pattern to avoid code branching
     *
     * @return List of BundleConsumable. The list will be empty if the response is empty, there aren't Bundles
     * registered or the response doesn't match any of the registered bundles' key. The latter should be considered a
     * IllegalState, tho it's not handled
     */
    @VisibleForTesting
    fun processResponseBody(syncAccountResponse: SynchronizeAccountResponse): List<BundleConsumable> {
        return SynchronizationBundles.bundles.mapNotNull {
            it.accept(
                bundleConsumableVisitor,
                syncAccountResponse
            )
        }
    }
}
