package examples

/**
 * Main class demonstrating kotlin-logging-extensions usage with real-world examples.
 * 
 * This file shows how to use the auto-generated `log` property in various scenarios:
 * - User management with validation and error handling
 * - Order processing with performance logging
 * - Data access with database operations
 * 
 * Run this example to see the logging in action!
 * 
 * Note: In the actual implementation after KSP processing, the println statements
 * will be replaced with actual log.info { }, log.error { }, etc. calls.
 */
fun main() {
    println("=".repeat(60))
    println("🚀 Kotlin Logging Extensions - Usage Examples")
    println("=".repeat(60))
    
    demonstrateUserService()
    println()
    
    demonstrateOrderProcessor()
    println()
    
    demonstrateDataRepository()
    println()
    
    println("=".repeat(60))
    println("✨ All examples completed! Check the logs above.")
    println("=".repeat(60))
}

private fun demonstrateUserService() {
    println("📱 USER SERVICE EXAMPLE")
    println("-".repeat(30))
    
    val userService = UserService()
    
    try {
        // Successful user creation
        val user1 = userService.createUser("John Doe", "john@example.com")
        userService.findUser(user1.id)
        
        // User creation with validation error
        try {
            userService.createUser("", "invalid@example.com")
        } catch (e: IllegalArgumentException) {
            println("Expected validation error caught: ${e.message}")
        }
        
        // User deletion
        userService.deleteUser(user1.id)
        
    } catch (e: Exception) {
        println("Unexpected error in user service: ${e.message}")
    }
}

private fun demonstrateOrderProcessor() {
    println("🛒 ORDER PROCESSOR EXAMPLE")
    println("-".repeat(30))
    
    val orderProcessor = OrderProcessor()
    
    try {
        val items = listOf(
            OrderItem("Laptop", 999.99, 1),
            OrderItem("Mouse", 29.99, 2),
            OrderItem("Keyboard", 79.99, 1)
        )
        
        val result = orderProcessor.processOrder("ORDER-001", items)
        println("Order processing result: $result")
        
        // Try with empty order (validation error)
        try {
            orderProcessor.processOrder("ORDER-002", emptyList())
        } catch (e: OrderValidationException) {
            println("Expected validation error: ${e.message}")
        }
        
    } catch (e: Exception) {
        println("Order processing error: ${e.message}")
    }
}

private fun demonstrateDataRepository() {
    println("💾 DATA REPOSITORY EXAMPLE")
    println("-".repeat(30))
    
    val repository = DataRepository()
    
    try {
        // Database operations
        repository.save("user-123", "User profile data")
        repository.findById("user-123")
        repository.findById("non-existent-id")
        
        val stats = repository.getStatistics()
        println("Database stats: $stats")
        
        repository.delete("user-123")
        
        // Try invalid data
        repository.save("user-456", "")
        
    } catch (e: Exception) {
        println("Database operation error: ${e.message}")
    }
} 