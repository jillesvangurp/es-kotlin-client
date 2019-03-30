package io.inbot.eskotlinwrapper

import org.elasticsearch.action.ActionListener
import java.lang.Exception
import kotlin.coroutines.Continuation
import kotlin.coroutines.suspendCoroutine

/**
 * Action listener that can be used with to adapt the async methods across the java client to co-routines.
 */
class SuspendingActionListener<T>(private val continuation: Continuation<T>) :
    ActionListener<T> {
    override fun onFailure(e: Exception) {
        continuation.resumeWith(Result.failure(e))
    }

    override fun onResponse(response: T) {
        continuation.resumeWith(Result.success(response))
    }

    companion object {
        /**
         * Use this to call java async methods in a co-routine.
         *
         * ```kotlin
         * suspending {
         *   client.searchAsync(searchRequest, requestOptions, it)
         * }
         * ```
         */
        suspend fun <T : Any> suspending(block: (SuspendingActionListener<T>) -> Unit): T {
            return suspendCoroutine {
                block.invoke(SuspendingActionListener(it))
            }
        }
    }
}