package io.github.cavaleirobrancopensante.filterout

/**
 * A starter list added on first launch (and re-addable from the main screen).
 *
 * Chosen to be specific: ordinary words that are often innocent are left out
 * on purpose, because they would block normal pages. For example "sex"/"sexo"
 * (gender fields on forms), "pelada" (a football match in Brazil), "gostosa"
 * (tasty food), "cama"/"cam4" (Portuguese for bed), "escort", "sexy".
 * Add any of them yourself if you want them.
 *
 * Sites are listed by name, so "pornhub" blocks pornhub.com, pt.pornhub.com, etc.
 */
object StarterWords {

    val english = listOf(
        "porn*", "xxx", "nsfw", "hentai", "nude*", "erotic*",
        "onlyfans", "only fans", "fansly", "camgirl*", "cam girl*", "webcam sex", "sexcam*",
        "sex video*", "sex tape*", "free sex", "live sex",
        "milf*", "blowjob*", "handjob*", "cumshot*", "creampie*", "gangbang*",
        "deepthroat*", "threesome*", "bdsm", "stripper*", "striptease",
        "boobs", "pussy", "dick pic*", "horny", "playboy", "rule34", "nudity"
    )

    val portuguese = listOf(
        "putaria", "putinha*", "safadas", "safadinha*", "novinhas",
        "sexo explicito", "video de sexo", "videos de sexo", "sexo ao vivo", "sexo gratis",
        "mulher pelada", "mulheres peladas", "garota de programa", "garotas de programa",
        "boquete*", "punheta*", "siririca*", "buceta*", "xoxota*", "fodendo", "transando",
        "brasileirinhas", "cameraprive", "sexlog"
    )

    val sites = listOf(
        "pornhub", "xvideos", "xnxx", "xhamster", "redtube", "youporn", "spankbang",
        "eporner", "tnaflix", "beeg", "brazzers", "hqporner", "txxx",
        "hclips", "thumbzilla", "tube8", "chaturbate", "stripchat", "bongacams",
        "livejasmin", "myfreecams", "nhentai", "redgifs", "manyvids", "erome",
        "fapello", "thothub"
    )

    val all: Set<String> get() = (english + portuguese + sites).toSet()
}
