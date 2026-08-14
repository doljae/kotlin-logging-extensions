package ovc
// extending a framework class that already exposes an open `log` of another type
open class Framework { open val log: String = "framework-log" }
class MyService : Framework() { fun who() = println(log) }
fun main() { MyService().who() }
