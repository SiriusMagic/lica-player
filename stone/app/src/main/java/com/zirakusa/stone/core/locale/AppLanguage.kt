package com.zirakusa.stone.core.locale

import android.content.Context
import android.content.res.Configuration
import java.util.Locale

/**
 * El idioma de Stone, elegido en la bienvenida o en Ajustes (los 6 de Stone 1.3.2).
 *
 * Se guarda en su propio archivo de ajustes y se aplica envolviendo el contexto de la
 * actividad ([wrap]); así no hace falta AppCompat (más peso) y funciona desde Android 7.1.
 */
object AppLanguage {
    /** Código y nombre de cada idioma, en el orden de Stone (`translations.ts`). */
    val all: List<Pair<String, String>> = listOf(
        "es" to "Español",
        "en" to "English",
        "pt" to "Português",
        "fr" to "Français",
        "it" to "Italiano",
        "de" to "Deutsch",
    )

    private const val PREFS = "app_language"
    private const val KEY = "lang"

    /** El guardado; si nunca se eligió, el del teléfono si Stone lo tiene, o español como Stone. */
    fun current(context: Context): String =
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE).getString(KEY, null)
            ?: Locale.getDefault().language.takeIf { code -> all.any { it.first == code } }
            ?: "es"

    fun save(context: Context, code: String) {
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE).edit().putString(KEY, code).apply()
    }

    /** Devuelve [base] con el idioma de Stone aplicado a sus textos. */
    fun wrap(base: Context): Context {
        val locale = Locale(current(base))
        Locale.setDefault(locale)
        val config = Configuration(base.resources.configuration)
        config.setLocale(locale)
        return base.createConfigurationContext(config)
    }
}
