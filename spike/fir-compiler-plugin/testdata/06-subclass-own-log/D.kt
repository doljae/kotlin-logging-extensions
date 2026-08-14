package ovd
open class Base { fun who() = log.info { "base" } }
class Sub : Base() {
    override val log: String = "not a logger"
    fun mine() = println(log)
}
fun main() { Sub().mine() }
