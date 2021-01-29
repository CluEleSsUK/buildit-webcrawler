package crawler

import kotlinx.coroutines.runBlocking
import java.net.URL
import kotlin.system.exitProcess

fun main(args: Array<String>) {
    if (args.size != 1) {
        println("You must enter a starting URL for the crawl")
        exitProcess(1)
    }
    try {
        val url = "http://bbc.com"
        val siteTree = WebCrawler().createSiteMapTree(URL(url))
        print(siteTree)
    } catch (throwable: Throwable) {
        println("The URL entered was not valid - did you remember to add a scheme? (e.g. http://")
        exitProcess(1)
    }
}

fun print(node: SiteMapNode, depth: Int = 0) {
    println(node.url)

    runBlocking {
        node.children.forEach {
            repeat(depth) {
                print("  ")
            }
            print("|__")
            print(it.await(), depth + 1)
        }
    }
}
