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
        println("LOG [INFO] Starting order processing: orderId=$orderId, items=${items.size}")

        val processingTime = measureTime {
            validateOrder(orderId, items)
            calculateTotal(items)
            processPayment(orderId)
            updateInventory(items)
        }

        println("LOG [INFO] Order processing completed: orderId=$orderId, duration=${processingTime.inWholeMilliseconds}ms")

        return OrderResult(orderId, "COMPLETED", items.sumOf { it.price * it.quantity })
    }

    private fun validateOrder(orderId: String, items: List<OrderItem>) {
        println("LOG [DEBUG] Validating order: $orderId")

        if (items.isEmpty()) {
            println("LOG [WARN] Order validation failed: empty items list for order $orderId")
            throw OrderValidationException("Order must contain at least one item")
        }

        items.forEach { item ->
            if (item.quantity <= 0) {
                println("LOG [ERROR] Invalid item quantity: ${item.name} quantity=${item.quantity}")
                throw OrderValidationException("Invalid quantity for item: ${item.name}")
            }
        }

        println("LOG [DEBUG] Order validation passed: $orderId")
    }

    private fun calculateTotal(items: List<OrderItem>): Double {
        val total = items.sumOf { it.price * it.quantity }
        println("LOG [DEBUG] Order total calculated: ${String.format("%.2f", total)}")
        return total
    }

    private fun processPayment(orderId: String) {
        println("LOG [INFO] Processing payment for order: $orderId")

        try {
            // Simulate payment processing
            Thread.sleep(100) // Simulate network delay
            
            if (Random.nextDouble() > 0.1) { // 90% success rate
                println("LOG [INFO] Payment processed successfully: $orderId")
            } else {
                throw PaymentException("Payment gateway timeout")
            }
        } catch (e: PaymentException) {
            println("LOG [ERROR] Payment processing failed: $orderId - ${e.message}")
            throw e
        }
    }

    private fun updateInventory(items: List<OrderItem>) {
        println("LOG [DEBUG] Updating inventory for ${items.size} items")

        items.forEach { item ->
            try {
                println("LOG [TRACE] Reducing inventory: ${item.name} by ${item.quantity}")
                // Simulate inventory update
                if (Random.nextBoolean()) {
                    println("LOG [DEBUG] Inventory updated: ${item.name}")
                } else {
                    println("LOG [WARN] Low stock warning: ${item.name}")
                }
            } catch (e: Exception) {
                println("LOG [ERROR] Failed to update inventory for ${item.name}: ${e.message}")
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