package ova
import io.github.oshai.kotlinlogging.KotlinLogging
open class Base { fun who() = log.info { "base" } }
class Sub : Base() {
    val log = KotlinLogging.logger("USER.SUB")     // user declares their own in a subclass
    fun mine() = log.info { "sub" }
}
fun main() { Base().who(); Sub().mine() }
