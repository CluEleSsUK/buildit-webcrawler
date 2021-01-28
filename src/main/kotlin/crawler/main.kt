package crawler

import java.net.URL

fun main(args: Array<String>) {
    val startPoint = URL("http://bbc.com")
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