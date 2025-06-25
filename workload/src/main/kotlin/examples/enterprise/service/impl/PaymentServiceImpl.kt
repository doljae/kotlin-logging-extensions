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
        log.info { "Processing payment: amount=$amount, method=$paymentMethod" }

        return try {
            validatePayment(amount, paymentMethod)
            executePayment(amount, paymentMethod)
            
            log.info { "Payment processed successfully" }
            PaymentResult(success = true, transactionId = "TXN-${System.currentTimeMillis()}")
            
        } catch (e: PaymentValidationException) {
            log.warn(e) { "Payment validation failed" }
            PaymentResult(success = false, error = e.message)
        } catch (e: Exception) {
            log.error(e) { "Unexpected payment error" }
            PaymentResult(success = false, error = "Internal payment processing error")
        }
    }

    private fun validatePayment(amount: Double, paymentMethod: String) {
        log.debug { "Validating payment parameters" }
        
        if (amount <= 0) {
            throw PaymentValidationException("Amount must be positive")
        }
        
        if (paymentMethod.isBlank()) {
            throw PaymentValidationException("Payment method is required")
        }
        
        log.debug { "Payment validation completed" }
    }

    private fun executePayment(amount: Double, paymentMethod: String) {
        log.debug { "Executing payment with $paymentMethod" }
        
        // Simulate payment processing
        Thread.sleep(50)
        
        log.trace { "Payment gateway response received" }
    }
}

data class PaymentResult(
    val success: Boolean,
    val transactionId: String? = null,
    val error: String? = null
)

class PaymentValidationException(message: String) : Exception(message) 