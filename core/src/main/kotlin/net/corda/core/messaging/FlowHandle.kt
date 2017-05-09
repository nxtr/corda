package net.corda.core.messaging

import com.google.common.util.concurrent.ListenableFuture
import net.corda.core.flows.StateMachineRunId
import net.corda.core.serialization.CordaSerializable
import rx.Observable

/**
 * [FlowHandle] is a serialisable handle for the started flow, parameterised by the type of the flow's return value.
 *
 * @property id The started state machine's ID.
 * @property returnValue A [ListenableFuture] of the flow's return value.
 */
interface FlowHandle<A> : AutoCloseable {
    val id: StateMachineRunId
    val returnValue: ListenableFuture<A>
}

/**
 * [FlowProgressHandle] is a serialisable handle for the started flow, parameterised by the type of the flow's return value.
 *
 * @property progress The stream of progress tracker events.
 */
interface FlowProgressHandle<A> : FlowHandle<A> {
    val progress: Observable<String>
}


@CordaSerializable
data class FlowHandleImpl<A>(
    override val id: StateMachineRunId,
    override val returnValue: ListenableFuture<A>) : FlowHandle<A> {

     // Remember to add @Throws to FlowHandle.close() if this throws an exception.
    override fun close() {
        returnValue.cancel(false)
    }
}

@CordaSerializable
data class FlowProgressHandleImpl<A>(
    override val id: StateMachineRunId,
    override val returnValue: ListenableFuture<A>,
    override val progress: Observable<String>) : FlowProgressHandle<A> {

    // Remember to add @Throws to FlowProgressHandle.close() if this throws an exception.
    override fun close() {
        progress.notUsed()
        returnValue.cancel(false)
    }
}

// Private copy of the version in client:rpc.
private fun <T> Observable<T>.notUsed() {
    try {
        this.subscribe({}, {}).unsubscribe()
    } catch (e: Exception) {
        // Swallow any other exceptions as well.
    }
}
