package examples

import examples.enterprise.service.impl.PaymentServiceImpl

/**
 * Main class demonstrating kotlin-logging-extensions usage with real-world examples.
 * 
 * This file shows how to use the auto-generated `log` property in various scenarios:
 * - User management with validation and error handling
 * - Order processing with performance logging  
 * - Data access with database operations
 * - Deep package structure with fully qualified logger names
 * 
 * All example classes (UserService, OrderProcessor, DataRepository, PaymentServiceImpl) 
 * now use the auto-generated `log` property with calls like:
 * - log.info { "message" }
 * - log.debug { "debug info" }
 * - log.error(exception) { "error message" }
 * 
 * Run this example to see the kotlin-logging in action!
 * 
 * Note: You'll see actual logging output from logback, not println statements.
 * The logger names will be fully qualified class names (e.g., "examples.UserService").
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
    
    demonstrateDeepPackageStructure()
    println()
    
    demonstrateComplexStructures()
    println()
    
    println("=".repeat(60))
    println("✨ All examples completed! Check the logs above.")
    println("✨ Notice how each class automatically has 'log' available!")
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

private fun demonstrateDeepPackageStructure() {
    println("🏢 DEEP PACKAGE STRUCTURE EXAMPLE")
    println("-".repeat(30))
    
    val paymentService = PaymentServiceImpl()
    
    try {
        // Successful payment
        val result1 = paymentService.processPayment(99.99, "CREDIT_CARD")
        println("Payment result: $result1")
        
        // Invalid amount
        val result2 = paymentService.processPayment(-10.0, "DEBIT_CARD")
        println("Payment result: $result2")
        
        // Missing payment method
        val result3 = paymentService.processPayment(50.0, "")
        println("Payment result: $result3")
        
    } catch (e: Exception) {
        println("Payment service error: ${e.message}")
    }
}

private fun demonstrateComplexStructures() {
    println("🏗️ COMPLEX STRUCTURES EXAMPLE")
    println("-".repeat(30))

    // 1. Singleton
    SingletonService.doSomething()

    // 2. Companion Object
    val classWithCompanion = ClassWithCompanion()
    classWithCompanion.instanceMethod()
    ClassWithCompanion.staticMethod()

    // 3. Inner Class
    val outer = OuterClass()
    outer.outerMethod()
    val inner = outer.InnerClass()
    inner.innerMethod()

    // 4. Sealed Class
    val addOp = AddOperation(5)
    addOp.logBase() // Base class method
    addOp.perform()
    
    ResetOperation.logBase()
    ResetOperation.perform()

    // 5. Enum Class
    ProcessingState.RUNNING.logState()

    // 6. Abstract Class
    val worker = ConcreteWorker()
    worker.commonWork()
    worker.specificWork()
}