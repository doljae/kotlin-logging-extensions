@file:Suppress("UnresolvedReference")

package examples

import kotlin.random.Random
import kotlin.time.measureTime

/**
 * Example of an order processing service demonstrating performance logging
 * and business workflow tracking with auto-generated logger.
 */
class OrderProcessor {

    fun processOrder(orderId: String, items: List<OrderItem>): OrderResult {
        log.info { "Starting order processing: orderId=$orderId, items=${items.size}" }

        val processingTime = measureTime {
            validateOrder(orderId, items)
            calculateTotal(items)
            processPayment(orderId)
            updateInventory(items)
        }

        log.info { "Order processing completed: orderId=$orderId, duration=${processingTime.inWholeMilliseconds}ms" }

        return OrderResult(orderId, "COMPLETED", items.sumOf { it.price * it.quantity })
    }

    private fun validateOrder(orderId: String, items: List<OrderItem>) {
        log.debug { "Validating order: $orderId" }

        if (items.isEmpty()) {
            log.warn { "Order validation failed: empty items list for order $orderId" }
            throw OrderValidationException("Order must contain at least one item")
        }

        items.forEach { item ->
            if (item.quantity <= 0) {
                log.error { "Invalid item quantity: ${item.name} quantity=${item.quantity}" }
                throw OrderValidationException("Invalid quantity for item: ${item.name}")
            }
        }

        log.debug { "Order validation passed: $orderId" }
    }

    private fun calculateTotal(items: List<OrderItem>): Double {
        val total = items.sumOf { it.price * it.quantity }
        log.debug { "Order total calculated: ${String.format("%.2f", total)}" }
        return total
    }

    private fun processPayment(orderId: String) {
        log.info { "Processing payment for order: $orderId" }

        try {
            // Simulate payment processing
            Thread.sleep(100) // Simulate network delay
            
            if (Random.nextDouble() > 0.1) { // 90% success rate
                log.info { "Payment processed successfully: $orderId" }
            } else {
                throw PaymentException("Payment gateway timeout")
            }
        } catch (e: PaymentException) {
            log.error(e) { "Payment processing failed: $orderId" }
            throw e
        }
    }

    private fun updateInventory(items: List<OrderItem>) {
        log.debug { "Updating inventory for ${items.size} items" }

        items.forEach { item ->
            try {
                log.trace { "Reducing inventory: ${item.name} by ${item.quantity}" }
                // Simulate inventory update
                if (Random.nextBoolean()) {
                    log.debug { "Inventory updated: ${item.name}" }
                } else {
                    log.warn { "Low stock warning: ${item.name}" }
                }
            } catch (e: Exception) {
                log.error(e) { "Failed to update inventory for ${item.name}" }
            }
        }
    }
}

data class OrderItem(
    val name: String,
    val price: Double,
    val quantity: Int
)

data class OrderResult(
    val orderId: String,
    val status: String,
    val total: Double
)

class OrderValidationException(message: String) : Exception(message)
class PaymentException(message: String) : Exception(message) 