# Web crawler tech test

This repo contains code to build and run a web crawler.  
Users must provide a starting domain via command line arguments, which is then crawled in line with the following requirements:

* the crawler should be limited to one domain. Given a starting URL – say
http://wiprodigital.com - it should visit all pages within the domain, but not follow the links
to external sites such as Google or Twitter.
* the output should be a simple structured site map (this does not need to be a traditional XML
sitemap - just some sort of output to reflect what your crawler has discovered), showing links
to other pages under the same domain, links to external URLs and links to static content such
as images for each respective page.
* any URLs found sharing the same domain as the starting domain are followed in the text

## Prerequisites
* JRE 8 +

## Quickstart
To start the crawler, run `./gradlew clean run --args="http://someurlhere.com"`, replacing `http://someurlhere.com` with the url you wish to use as a starting point for the crawler.  
It could take a long time!

Alternatively, you can run a full build with `./gradlew clean build` and use the output JAR created in [the build directory](./build).  

To run the tests run `./gradlew clean test`

## Assumptions/tradeoffs
* follow redirect responses on any links under the start point domain
* links returning an error code or not responding within a timeout are marked as visited, but errors are not reported
* TLDs are not checked for validity - if it looks like a link, it will be recorded
* URLS ending with / are different from those without a / ie. bbc.com != bbc.com/ - not strictly true for top level domains, but true of other URLs
* pages and static content don't need to be separated for reporting
* there's no marking of pages that have been already seen elsewhere in the printed tree

## Extensions
Given more time, I could:
* add integration tests with a mock HTTP server (e.g. Wiremock)
* perform a more robust "same-domain" check
* completely separate URL extraction and the network calls for individual scaling
* validate TLDs
* provide more configurable/tunable threading options.  I used coroutines here, but a `ThreadPoolExecutor` or similar could also work.
* store the output of crawls in a database/cache so they aren't lost between runs

## Concurrency
The `WebCrawler` outputs a tree of `SiteMapNode`s.  Each node's children are of type `Deferred<SiteMapNode>`, which seems like an unusual choice at first glance.  
On the plus side, it allows the printing of the tree while children are still being resolved - otherwise the crawler sits silently crawling until termination and that's no fun!  
On the flip side however, `find` methods of data structures don't tend to be concurrent, so its behaviour could shock an unsuspecting developer.
