package hard

import io.github.oshai.kotlinlogging.KotlinLogging

// 1. inheritance — KSP generates a separate extension per class; a member is inherited.
open class Base { fun who() = log.info { "from Base method" } }
class Sub : Base()

// 2. user-declared log — KSP backs off via hasDeclaredLogProperty()
class Manual {
    val log = KotlinLogging.logger("hand.written")
    fun who() = log.info { "manual" }
}

// 3. companion-object log — also covered by hasDeclaredLogProperty()
class ManualCompanion {
    companion object { val log = KotlinLogging.logger("hand.companion") }
    fun who() = log.info { "manual companion" }
}

// 4. interface
interface Contract { fun who() = log.info { "iface" } }

// 5. object / enum / data / value
object Singleton { fun who() = log.info { "object" } }
enum class Color { RED, BLUE; fun who() = log.info { "enum" } }
data class Point(val x: Int) { fun who() = log.info { "data" } }

fun main() {
    Sub().who(); Manual().who(); ManualCompanion().who()
    Singleton.who(); Color.RED.who(); Point(1).who()
    object : Contract {}.who()
}
