# Web crawler tech test

## Prerequisites
* JRE 8 +

## Quickstart
To start the crawler, run `./gradlew clean run --args="http://someurlhere.com`, replacing `http://someurlhere.com` with the url you wish to use as a starting point for the crawler.  
It could take a long time!

Alternatively, you can run a full build with `./gradlew clean build` and use the output JAR created in [the build directory](./build).  

To run the tests run `./gradlew clean test`

## Assumptions/tradeoffs
* follow redirect responses on any links under the start point domain
* links returning an error code or not responding within a timeout are marked as visited, but errors are not reported
* TLDs are not checked for validity - if it looks like a link, it will be recorded
* URLS ending with / are different from those without a / ie. bbc.com != bbc.com/ - not strictly true for top level domains, but true of other URLs
* pages and static content don't need to be separated for reporting