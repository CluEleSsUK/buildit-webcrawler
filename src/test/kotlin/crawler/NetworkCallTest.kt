package crawler

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.mockito.Mockito
import java.lang.AssertionError
import java.lang.RuntimeException
import java.net.URL
import java.net.http.HttpHeaders
import java.net.http.HttpResponse

class NetworkCallTest {

    @Test
    fun `OK responses returns success and the text of the web page`() {
        // given
        val expectedBody = "this is the right body"

        val baseUrlResponse = parameterizedMock<HttpResponse<String>>()
        Mockito.`when`(baseUrlResponse.statusCode()).thenReturn(200)
        Mockito.`when`(baseUrlResponse.body()).thenReturn(expectedBody)

        // when
        when (val result = NetworkCall.wrap { baseUrlResponse }) {
            // then
            is NetworkCall.Success -> assertThat(result.text).isEqualTo(expectedBody)
            else -> throw AssertionError("NetworkCall result was not a Success")
        }
    }

    @Test
    fun `all unambiguous redirects ie status code 301 to 399 return redirects`() {
        // given
        val redirectPath = "http://some-other-link.com"
        val baseUrlResponse = parameterizedMock<HttpResponse<String>>()
        Mockito.`when`(baseUrlResponse.statusCode()).thenReturn(301)
        Mockito.`when`(baseUrlResponse.headers()).thenReturn(HttpHeaders.of(mapOf("Location" to listOf(redirectPath))) { _, _ -> true })

        // when
        when (val result = NetworkCall.wrap { baseUrlResponse }) {
            // then
            is NetworkCall.Redirect -> assertThat(result.nextUrl).isEqualTo(URL(redirectPath))
            else -> throw AssertionError("NetworkCall result was not a Redirect")
        }
    }

    @Test
    fun `redirects without a Location header return a Failure`() {
        // given
        val baseUrlResponse = parameterizedMock<HttpResponse<String>>()
        val headersWithNoLocationHeader = HttpHeaders.of(mapOf<String, List<String>>()) { _, _ -> true }

        Mockito.`when`(baseUrlResponse.statusCode()).thenReturn(301)
        Mockito.`when`(baseUrlResponse.headers()).thenReturn(headersWithNoLocationHeader)

        // when
        val result = NetworkCall.wrap { baseUrlResponse }

        // then
        assertThat(result).isEqualTo(NetworkCall.Failure)
    }

    @Test
    fun `any errors making the request return an error response`() {
        // when
        val result = NetworkCall.wrap { throw RuntimeException("Connection failure!")}

        // then
        assertThat(result).isEqualTo(NetworkCall.Failure)
    }
}