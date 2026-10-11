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

    /** Adds the starter list once, the first time the app is opened. */
    fun seedStarterWordsOnce() {
        if (prefs.getBoolean(KEY_SEEDED, false)) return
        words = words + StarterWords.all
        prefs.edit().putBoolean(KEY_SEEDED, true).apply()
    }

    /** Adds back any starter words that were removed. Returns how many were added. */
    fun addStarterWords(): Int {
        val current = words
        val missing = StarterWords.all - current
        if (missing.isNotEmpty()) words = current + missing
        return missing.size
    }

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
        const val KEY_SEEDED = "starter_seeded"
    }
}
