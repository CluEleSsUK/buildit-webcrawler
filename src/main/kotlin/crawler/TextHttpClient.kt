package crawler

import java.net.URL
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse
import java.time.Duration
import java.time.temporal.ChronoUnit

class TextHttpClient(
    private val httpClient: HttpClient = HttpClient.newHttpClient(),
    private val httpRequestTimeout: Duration = Duration.of(2, ChronoUnit.SECONDS)
) {
    fun textFrom(url: URL): HttpResponse<String> {
        return httpClient.send(
            HttpRequest
                .newBuilder()
                .timeout(httpRequestTimeout)
                .header("Accept", "text/html")
                .header("Accept-Charset", "utf-8")
                .GET()
                .uri(url.toURI())
                .build(),
            HttpResponse.BodyHandlers.ofString()
        )
    }
}