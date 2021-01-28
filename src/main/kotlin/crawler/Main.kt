package crawler

import java.net.URL

fun main() {
    val startPoint = URL("http://bbc.com")
    println("Web crawler started")

    val (seen, visited) = Crawler().crawl(startPoint)
    printResults(seen, visited)
}


fun printResults(found: Set<URL>, visited: Set<URL>) {

    println("visited:")
    for (v in visited) {
        println(v)
    }

    println("seen, but not visited:")
    for (f in found) {
        println(found)
    }
}