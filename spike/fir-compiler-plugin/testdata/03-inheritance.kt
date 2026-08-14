package inh
open class Base { fun who() = log.info { "called on ${this::class.simpleName}" } }
class Sub : Base()
fun main() { Base().who(); Sub().who() }
