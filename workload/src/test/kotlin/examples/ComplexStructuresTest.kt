package examples

import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import org.junit.jupiter.api.Test

class ComplexStructuresTest {

    @Test
    fun `SingletonService has log property`() {
        val logger = SingletonService.log
        logger shouldNotBe null
        logger.name shouldBe "examples.SingletonService"
    }

    @Test
    fun `ClassWithCompanion has log property on instance and companion`() {
        val instance = ClassWithCompanion()
        val instanceLogger = instance.log
        instanceLogger shouldNotBe null
        instanceLogger.name shouldBe "examples.ClassWithCompanion"

        val companionLogger = ClassWithCompanion.log
        companionLogger shouldNotBe null
        companionLogger.name shouldBe "examples.ClassWithCompanion.Companion"
    }

    @Test
    fun `InnerClass has log property`() {
        val outer = OuterClass()
        val outerLogger = outer.log
        outerLogger shouldNotBe null
        outerLogger.name shouldBe "examples.OuterClass"

        val inner = outer.InnerClass()
        val innerLogger = inner.log
        innerLogger shouldNotBe null
        innerLogger.name shouldBe "examples.OuterClass.InnerClass"
    }

    @Test
    fun `Sealed hierarchy has log properties`() {
        val base: BaseOperation = AddOperation(1)
        // Accessing log via base reference
        val baseLogger = base.log
        baseLogger shouldNotBe null
        baseLogger.name shouldBe "examples.BaseOperation"

        val addOp = AddOperation(1)
        val addLogger = addOp.log
        addLogger shouldNotBe null
        addLogger.name shouldBe "examples.AddOperation"

        val resetOp = ResetOperation
        val resetLogger = resetOp.log
        resetLogger shouldNotBe null
        resetLogger.name shouldBe "examples.ResetOperation"
    }

    @Test
    fun `Enum class has log property`() {
        // Enum class itself
        val state = ProcessingState.RUNNING
        val logger = state.log
        logger shouldNotBe null
        logger.name shouldBe "examples.ProcessingState"
    }

    @Test
    fun `Abstract class hierarchy has log properties`() {
        val worker = ConcreteWorker()
        
        // Concrete class logger
        val concreteLogger = worker.log
        concreteLogger shouldNotBe null
        concreteLogger.name shouldBe "examples.ConcreteWorker"
        
        // Abstract base class logger (via casting or internal usage)
        // Since extension is on AbstractWorker, we can access it on the instance if typed as AbstractWorker
        val abstractWorker: AbstractWorker = worker
        val abstractLogger = abstractWorker.log
        abstractLogger shouldNotBe null
        abstractLogger.name shouldBe "examples.AbstractWorker"
    }
}
