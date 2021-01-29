package crawler

import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.async
import java.net.URL
import java.util.concurrent.LinkedBlockingDeque
import java.util.concurrent.atomic.AtomicInteger

data class CrawlStatistics(val seen: Set<URL>, val visited: Set<URL>)

class Crawler(
    private val httpClient: TextHttpClient = TextHttpClient()
) {

    fun crawl(startingUrl: URL): CrawlStatistics {
        val leftToCrawl = LinkedBlockingDeque<URL>()
            .also { it.add(startingUrl) }
        val urlsIdentified = ConcurrentSet<URL>()
        val urlsVisited = ConcurrentSet<URL>()

        val requestsCurrentlyProcessing = AtomicInteger(0)

        do {
            val nextUrl = leftToCrawl.poll() ?: continue
            requestsCurrentlyProcessing.incrementAndGet()

            val hasBeenProcessedBefore = urlsIdentified.put(nextUrl)

            if (hasBeenProcessedBefore || !underSameDomain(startingUrl, nextUrl)) {
                requestsCurrentlyProcessing.decrementAndGet()
            } else {
                GlobalScope.async {
                    try {
                        allUrlsWithin(nextUrl).forEach { leftToCrawl.add(it) }
                        urlsVisited.put(nextUrl)
                    } finally {
                        requestsCurrentlyProcessing.decrementAndGet()
                    }
                }
            }
        } while (requestsCurrentlyProcessing.get() > 0)

        return CrawlStatistics(urlsIdentified.value(), urlsVisited.value())
    }

    // ie. (bbc.com, www.bbc.com) returns true
    // but (bbc.com myfakesitebbc.com) returns false
    private fun underSameDomain(baseDomain: URL, other: URL): Boolean {
        return other.host.startsWith(baseDomain.host) || other.host.endsWith(".${baseDomain.host}")
    }

    private fun allUrlsWithin(url: URL): Sequence<URL> {
        return when (val response = NetworkCall.wrap { httpClient.textFrom(url) }) {
            is NetworkCall.Failure ->
                sequence { }

            is NetworkCall.Redirect ->
                allUrlsWithin(response.nextUrl)

            is NetworkCall.Success ->
                UrlExtractor.from(response.text)
                    .mapNotNull { nullIfException { URL(it) } }
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
