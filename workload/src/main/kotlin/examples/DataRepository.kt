@file:Suppress("UnresolvedReference")

package examples

import kotlin.random.Random

/**
 * Example of a data access layer demonstrating database operation logging
 * and error handling with auto-generated logger.
 */
class DataRepository {

    private val cache = mutableMapOf<String, Any>()

    fun findById(id: String): String? {
        println("LOG [DEBUG] Querying database for record: $id")

        return try {
            simulateQuery(id)
        } catch (e: DatabaseException) {
            println("LOG [ERROR] Database query failed for ID $id: ${e.message}")
            null
        }
    }

    fun save(id: String, data: String): Boolean {
        println("LOG [INFO] Saving data to database: id=$id")

        return try {
            validateData(data)
            performSave(id, data)
            updateCache(id, data)

            println("LOG [INFO] Data saved successfully: $id")
            true
        } catch (e: ValidationException) {
            println("LOG [WARN] Data validation failed for $id: ${e.message}")
            false
        } catch (e: DatabaseException) {
            println("LOG [ERROR] Database save operation failed for $id: ${e.message}")
            false
        }
    }

    fun delete(id: String): Boolean {
        println("LOG [INFO] Deleting record: $id")

        return try {
            if (!exists(id)) {
                println("LOG [WARN] Attempted to delete non-existent record: $id")
                return false
            }

            performDelete(id)
            cache.remove(id)

            println("LOG [INFO] Record deleted successfully: $id")
            true
        } catch (e: DatabaseException) {
            println("LOG [ERROR] Failed to delete record $id: ${e.message}")
            false
        }
    }

    fun getStatistics(): DatabaseStats {
        println("LOG [DEBUG] Generating database statistics")

        val stats = DatabaseStats(
            totalRecords = Random.nextInt(100, 1000),
            cacheSize = cache.size,
            hitRate = Random.nextDouble(0.7, 0.95)
        )

        println("LOG [INFO] Database statistics: totalRecords=${stats.totalRecords}, cacheSize=${stats.cacheSize}, hitRate=${String.format("%.2f", stats.hitRate)}")

        return stats
    }

    private fun simulateQuery(id: String): String? {
        // Simulate database lookup
        Thread.sleep(Random.nextLong(10, 50))

        return when {
            Random.nextDouble() > 0.8 -> {
                // 20% chance of database error
                throw DatabaseException("Connection timeout")
            }
            Random.nextDouble() > 0.3 -> {
                // 50% chance of finding record
                val data = "Sample data for $id"
                println("LOG [TRACE] Query result: found data for $id")
                data
            }
            else -> {
                // 30% chance of no record found
                println("LOG [TRACE] Query result: no data found for $id")
                null
            }
        }
    }

    private fun validateData(data: String) {
        if (data.isBlank()) {
            throw ValidationException("Data cannot be blank")
        }
        if (data.length > 1000) {
            throw ValidationException("Data too large: ${data.length} characters")
        }
        println("LOG [TRACE] Data validation passed")
    }

    private fun performSave(id: String, data: String) {
        println("LOG [TRACE] Executing database save operation")
        Thread.sleep(Random.nextLong(20, 100))

        if (Random.nextDouble() > 0.9) { // 10% failure rate
            throw DatabaseException("Disk full")
        }
    }

    private fun performDelete(id: String) {
        println("LOG [TRACE] Executing database delete operation")
        Thread.sleep(Random.nextLong(10, 30))

        if (Random.nextDouble() > 0.95) { // 5% failure rate
            throw DatabaseException("Lock timeout")
        }
    }

    private fun updateCache(id: String, data: String) {
        cache[id] = data
        println("LOG [TRACE] Cache updated for $id")
    }

    private fun exists(id: String): Boolean {
        val exists = Random.nextBoolean()
        println("LOG [TRACE] Record existence check: $id -> $exists")
        return exists
    }
}

data class DatabaseStats(
    val totalRecords: Int,
    val cacheSize: Int,
    val hitRate: Double
)

class DatabaseException(message: String) : Exception(message)
class ValidationException(message: String) : Exception(message) 