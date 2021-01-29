package crawler

import java.net.URL
import java.net.http.HttpResponse

sealed class NetworkCall {
    data class Success(val text: String) : NetworkCall()
    data class Redirect(val nextUrl: URL) : NetworkCall()
    object Failure : NetworkCall()

    companion object {
        fun wrap(makeNetworkCall: () -> HttpResponse<String>): NetworkCall {
            return try {
                val response = makeNetworkCall()
                when (response.statusCode()) {
                    in 200..299 -> Success(response.body() ?: "")
                    in 301..399 -> redirect(response)
                    else -> Failure
                }
            } catch (ex: Throwable) {
                Failure
            }
        }

        private fun redirect(response: HttpResponse<String>): NetworkCall {
            val redirectUrl = response.headers()
                .allValues("Location")
                .firstOrNull()

            return if (redirectUrl == null) {
                Failure
            } else {
                Redirect(URL(redirectUrl))
            }
        }
    }
}
