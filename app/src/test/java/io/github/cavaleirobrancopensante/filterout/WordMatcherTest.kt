package io.github.cavaleirobrancopensante.filterout

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class WordMatcherTest {

    @Test fun wholeWordOnlyByDefault() {
        val m = WordMatcher(listOf("sex"))
        assertTrue(m.matches("Free SEX videos"))
        assertFalse(m.matches("Visiting Essex and Sussex"))
        assertFalse(m.matches("sexton"))
    }

    @Test fun trailingStarMatchesPrefix() {
        val m = WordMatcher(listOf("porn*"))
        assertTrue(m.matches("pornography"))
        assertTrue(m.matches("porn"))
        assertFalse(m.matches("unporn"))
    }

    @Test fun starsBothSidesMatchAnywhere() {
        val m = WordMatcher(listOf("*porn*"))
        assertTrue(m.matches("xxpornhubxx"))
    }

    @Test fun accentsLookAlikesAndPunctuation() {
        assertTrue(WordMatcher(listOf("porno")).matches("Pornô grátis"))
        assertTrue(WordMatcher(listOf("porn")).matches("best p0rn here"))
        assertTrue(WordMatcher(listOf("sex")).matches("\$EX"))
        assertTrue(WordMatcher(listOf("x x x")).matches("x.x.x"))
    }

    @Test fun phrases() {
        val m = WordMatcher(listOf("only fans"))
        assertTrue(m.matches("my Only-Fans link"))
        assertFalse(m.matches("only the fans of football"))
    }

    @Test fun emptyListMatchesNothing() {
        val m = WordMatcher(listOf("", "*", "  "))
        assertTrue(m.isEmpty)
        assertFalse(m.matches("anything"))
    }
}
