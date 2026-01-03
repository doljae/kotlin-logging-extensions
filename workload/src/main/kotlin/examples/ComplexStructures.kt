@file:Suppress("UnresolvedReference")

package examples

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
object SingletonService {
    fun doSomething() {
        log.info { "Logging from SingletonService (object)" }
    }
}

/**
 * 2. Class with Companion Object
 */
class ClassWithCompanion {
    fun instanceMethod() {
        log.info { "Logging from ClassWithCompanion instance" }
    }

    companion object {
        fun staticMethod() {
            log.info { "Logging from ClassWithCompanion companion object" }
        }
    }
}

/**
 * 3. Class with Inner Class
 */
class OuterClass {
    fun outerMethod() {
        log.info { "Logging from OuterClass" }
    }

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
sealed class BaseOperation {
    fun logBase() {
        log.info { "Logging from BaseOperation (sealed class)" }
    }
}

class AddOperation(val value: Int) : BaseOperation() {
    fun perform() {
        log.info { "Logging from AddOperation: adding $value" }
    }
}

object ResetOperation : BaseOperation() {
    fun perform() {
        log.info { "Logging from ResetOperation (object extending sealed)" }
    }
}

/**
 * 5. Enum Class
 */
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
abstract class AbstractWorker {
    fun commonWork() {
        log.info { "Logging from AbstractWorker common logic" }
    }

    abstract fun specificWork()
}

class ConcreteWorker : AbstractWorker() {
    override fun specificWork() {
        log.info { "Logging from ConcreteWorker specific logic" }
    }
}
