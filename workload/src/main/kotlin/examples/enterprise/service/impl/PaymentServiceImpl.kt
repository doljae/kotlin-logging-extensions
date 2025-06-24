@file:Suppress("UnresolvedReference")

package examples.enterprise.service.impl

/**
 * Example demonstrating that kotlin-logging-extensions works seamlessly
 * even with deeply nested package structures.
 * 
 * Package: examples.enterprise.service.impl
 * 
 * The auto-generated logger will use the fully qualified class name:
 * "examples.enterprise.service.impl.PaymentServiceImpl"
 */
class PaymentServiceImpl {

    fun processPayment(amount: Double, paymentMethod: String): PaymentResult {
        println("LOG [INFO] [examples.enterprise.service.impl.PaymentServiceImpl] Processing payment: amount=$amount, method=$paymentMethod")

        return try {
            validatePayment(amount, paymentMethod)
            executePayment(amount, paymentMethod)
            
            println("LOG [INFO] [examples.enterprise.service.impl.PaymentServiceImpl] Payment processed successfully")
            PaymentResult(success = true, transactionId = "TXN-${System.currentTimeMillis()}")
            
        } catch (e: PaymentValidationException) {
            println("LOG [WARN] [examples.enterprise.service.impl.PaymentServiceImpl] Payment validation failed: ${e.message}")
            PaymentResult(success = false, error = e.message)
        } catch (e: Exception) {
            println("LOG [ERROR] [examples.enterprise.service.impl.PaymentServiceImpl] Unexpected payment error: ${e.message}")
            PaymentResult(success = false, error = "Internal payment processing error")
        }
    }

    private fun validatePayment(amount: Double, paymentMethod: String) {
        println("LOG [DEBUG] [examples.enterprise.service.impl.PaymentServiceImpl] Validating payment parameters")
        
        if (amount <= 0) {
            throw PaymentValidationException("Amount must be positive")
        }
        
        if (paymentMethod.isBlank()) {
            throw PaymentValidationException("Payment method is required")
        }
        
        println("LOG [DEBUG] [examples.enterprise.service.impl.PaymentServiceImpl] Payment validation completed")
    }

    private fun executePayment(amount: Double, paymentMethod: String) {
        println("LOG [DEBUG] [examples.enterprise.service.impl.PaymentServiceImpl] Executing payment with $paymentMethod")
        
        // Simulate payment processing
        Thread.sleep(50)
        
        println("LOG [TRACE] [examples.enterprise.service.impl.PaymentServiceImpl] Payment gateway response received")
    }
}

data class PaymentResult(
    val success: Boolean,
    val transactionId: String? = null,
    val error: String? = null
)

class PaymentValidationException(message: String) : Exception(message) 