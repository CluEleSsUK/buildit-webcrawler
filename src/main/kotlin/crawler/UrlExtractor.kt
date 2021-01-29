package crawler

import org.apache.commons.text.StringEscapeUtils

object UrlExtractor {
    // stolen shamelessly from urlregex.com
    private val urlRegex = "(https?|ftp|file)://[-a-zA-Z0-9+&@#/%?=~_|!:,.;]*[-a-zA-Z0-9+&@#/%=~_|]".toRegex()

    fun from(text: String): Sequence<String> {
        val textWithoutEscapes = StringEscapeUtils.unescapeHtml4(text)
        return urlRegex
            .findAll(textWithoutEscapes)
            .map { it.value }
    }
}
