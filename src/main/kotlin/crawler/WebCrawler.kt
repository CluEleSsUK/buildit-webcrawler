package crawler

import kotlinx.coroutines.Deferred
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.async
import kotlinx.coroutines.runBlocking
import java.net.URL


data class SiteMapNode(
    val url: URL,
    val children: List<Deferred<SiteMapNode>>,
    val visited: Boolean,
    val seenElsewhere: Boolean = false
) {

    // NOTE: this awaits all the children and is not a fixed-time operation
    fun find(otherUrl: URL): SiteMapNode? {
        if (url == otherUrl) {
            return this
        }

        return runBlocking {
            for (node in children) {
                val child = node.await()
                val found = child.find(otherUrl)
                if (found != null) {
                    return@runBlocking found
                }
            }
            null
        }
    }
}

class WebCrawler(
    private val httpClient: TextHttpClient = TextHttpClient()
) {

    fun createSiteMapTree(url: URL): SiteMapNode {
        return runBlocking {
            crawl(url, url, ConcurrentSet())
        }
    }

    private fun crawl(startingUrl: URL, currentUrl: URL, alreadySeen: ConcurrentSet<URL>): SiteMapNode {
        val urlHasBeenSeenAlready = alreadySeen.put(currentUrl)

        return when {
            !underSameDomain(startingUrl, currentUrl) ->
                SiteMapNode(
                    url = currentUrl,
                    children = emptyList(),
                    visited = false
                )

            urlHasBeenSeenAlready ->
                SiteMapNode(
                    url = currentUrl,
                    children = emptyList(),
                    visited = true,
                    seenElsewhere = true
                )

            else -> {
                try {
                    val urls = allUrlsWithin(currentUrl)
                    val children = urls
                        .map { GlobalScope.async { crawl(startingUrl, it, alreadySeen) } }

                    SiteMapNode(
                        url = currentUrl,
                        children = children.toList(),
                        visited = true,
                        seenElsewhere = false
                    )
                } catch (throwable: Throwable) {
                    SiteMapNode(
                        url = currentUrl,
                        children = emptyList(),
                        visited = true,
                        seenElsewhere = false
                    )
                }
            }
        }
    }

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

fun <T> nullIfException(fn: () -> T): T? =
    try {
        fn()
    } catch (throwable: Throwable) {
        null
    }