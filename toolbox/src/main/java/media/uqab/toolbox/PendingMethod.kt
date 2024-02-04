/**
 * Copyright <2024> <Shahriar Zaman>
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and
 * associated documentation files (the “Software”), to deal in the Software without restriction,
 * including without limitation the rights to use, copy, modify, merge, publish, distribute,
 * sublicense, and/or sell copies of the Software, and to permit persons 
 * to whom the Software is furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included
 * in all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED “AS IS”, WITHOUT WARRANTY OF ANY KIND,
 * EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. 
 * IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM,
 * DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE,
 * ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE 
 * OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */

/**
 * Hold executing until something is ready.
 *
 * - Execute method with [executeOrPending] that needs a prerequisite condition to be satisfied
 * - Start executing pending method with [executePendingMethods] if the required
 * conditions are already satisfied, e.g. activity created, fragment created, data available etc.
 *
 * @author github/fCat97
 */
class PendingMethod {
    // queue of pending methods
    private val pendingFunctions = ArrayDeque<Pending>()


    /**
     * Start executing all the methods from pending queue.
     */
    @Synchronized
    fun executePendingMethods() {
        var p = pendingFunctions.dequeue()

        while (p != null) {
            p = if (p.condition.isSatisfied()) {
                p.method.run()
                pendingFunctions.dequeue() // get the next one
            } else {
                pendingFunctions.unDequeue(p) // put it back
                null
            }
        }
    }

    /**
     * Run an operation if it's ready, otherwise add to queue.
     * These pending methods will be executed once [executePendingMethods] is called.
     *
     * @param waitUntil Condition that needed to be fulfilled before execution.
     * @param thenExecute The actual word that need to be done.
     */
    @Synchronized
    fun executeOrPending(waitUntil: Condition, thenExecute: Runnable) {
        if (waitUntil.isSatisfied()) thenExecute.run()
        else pendingFunctions.queue(Pending(waitUntil, thenExecute))
    }

    private fun <T> ArrayDeque<T>.queue(element: T) = addLast(element)
    private fun <T> ArrayDeque<T>.dequeue() = removeFirstOrNull()
    private fun <T> ArrayDeque<T>.unDequeue(element: T) = addFirst(element)
}

/**
 * Pending method to be executed once required [Condition] is satisfied.
 */
private class Pending(val condition: Condition, val method: Runnable)

/**
 * Condition that must be satisfied before executing the [Pending]
 */
fun interface Condition {
    fun isSatisfied(): Boolean
}


// natural builder ------------------------------------------------------------
data class Partial(val pendingMethod: PendingMethod, val c: Condition)
infix fun PendingMethod.waitFor(c: Condition): Partial {
    return Partial(this, c)
}
infix fun Partial.thenExecute(r: Runnable) {
    return pendingMethod.executeOrPending(c, r)
}
