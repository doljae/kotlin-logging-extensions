package examples

import examples.enterprise.service.impl.PaymentServiceImpl
import examples.enterprise.service.impl.log
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import org.junit.jupiter.api.Test

class LogExtensionTest {

    @Test
    fun `UserService has log property`() {
        val service = UserService()
        // Access the extension property
        val logger = service.log
        logger shouldNotBe null
        logger.name shouldBe "examples.UserService"
    }

    @Test
    fun `DataRepository has log property`() {
        val repo = DataRepository()
        val logger = repo.log
        logger shouldNotBe null
        logger.name shouldBe "examples.DataRepository"
    }

    @Test
    fun `OrderProcessor has log property`() {
        val processor = OrderProcessor()
        val logger = processor.log
        logger shouldNotBe null
        logger.name shouldBe "examples.OrderProcessor"
    }

    @Test
    fun `PaymentServiceImpl has log property`() {
        val service = PaymentServiceImpl()
        val logger = service.log
        logger shouldNotBe null
        logger.name shouldBe "examples.enterprise.service.impl.PaymentServiceImpl"
    }
}