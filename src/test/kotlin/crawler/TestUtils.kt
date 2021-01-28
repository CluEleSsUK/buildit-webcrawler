package crawler

import org.mockito.Mockito

// a convenient helper for mocking generic classes
inline fun <reified T : Any> parameterizedMock() = Mockito.mock(T::class.java)

