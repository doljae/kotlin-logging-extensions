package ove
import org.slf4j.LoggerFactory
open class Base { fun who() = log.info { "base" } }
class Sub : Base() {
    private val log = LoggerFactory.getLogger(Sub::class.java)   // mixing slf4j directly
    fun mine() = log.info("sub")
}
fun main() { Sub().mine() }
