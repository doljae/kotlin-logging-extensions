package examples

import examples.reserved.`is`.ReservedClass
import examples.reserved.`is`.log
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import org.junit.jupiter.api.Test

class ReservedPackageTest {
    @Test
    fun `ReservedClass has log property`() {
        val instance = ReservedClass()
        val logger = instance.log
        logger shouldNotBe null
        logger.name shouldBe "examples.reserved.is.ReservedClass"
        
        // Ensure we can call it (runtime check)
        instance.doSomething()
    }
}
