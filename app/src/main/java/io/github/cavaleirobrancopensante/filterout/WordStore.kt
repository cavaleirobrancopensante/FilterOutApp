package io.github.cavaleirobrancopensante.filterout

import android.content.Context
import android.content.SharedPreferences

/** The banned words and settings, saved privately on the phone only. */
class WordStore(context: Context) {

    val prefs: SharedPreferences =
        context.applicationContext.getSharedPreferences("filterout", Context.MODE_PRIVATE)

    var words: Set<String>
        get() = prefs.getStringSet(KEY_WORDS, emptySet())!!.toSet()
        set(value) = prefs.edit().putStringSet(KEY_WORDS, value.toSet()).apply()

    /** Read text inside pictures and videos too (needs Android 11 or newer). */
    var ocrEnabled: Boolean
        get() = prefs.getBoolean(KEY_OCR, true)
        set(value) = prefs.edit().putBoolean(KEY_OCR, value).apply()

    fun add(word: String): Boolean {
        val clean = word.trim().lowercase()
        if (clean.trim('*').isBlank()) return false
        val current = words
        if (clean in current) return false
        words = current + clean
        return true
    }

    fun remove(word: String) {
        words = words - word
    }

    companion object {
        const val KEY_WORDS = "words"
        const val KEY_OCR = "ocr_enabled"
    }
}
