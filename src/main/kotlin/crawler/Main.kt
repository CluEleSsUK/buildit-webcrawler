package crawler

import java.net.URL
import kotlin.system.exitProcess

fun main(args: Array<String>) {
    if (args.size != 1) {
        println("You must enter a starting URL for the crawl")
        exitProcess(1)
    }

    try {
        val startPoint = URL(args[0])
        val (seen, visited) = Crawler().crawl(startPoint)
        printResults(startPoint, seen, visited)
    } catch (throwable: Throwable) {
        println("The URL entered was not valid - did you remember to add a scheme? (e.g. http://")
        exitProcess(1)
    }
}

fun printResults(startPoint: URL, found: Set<URL>, visited: Set<URL>) {
    println("Start point:")
    println(startPoint)

    println("Links under the same domain (visited):")
    for (v in visited.minus(startPoint)) {
        println(v)
    }

    println("Links under other domains (not visited):")
    for (f in found.minus(visited)) {
        println(f)
    }
}