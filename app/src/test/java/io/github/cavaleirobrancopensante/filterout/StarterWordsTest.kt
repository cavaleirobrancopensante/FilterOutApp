package io.github.cavaleirobrancopensante.filterout

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class StarterWordsTest {

    private val matcher = WordMatcher(StarterWords.all)

    @Test fun catchesTypicalPagesInBothLanguages() {
        listOf(
            "https://www.pornhub.com/view_video",
            "Watch free XXX videos",
            "pt.xvideos.com - Vídeos Pornô Grátis",
            "Vídeo de sexo amador",
            "Mulheres peladas",
            "my OnlyFans link in bio",
            "Pornografia",
        ).forEach { assertTrue("should block: $it", matcher.matches(it)) }
    }

    @Test fun leavesEverydayTextAlone() {
        listOf(
            "Sex: Male  Date of birth: 01/02/1990",
            "Sexo: Masculino",
            "Vamos jogar uma pelada no domingo",
            "Que comida gostosa!",
            "Comprei uma cama nova",
            "Living in Essex and Sussex",
            "Stripe payments and stripes",
            "Seen with the naked eye",
            "Dados vazados da empresa",
            "Escort mission in the game",
            "A sexy new phone design",
            "Data analysis report",
        ).forEach { assertFalse("should not block: $it", matcher.matches(it)) }
    }
}
