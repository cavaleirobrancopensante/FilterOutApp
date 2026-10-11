package io.github.cavaleirobrancopensante.filterout

import java.text.Normalizer

/**
 * Decides whether a piece of screen text contains a banned word.
 *
 * Rules for entries in the banned list:
 *  - "word"   matches the whole word only        ("sex" does not match "Essex")
 *  - "word*"  matches words starting with it     ("porn*" matches "pornography")
 *  - "*word"  matches words ending with it
 *  - "*word*" matches anywhere inside a word
 *  - Phrases with spaces work too ("only fans").
 *
 * Both the list and the screen text are normalised first: lower case, accents
 * removed ("pornô" -> "porno"), common look-alike characters turned into letters
 * ("p0rn", "s3x", "$ex"), and punctuation turned into spaces ("x.x.x" -> "x x x").
 */
class WordMatcher(entries: Collection<String>) {

    private class Rule(val needle: String)

    private val rules: List<Rule> = entries.mapNotNull { raw ->
        val trimmed = raw.trim()
        val startsWild = trimmed.startsWith("*")
        val endsWild = trimmed.endsWith("*")
        val core = normalize(trimmed.trim('*'))
        if (core.isEmpty()) return@mapNotNull null
        // The normalised screen text is padded with a space on both sides and
        // every word is separated by exactly one space, so " word " means
        // "whole word", " word" means "starts with", and so on.
        val needle = (if (startsWild) "" else " ") + core + (if (endsWild) "" else " ")
        Rule(needle)
    }

    val isEmpty: Boolean get() = rules.isEmpty()

    /** Returns true if [text] contains any banned entry. */
    fun matches(text: CharSequence?): Boolean {
        if (text.isNullOrEmpty() || rules.isEmpty()) return false
        val haystack = " " + normalize(text) + " "
        return rules.any { haystack.contains(it.needle) }
    }

    companion object {
        private val combiningMarks = Regex("\\p{Mn}+")
        private val separators = Regex("[^\\p{L}\\p{N}]+")

        private val lookAlikes = mapOf(
            '0' to 'o', '1' to 'i', '3' to 'e', '4' to 'a', '5' to 's', '7' to 't',
            '@' to 'a', '$' to 's', '!' to 'i', '|' to 'i', '€' to 'e'
        )

        fun normalize(input: CharSequence): String {
            val lower = input.toString().lowercase()
            val mapped = buildString(lower.length) {
                for (c in lower) append(lookAlikes[c] ?: c)
            }
            val noAccents = combiningMarks.replace(Normalizer.normalize(mapped, Normalizer.Form.NFD), "")
            return separators.replace(noAccents, " ").trim()
        }
    }
}
