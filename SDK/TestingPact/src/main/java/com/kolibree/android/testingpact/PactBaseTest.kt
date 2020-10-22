package com.kolibree.android.testingpact

import androidx.annotation.CallSuper
import au.com.dius.pact.consumer.ConsumerPactTestMk2
import au.com.dius.pact.consumer.MockServer
import au.com.dius.pact.consumer.PactTestExecutionContext
import au.com.dius.pact.consumer.dsl.PactDslRequestWithPath
import au.com.dius.pact.consumer.dsl.PactDslRequestWithoutPath
import com.google.gson.FieldNamingPolicy
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.gson.TypeAdapter
import com.google.gson.stream.JsonReader
import com.google.gson.stream.JsonToken
import com.google.gson.stream.JsonWriter
import com.kolibree.android.commons.DATETIME_FORMATTER
import com.kolibree.android.commons.DATE_FORMATTER
import com.kolibree.android.commons.models.StrippedMac
import com.kolibree.android.testingpact.HeaderInterceptor.Companion.ACCESS_TOKEN
import com.kolibree.android.testingpact.HeaderInterceptor.Companion.BASE_URL
import com.kolibree.android.testingpact.HeaderInterceptor.Companion.CLIENT_ID
import com.kolibree.android.testingpact.HeaderInterceptor.Companion.CLIENT_SECRET
import com.kolibree.android.testingpact.HeaderInterceptor.Companion.clientSignature
import com.kolibree.android.testingpact.state.PactProviderState
import java.io.IOException
import java.lang.reflect.Type
import java.util.concurrent.TimeUnit
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import org.threeten.bp.LocalDate
import org.threeten.bp.ZonedDateTime
import org.threeten.bp.format.DateTimeParseException
import retrofit2.Converter
import retrofit2.Retrofit
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import retrofit2.converter.gson.GsonConverterFactory

abstract class PactBaseTest<T : PactProviderState>(
    protected val state: T,
    private val ignore: Boolean = false
) : ConsumerPactTestMk2() {

    protected companion object {
        const val PROVIDER_NAME = "Kolibree Backend"
        const val CONSUMER_NAME = "Android Colgate Connect"
        const val PACT_FOLDER = "../build/pacts"

        private fun defaultHeader(url: String): Map<String, String> =
            mapOf(
                "x-access-token" to ACCESS_TOKEN,
                "x-client-id" to CLIENT_ID.toString(),
                "x-client-secret" to CLIENT_SECRET,
                "x-client-sig" to clientSignature(url)
            )

        fun PactDslRequestWithoutPath.withPathAndDefaultHeader(
            path: String
        ): PactDslRequestWithPath =
            path(path).headers(defaultHeader("$BASE_URL$path"))
    }

    protected fun log(message: String) {
        println(message)
    }

    protected fun <T> buildRetrofitClient(
        url: String,
        clazz: Class<T>,
        gson: Gson = defaultGson(),
        okHttpClient: OkHttpClient = defaultOkHttp()
    ): T {
        val retrofit = Retrofit.Builder()
            .baseUrl(url)
            .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
            .addConverterFactory(GsonConverterFactory.create(gson))
            .addConverterFactory(CustomRetrofitConverterFactory.create())
            .client(okHttpClient)
            .build()

        return retrofit.create(clazz)
    }

    protected fun headerWithLanguageInterceptor(language: String): Interceptor {
        return Interceptor { chain ->
            val builder = chain.request().newBuilder()

            builder.addHeader("accept-language", language)

            chain.proceed(builder.build())
        }
    }

    protected fun defaultGson(): Gson = defaultGsonBuilder().create()

    protected fun defaultGsonBuilder(): GsonBuilder {
        return GsonBuilder()
            .setFieldNamingPolicy(FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES)
            .registerTypeAdapter(LocalDate::class.java, LocalDateTypeAdapter())
            .registerTypeAdapter(ZonedDateTime::class.java, ZonedDateTimeTypeAdapter())
    }

    private fun defaultOkHttp() = defaultOkHttpBuilder().build()

    protected fun defaultOkHttpBuilder() = OkHttpClient.Builder()
        .addInterceptor(HeaderInterceptor())
        .readTimeout(30, TimeUnit.SECONDS)
        .writeTimeout(30, TimeUnit.SECONDS)

    override fun providerName(): String = PROVIDER_NAME
    override fun consumerName(): String = CONSUMER_NAME

    @CallSuper
    override fun runTest(mockServer: MockServer, context: PactTestExecutionContext) {
        context.pactFolder = PACT_FOLDER
    }

    override fun testPact() {
        if (ignore) {
            log("PACT test $this is marked as ignored, skipping execution.")
            return
        }
        super.testPact()
    }
}

private class LocalDateTypeAdapter : TypeAdapter<LocalDate>() {

    @Throws(IOException::class)
    override fun write(out: JsonWriter, value: LocalDate?) {
        if (value == null) {
            out.nullValue()
            return
        }
        out.value(value.format(DATE_FORMATTER))
    }

    @Throws(IOException::class)
    override fun read(`in`: JsonReader): LocalDate? {
        if (`in`.peek() == JsonToken.NULL) {
            `in`.nextNull()
            return null
        }

        try {
            return LocalDate.parse(`in`.nextString(), DATE_FORMATTER)
        } catch (e: DateTimeParseException) {
            throw IOException("Unable to convert value into LocalDate.", e)
        }
    }
}

private class ZonedDateTimeTypeAdapter : TypeAdapter<ZonedDateTime>() {
    @Throws(IOException::class)
    override fun write(out: JsonWriter, value: ZonedDateTime?) {
        if (value == null) {
            return
        }

        try {
            out.value(value.format(DATETIME_FORMATTER))
        } catch (e: DateTimeParseException) {
            System.err.println(e)
        }
    }

    @Throws(IOException::class)
    override fun read(json: JsonReader): ZonedDateTime? {
        if (json.peek() == JsonToken.NULL) {
            json.nextNull()
            return null
        }

        val value = json.nextString()

        if (value.isNullOrEmpty()) return null

        return try {
            ZonedDateTime.parse(value, DATETIME_FORMATTER)
        } catch (e: DateTimeParseException) {
            System.err.println(e)

            null
        }
    }
}

private class CustomRetrofitConverterFactory private constructor() : Converter.Factory() {
    override fun stringConverter(
        type: Type,
        annotations: Array<Annotation>,
        retrofit: Retrofit
    ): Converter<*, String>? {
        return when (type) {
            StrippedMac::class.java -> Converter<Any, String> { (it as StrippedMac).value }
            else -> super.stringConverter(type, annotations, retrofit)
        }
    }

    companion object {
        @JvmStatic
        fun create(): CustomRetrofitConverterFactory = CustomRetrofitConverterFactory()
    }
}
