package com.kolibree.android.translationssupport

import android.content.Context
import android.content.ContextWrapper
import android.content.res.Resources
import androidx.annotation.Keep
import com.jcminarro.philology.Philology
import com.jcminarro.philology.PhilologyInterceptor
import com.jcminarro.philology.PhilologyRepository
import com.jcminarro.philology.PhilologyRepositoryFactory
import io.github.inflationx.viewpump.ViewPump
import io.github.inflationx.viewpump.ViewPumpContextWrapper
import java.util.Locale

@Keep
object Translations {

    @JvmStatic
    fun init(context: Context, translationsProvider: TranslationsProvider?) {
        if (translationsProvider == null) return

        Philology.init(LocaleRepositoryFactory(context, translationsProvider))
        ViewPump.init(ViewPump.builder().addInterceptor(PhilologyInterceptor).build())
    }

    @JvmStatic
    fun wrapContext(context: Context): ContextWrapper = ViewPumpContextWrapper.wrap(Philology.wrap(context))
}

private class LocaleRepository(private val stringMapping: Map<String, String>) : PhilologyRepository {
    override fun getText(key: String): CharSequence? = stringMapping[key]
}

private class LocaleRepositoryFactory(context: Context, translationsProvider: TranslationsProvider) :
    PhilologyRepositoryFactory {
    private val localeMapping: Map<Locale, LocaleRepository>

    init {
        val resources = context.resources

        localeMapping = translationsProvider.translations().map { (locale, languageTranslations) ->
            locale to LocaleRepository(languageTranslations.map { (key, translation) ->
                try {
                    resources.getResourceEntryName(key) to translation
                } catch (e: Resources.NotFoundException) {
                    throw IllegalArgumentException(
                        "TranslationsProvider contains illegal key `$key`. " +
                            "Every key needs to correspond to valid R.string resource.", e
                    )
                }
            }.toMap())
        }.toMap()
    }

    override fun getPhilologyRepository(locale: Locale): PhilologyRepository? = localeMapping[locale]
}
