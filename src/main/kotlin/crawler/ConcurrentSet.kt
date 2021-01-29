package crawler

import java.util.concurrent.ConcurrentHashMap

// The built-in concurrency collection `ConcurrencySkipListSet` is ordered, and the URL objects
// in the crawler are not `Comparable`, thus I've created a little wrapper around a `ConcurrentHashMap`
class ConcurrentSet<T> {
    private val delegate = ConcurrentHashMap<T, Boolean>()

    // returns true if value T was already in Set
    fun put(value: T): Boolean {
        return delegate.putIfAbsent(value, true) != null
    }

    fun value(): Set<T> {
        return delegate.keys().toList().toSet()
    }
}
