package com.kolibree.sdkws.data.model

import android.os.Parcel
import android.os.Parcelable
import androidx.annotation.Keep
import androidx.annotation.VisibleForTesting
import com.google.gson.Gson
import com.google.gson.JsonObject
import com.google.gson.JsonParser
import com.google.gson.annotations.SerializedName
import com.kolibree.android.commons.DATETIME_FORMATTER
import com.kolibree.android.commons.MIN_BRUSHING_DURATION_SECONDS
import com.kolibree.android.commons.ZONE_FORMATTER
import com.kolibree.sdkws.data.JSONModel
import java.util.UUID
import org.threeten.bp.Duration
import org.threeten.bp.OffsetDateTime
import timber.log.Timber

/** Created by aurelien on 05/08/15.  */
@Keep
class CreateBrushingData : JSONModel, Parcelable {

    @SerializedName("game")
    val game: String

    @SerializedName("datetime")
    val dateTime: String

    @SerializedName("datetime_offset")
    val dateTimeOffset: String

    @Transient
    val date: OffsetDateTime

    @SerializedName("coins")
    val coins: Int

    @SerializedName("goal_duration")
    val goalDuration: Int

    @SerializedName("idempotency_key")
    val idempotencyKey: UUID

    @SerializedName("duration")
    var duration: Long
        private set

    @SerializedName("coverage")
    var coverage: Int?
        set(value) {
            value?.let {
                field = safeCoverage(it)
            }
        }

    @SerializedName("processed_data")
    private var processedData: JsonObject? = null

    // Support data
    @SerializedName("serial")
    var serial: String? = null
        private set

    @SerializedName("mac_address")
    var mac: String? = null
        private set

    @SerializedName("app_version")
    var appVersion: String? = null
        private set

    @SerializedName("build_version")
    var buildVersion: String? = null
        private set

    @SerializedName("fake")
    var isFakeBrushing: Boolean = false

    val durationObject: Duration
        get() = Duration.ofSeconds(duration)

    val isDurationValid: Boolean
        get() = duration >= MIN_BRUSHING_DURATION_SECONDS

    @Transient
    private val jsonParser = JsonParser()

    constructor(
        game: String,
        duration: Long,
        goalDuration: Int,
        date: OffsetDateTime,
        coins: Int,
        idempotencyKey: UUID = UUID.randomUUID(),
        isFakeBrushing: Boolean = false
    ) {
        this.game = game
        this.duration = duration
        this.goalDuration = goalDuration
        this.date = date
        dateTime = DATETIME_FORMATTER.format(date)
        dateTimeOffset = ZONE_FORMATTER.format(date)
        this.coins = coins
        coverage = NO_DATA_COVERAGE
        this.idempotencyKey = idempotencyKey
        this.isFakeBrushing = isFakeBrushing
    }

    constructor(
        game: String,
        duration: Long,
        goalDuration: Int,
        date: OffsetDateTime,
        coins: Int,
        isFakeBrushing: Boolean = false
    ) : this(game, duration, goalDuration, date, coins, UUID.randomUUID(), isFakeBrushing)

    constructor(
        game: String,
        duration: Duration,
        goalDuration: Int,
        date: OffsetDateTime,
        coins: Int,
        isFakeBrushing: Boolean = false
    ) : this(game, duration.seconds, goalDuration, date, coins, isFakeBrushing)

    private constructor(parcel: Parcel) {
        game = parcel.readString()!!
        duration = parcel.readLong()
        date = parcel.readSerializable() as OffsetDateTime
        dateTime = parcel.readString()!!
        dateTimeOffset = ZONE_FORMATTER.format(date)
        coverage = parcel.readInt()
        coins = parcel.readInt()
        goalDuration = parcel.readInt()
        serial = parcel.readString()
        mac = parcel.readString()
        appVersion = parcel.readString()
        buildVersion = parcel.readString()
        val parcelProcessedData = parcel.readString()
        if (parcelProcessedData != null) {
            processedData = jsonParser.parse(parcelProcessedData).asJsonObject
        }
        idempotencyKey = UUID.fromString(parcel.readString())
        isFakeBrushing = parcel.readInt() == 1
    }

    fun addSupportData(
        serial: String?,
        mac: String?,
        appVersion: String?,
        buildVersion: String?
    ) {
        this.serial = serial
        this.mac = mac
        this.appVersion = appVersion
        this.buildVersion = buildVersion
    }

    fun setDuration(duration: Int) {
        this.duration = duration.toLong()
    }

    fun setProcessedData(processedData: String?) {
        this.processedData = processedData?.let {
            jsonParser.parse(processedData).asJsonObject
        }
    }

    fun setProcessedData(brushProcessData: BrushProcessData?) {
        processedData = brushProcessData?.let { buildProcessedData(brushProcessData.zonepasses) }
    }

    fun setProcessedData(processedData: JsonObject?) {
        this.processedData = processedData
    }

    fun getProcessedData(): String? {
        return if (processedData != null) processedData.toString() else null
    }

    override fun toJsonString(): String {
        return Gson().toJson(this)
    }

    private fun buildProcessedData(processedData: List<BrushZonePasses>?): JsonObject? {
        if (processedData != null) {
            val root = JsonObject()
            for (pass in processedData) {
                val zone = JsonObject()
                zone.addProperty(FIELD_EXPECTED_TIME, pass.expectedTime)
                zone.add(FIELD_PASSES, pass.passesAsJsonArray)
                root.add(pass.zoneName, zone)
            }
            return root
        }
        return null
    }

    override fun describeContents(): Int {
        // TODO Auto-generated method stub
        return 0
    }

    override fun writeToParcel(dest: Parcel, flags: Int) {
        dest.writeString(game)
        dest.writeLong(duration)
        dest.writeString(dateTime)
        dest.writeSerializable(date)
        dest.writeInt(coverage!!)
        dest.writeInt(coins)
        dest.writeInt(goalDuration)
        dest.writeString(serial)
        dest.writeString(mac)
        dest.writeString(appVersion)
        dest.writeString(buildVersion)
        dest.writeString(getProcessedData())
        dest.writeString(idempotencyKey.toString())
        dest.writeInt(if (isFakeBrushing) 1 else 0)
    }

    @VisibleForTesting
    fun safeCoverage(coverage: Int): Int? {
        if (coverage < 0) {
            Timber.w("Negative coverage data %d has been provided by game %s", coverage, game)
            return NO_DATA_COVERAGE
        } else if (coverage > 100) {
            Timber.w("Overflowed coverage data %d has been provided by game %s", coverage, game)
            return 100
        }
        return coverage
    }

    companion object {

        @JvmField
        @VisibleForTesting
        val NO_DATA_COVERAGE: Int? = null

        @JvmField
        val CREATOR: Parcelable.Creator<CreateBrushingData> =
            object : Parcelable.Creator<CreateBrushingData> {
                override fun createFromParcel(parcel: Parcel): CreateBrushingData? {
                    return CreateBrushingData(parcel)
                }

                override fun newArray(size: Int): Array<CreateBrushingData?> {
                    return arrayOfNulls(size)
                }
            }

        private const val FIELD_EXPECTED_TIME = "expected_time"
        private const val FIELD_PASSES = "passes"
    }
}
