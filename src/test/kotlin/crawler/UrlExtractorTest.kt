package crawler

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

class UrlExtractorTest {

    @Test
    fun `multiple URLs are returned correctly`() {
        // given
        val urlsToFind = listOf("http://bbc.com", "https://blah.blah.blah.ru", "ftp://somewhere.local", "http://localhost:8080")

        // when
        val output = UrlExtractor.from("http://bbc.com with some https://blah.blah.blah.ru words &/ other ftp://somewhere.local chars :ftp: http:// http:// between http://localhost:8080 each    ")

        // then
        assertThat(output.toList()).containsAll(urlsToFind)
    }

    @Test
    fun `invalid TLDs are returned if they look like a URL`() {
        // when
        val output = UrlExtractor.from("http://bbc.afaketld")

        // then
        assertThat(output.toList()).contains("http://bbc.afaketld")
    }

    @Test
    fun `html escaped characters are unescaped before finding URLs, so that you weird quot marks dont get parsed as URL`() {
        // when
        val output = UrlExtractor.from("http://bbc.com&quot;")

        // then
        assertThat(output.toList()).isEqualTo(listOf("http://bbc.com"))
    }
}
