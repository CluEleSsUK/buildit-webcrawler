package crawler

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.mockito.Mockito
import org.mockito.Mockito.`when`
import org.mockito.Mockito.doThrow
import org.mockito.Mockito.times
import org.mockito.Mockito.verify
import java.lang.RuntimeException
import java.net.URL
import java.net.http.HttpHeaders
import java.net.http.HttpResponse

class WebCrawlerTest {

    private val httpClientMock = Mockito.mock(TextHttpClient::class.java)
    private val siteMap = WebCrawler(httpClientMock)

    @Test
    internal fun `crawler visits sites under the root domain`() {
        // given
        val baseUrl = URL("https://somewebsite.com")
        val siteUnderBaseUrl = URL("https://somewebsite.com/otherpage")

        val baseUrlResponse = parameterizedMock<HttpResponse<String>>()
        `when`(baseUrlResponse.statusCode()).thenReturn(200)
        `when`(baseUrlResponse.body()).thenReturn("blah $siteUnderBaseUrl www.notsomewebsite.com")
        `when`(httpClientMock.textFrom(baseUrl)).thenReturn(baseUrlResponse)

        val siteUnderBaseUrlResponse = parameterizedMock<HttpResponse<String>>()
        `when`(siteUnderBaseUrlResponse.statusCode()).thenReturn(200)
        `when`(siteUnderBaseUrlResponse.body()).thenReturn("no links in here")
        `when`(httpClientMock.textFrom(siteUnderBaseUrl)).thenReturn(siteUnderBaseUrlResponse)

        // when
        val siteMapTree = siteMap.createSiteMapTree(baseUrl)

        // then
        val baseNode = siteMapTree.find(baseUrl)
        val siteUnderBaseNode = siteMapTree.find(siteUnderBaseUrl)

        assertThat(baseNode).isNotNull()
        assertThat(baseNode?.visited).isEqualTo(true)
        assertThat(siteUnderBaseNode).isNotNull()
        assertThat(siteUnderBaseNode?.visited).isEqualTo(true)

        verify(httpClientMock, times(1)).textFrom(baseUrl)
        verify(httpClientMock, times(1)).textFrom(siteUnderBaseUrl)
    }

    @Test
    internal fun `crawler notes sites not under the root domain, but does not follow them`() {
        // given
        val baseUrl = URL("https://somewebsite.com")
        val differentDomain = URL("http://www.notsomewebsite.com?q=12234")

        val baseUrlResponse = parameterizedMock<HttpResponse<String>>()
        `when`(baseUrlResponse.statusCode()).thenReturn(200)
        `when`(baseUrlResponse.body()).thenReturn("blah $differentDomain and lots of other text")
        `when`(httpClientMock.textFrom(baseUrl)).thenReturn(baseUrlResponse)

        // when
        val siteMapTree = siteMap.createSiteMapTree(baseUrl)

        // then
        val siteNotUnderRootDomain = siteMapTree.find(differentDomain)

        assertThat(siteNotUnderRootDomain).isNotNull()
        assertThat(siteNotUnderRootDomain?.visited).isEqualTo(false)

        verify(httpClientMock, times(0)).textFrom(differentDomain)
    }

    @Test
    internal fun `redirects (http 301-399) from pages under the root domain are followed regardless of their path, but are not added separately to visited`() {
        // given
        val baseUrl = URL("https://somewebsite.com")
        val redirectPath = "https://anotherwebsite.com/otherpage"
        val redirectUrl = URL(redirectPath)

        val baseUrlResponse = parameterizedMock<HttpResponse<String>>()
        `when`(baseUrlResponse.statusCode()).thenReturn(301)
        `when`(baseUrlResponse.headers()).thenReturn(HttpHeaders.of(mapOf("Location" to listOf(redirectPath))) { _, _ -> true })
        `when`(httpClientMock.textFrom(baseUrl)).thenReturn(baseUrlResponse)

        val siteUnderBaseUrlResponse = parameterizedMock<HttpResponse<String>>()
        `when`(siteUnderBaseUrlResponse.statusCode()).thenReturn(200)
        `when`(siteUnderBaseUrlResponse.body()).thenReturn("no links in here")
        `when`(httpClientMock.textFrom(redirectUrl)).thenReturn(siteUnderBaseUrlResponse)

        // when
        val siteMapTree = siteMap.createSiteMapTree(baseUrl)

        // then
        val baseNode = siteMapTree.find(baseUrl)

        assertThat(baseNode).isNotNull()
        assertThat(baseNode?.visited).isEqualTo(true)

        val redirectNode = siteMapTree.find(redirectUrl)
        assertThat(redirectNode).isNull()

        verify(httpClientMock, times(1)).textFrom(baseUrl)
        verify(httpClientMock, times(1)).textFrom(redirectUrl)
    }

    @Test
    internal fun `crawler does not revisit URLs that it has already visited`() {
        // given
        val baseUrl = URL("https://somewebsite.com")

        val baseUrlResponse = parameterizedMock<HttpResponse<String>>()
        `when`(baseUrlResponse.statusCode()).thenReturn(200)
        `when`(baseUrlResponse.body()).thenReturn("$baseUrl $baseUrl $baseUrl $baseUrl")
        `when`(httpClientMock.textFrom(baseUrl)).thenReturn(baseUrlResponse)

        // when
        siteMap.createSiteMapTree(baseUrl)

        // then
        verify(httpClientMock, times(1)).textFrom(baseUrl)
    }

    @Test
    internal fun `http error responses are ignored`() {
        // given
        val baseUrl = URL("https://somewebsite.com")

        val baseUrlResponse = parameterizedMock<HttpResponse<String>>()
        `when`(baseUrlResponse.statusCode()).thenReturn(400)
        `when`(httpClientMock.textFrom(baseUrl)).thenReturn(baseUrlResponse)

        // when
        val siteMapTree = siteMap.createSiteMapTree(baseUrl)

        // then
        val baseUrlNode = siteMapTree.find(baseUrl)
        assertThat(baseUrlNode).isNotNull()
        assertThat(baseUrlNode?.visited).isEqualTo(true)

        verify(httpClientMock, times(1)).textFrom(baseUrl)
    }

    @Test
    internal fun `connect failure is handled gracefully`() {
        // given
        val baseUrl = URL("https://somewebsite.com")

        doThrow(RuntimeException("connection failed")).`when`(httpClientMock).textFrom(baseUrl)

        // when
        val siteMapTree = siteMap.createSiteMapTree(baseUrl)

        // then
        val baseUrlNode = siteMapTree.find(baseUrl)
        assertThat(baseUrlNode).isNotNull()
        assertThat(baseUrlNode?.visited).isEqualTo(true)
        verify(httpClientMock, times(1)).textFrom(baseUrl)
    }
}
