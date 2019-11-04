package io.inbot.eskotlinwrapper

import java.lang.Exception
import kotlin.coroutines.Continuation
import kotlinx.coroutines.suspendCancellableCoroutine
import org.elasticsearch.action.ActionListener

/**
 * Action listener that can be used with to adapt the async methods across the java client to co-routines.
 */
class OldSuspendingActionListener<T>(private val continuation: Continuation<T>) :
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
        suspend fun <T : Any> suspending(block: (OldSuspendingActionListener<T>) -> Unit): T {
            return suspendCancellableCoroutine {
                it.invokeOnCancellation {
                    // TODO blocked on https://github.com/elastic/elasticsearch/issues/44802
                    // given where the ticket is going we probably grab the cancellation token and pass it into suspending so we can call cancel here.
                }
                block.invoke(OldSuspendingActionListener(it))
            }
        }
    }
}
