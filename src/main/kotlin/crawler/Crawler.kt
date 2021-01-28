package crawler

import java.net.URL
import java.util.LinkedList

data class CrawlStatistics(val seen: Set<URL>, val visited: Set<URL>)

class Crawler(
    private val httpClient: TextHttpClient = TextHttpClient(),
) {

    fun crawl(startingUrl: URL): CrawlStatistics {
        val leftToCrawl = LinkedList<URL>()
        val urlsSeen = hashSetOf<URL>()
        val visited = hashSetOf<URL>()

        leftToCrawl.add(startingUrl)

        var nextUrlToCrawl = leftToCrawl.poll()
        while (nextUrlToCrawl != null) {
            urlsSeen += nextUrlToCrawl

            if (!visited.contains(nextUrlToCrawl) && underSameDomain(startingUrl, nextUrlToCrawl)) {
                leftToCrawl += allUrlsWithin(nextUrlToCrawl)
                visited += nextUrlToCrawl
            }

            nextUrlToCrawl = leftToCrawl.poll()
        }

        return CrawlStatistics(urlsSeen, visited)
    }

    // ie. (bbc.com, www.bbc.com) returns true
    // but (bbc.com myfakesitebbc.com) returns false
    private fun underSameDomain(baseDomain: URL, other: URL): Boolean {
        return other.host.startsWith(baseDomain.host) || other.host.endsWith(".${baseDomain.host}")
    }

    private fun allUrlsWithin(url: URL): List<URL> {
        return when (val response = NetworkCall.wrap { httpClient.textFrom(url) }) {
            is NetworkCall.Failure ->
                emptyList()

            is NetworkCall.Redirect ->
                allUrlsWithin(response.nextUrl)

            is NetworkCall.Success ->
                UrlExtractor.from(response.text)
                    .mapNotNull { nullIfException { URL(it) } }
                    .toList()
        }
    }
}

fun <T> nullIfException(block: () -> T): T? {
    return try {
        block()
    } catch (ex: Throwable) {
        null
    }
}
