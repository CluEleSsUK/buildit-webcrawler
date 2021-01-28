# Web crawler tech test

## Quickstart
To start the crawler, run `./gradlew clean run`
To run the tests run `./gradlew clean test`

## Assumptions/tradeoffs
* to follow redirect responses on any links relating to the start point
* links returning an error code or not responding within a timeout are marked as visited, but there is no count of errors
* TLDs are not checked for validity - if it looks like a link, it will be recorded
* nothing is stopping a user entering localhost
