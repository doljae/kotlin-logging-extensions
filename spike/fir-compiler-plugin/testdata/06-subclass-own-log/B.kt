package ovb
import io.github.oshai.kotlinlogging.KotlinLogging
open class Base { fun who() = log.info { "base" } }
class Sub : Base() {
    private val log = KotlinLogging.logger("USER.SUB")   // the most common hand-written form
    fun mine() = log.info { "sub" }
}
fun main() { Sub().mine() }
