@file:Suppress("UnresolvedReference")

package examples

import io.github.doljae.kotlinlogging.extensions.Log

/**
 * Demonstrates the usage of the auto-generated `log` property in various
 * complex Kotlin class structures:
 * - Objects (Singleton)
 * - Companion Objects
 * - Inner Classes
 * - Sealed Classes
 * - Enum Classes
 * - Abstract Classes
 */

/**
 * 1. Singleton Object
 */
@Log
object SingletonService {
    fun doSomething() {
        log.info { "Logging from SingletonService (object)" }
    }
}

/**
 * 2. Class with Companion Object
 */
@Log
class ClassWithCompanion {
    fun instanceMethod() {
        log.info { "Logging from ClassWithCompanion instance" }
    }

    @Log
    companion object {
        fun staticMethod() {
            log.info { "Logging from ClassWithCompanion companion object" }
        }
    }
}

/**
 * 3. Class with Inner Class
 */
@Log
class OuterClass {
    fun outerMethod() {
        log.info { "Logging from OuterClass" }
    }

    @Log
    class SimpleInnerClass {
        fun innerMethod() {
            log.info { "Logging from InnerClass" }
        }
    }

    @Log
    inner class InnerClass {
        fun innerMethod() {
            log.info { "Logging from InnerClass" }
            // Verify we can access outer context if needed, though logger is specific to inner
            log.debug { "Inner accessing outer: ${this@OuterClass}" }
        }
    }
}

/**
 * 4. Sealed Class Hierarchy
 */
@Log
sealed class BaseOperation {
    fun logBase() {
        log.info { "Logging from BaseOperation (sealed class)" }
    }
}

@Log
class AddOperation(val value: Int) : BaseOperation() {
    fun perform() {
        log.info { "Logging from AddOperation: adding $value" }
    }
}

@Log
object ResetOperation : BaseOperation() {
    fun perform() {
        log.info { "Logging from ResetOperation (object extending sealed)" }
    }
}

/**
 * 5. Enum Class
 */
@Log
enum class ProcessingState {
    IDLE,
    RUNNING,
    FINISHED;

    fun logState() {
        log.info { "Current state is: $name" }
    }
}

/**
 * 6. Abstract Class
 */
@Log
abstract class AbstractWorker {
    fun commonWork() {
        log.info { "Logging from AbstractWorker common logic" }
    }

    abstract fun specificWork()
}

@Log
class ConcreteWorker : AbstractWorker() {
    override fun specificWork() {
        log.info { "Logging from ConcreteWorker specific logic" }
    }
}

/**
 * 7. Bounded Generic Class
 *
 * The generated receiver is star-projected, so the bound below never has to be restated.
 */
@Log
class BoundedCache<K : Any, V : Comparable<V>> {
    private val entries = mutableMapOf<K, V>()

    fun put(key: K, value: V) {
        entries[key] = value
        log.info { "Cached $key" }
    }
}

/**
 * 8. Generic Outer With Nested And Inner Classes
 */
@Log
class GenericHolder<T : Any> {
    @Log
    class Nested<U : Any> {
        fun work() {
            log.info { "Logging from GenericHolder.Nested" }
        }
    }

    @Log
    inner class Inner {
        fun work() {
            log.info { "Logging from GenericHolder.Inner" }
        }
    }
}

/**
 * 9. Private Class
 *
 * Nothing is generated: the extension would live in another file, which cannot name a private class.
 */
@Log
private class PrivateWorker {
    fun work(): String = "no generated log here"
}

internal fun usePrivateWorker(): String = PrivateWorker().work()
