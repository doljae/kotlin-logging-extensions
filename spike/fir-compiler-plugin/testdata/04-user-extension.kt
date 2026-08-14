package ex
import io.github.oshai.kotlinlogging.KotlinLogging
import io.github.oshai.kotlinlogging.KLogger
class Service { fun who() = log.info { "which one won?" } }
// user-written extension — the exact shape KSP itself generates
val Service.log: KLogger get() = KotlinLogging.logger("USER.WRITTEN.EXTENSION")
fun main() { Service().who() }
