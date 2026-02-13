@file:Suppress("UnresolvedReference")

package examples

import io.github.doljae.kotlinlogging.extensions.AutoLog

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
@AutoLog
object SingletonService {
    fun doSomething() {
        log.info { "Logging from SingletonService (object)" }
    }
}

/**
 * 2. Class with Companion Object
 */
@AutoLog
class ClassWithCompanion {
    fun instanceMethod() {
        log.info { "Logging from ClassWithCompanion instance" }
    }

    @AutoLog
    companion object {
        fun staticMethod() {
            log.info { "Logging from ClassWithCompanion companion object" }
        }
    }
}

/**
 * 3. Class with Inner Class
 */
@AutoLog
class OuterClass {
    fun outerMethod() {
        log.info { "Logging from OuterClass" }
    }

    @AutoLog
    class SimpleInnerClass {
        fun innerMethod() {
            log.info { "Logging from InnerClass" }
        }
    }

    @AutoLog
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
@AutoLog
sealed class BaseOperation {
    fun logBase() {
        log.info { "Logging from BaseOperation (sealed class)" }
    }
}

@AutoLog
class AddOperation(val value: Int) : BaseOperation() {
    fun perform() {
        log.info { "Logging from AddOperation: adding $value" }
    }
}

@AutoLog
object ResetOperation : BaseOperation() {
    fun perform() {
        log.info { "Logging from ResetOperation (object extending sealed)" }
    }
}

/**
 * 5. Enum Class
 */
@AutoLog
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
@AutoLog
abstract class AbstractWorker {
    fun commonWork() {
        log.info { "Logging from AbstractWorker common logic" }
    }

    abstract fun specificWork()
}

@AutoLog
class ConcreteWorker : AbstractWorker() {
    override fun specificWork() {
        log.info { "Logging from ConcreteWorker specific logic" }
    }
}
