package sh
import io.github.oshai.kotlinlogging.KotlinLogging
val log = KotlinLogging.logger("TOP.LEVEL")
class Service { fun who() = log.info { "inside class" } }
fun topLevelFn() = log.info { "top-level fn" }
fun main() { Service().who(); topLevelFn() }
