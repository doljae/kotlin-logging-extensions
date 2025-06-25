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
        log.debug { "Querying database for record: $id" }

        return try {
            simulateQuery(id)
        } catch (e: DatabaseException) {
            log.error(e) { "Database query failed for ID $id" }
            null
        }
    }

    fun save(id: String, data: String): Boolean {
        log.info { "Saving data to database: id=$id" }

        return try {
            validateData(data)
            performSave(id, data)
            updateCache(id, data)

            log.info { "Data saved successfully: $id" }
            true
        } catch (e: ValidationException) {
            log.warn(e) { "Data validation failed for $id" }
            false
        } catch (e: DatabaseException) {
            log.error(e) { "Database save operation failed for $id" }
            false
        }
    }

    fun delete(id: String): Boolean {
        log.info { "Deleting record: $id" }

        return try {
            if (!exists(id)) {
                log.warn { "Attempted to delete non-existent record: $id" }
                return false
            }

            performDelete(id)
            cache.remove(id)

            log.info { "Record deleted successfully: $id" }
            true
        } catch (e: DatabaseException) {
            log.error(e) { "Failed to delete record $id" }
            false
        }
    }

    fun getStatistics(): DatabaseStats {
        log.debug { "Generating database statistics" }

        val stats = DatabaseStats(
            totalRecords = Random.nextInt(100, 1000),
            cacheSize = cache.size,
            hitRate = Random.nextDouble(0.7, 0.95)
        )

        log.info { "Database statistics: totalRecords=${stats.totalRecords}, cacheSize=${stats.cacheSize}, hitRate=${String.format("%.2f", stats.hitRate)}" }

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
                log.trace { "Query result: found data for $id" }
                data
            }
            else -> {
                // 30% chance of no record found
                log.trace { "Query result: no data found for $id" }
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
        log.trace { "Data validation passed" }
    }

    private fun performSave(id: String, data: String) {
        log.trace { "Executing database save operation" }
        Thread.sleep(Random.nextLong(20, 100))

        if (Random.nextDouble() > 0.9) { // 10% failure rate
            throw DatabaseException("Disk full")
        }
    }

    private fun performDelete(id: String) {
        log.trace { "Executing database delete operation" }
        Thread.sleep(Random.nextLong(10, 30))

        if (Random.nextDouble() > 0.95) { // 5% failure rate
            throw DatabaseException("Lock timeout")
        }
    }

    private fun updateCache(id: String, data: String) {
        cache[id] = data
        log.trace { "Cache updated for $id" }
    }

    private fun exists(id: String): Boolean {
        val exists = Random.nextBoolean()
        log.trace { "Record existence check: $id -> $exists" }
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