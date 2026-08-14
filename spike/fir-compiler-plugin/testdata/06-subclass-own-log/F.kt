package ovf
// extending a third-party class that was NOT compiled with this plugin
class MyList : ArrayList<String>() { fun who() = log.info { "list" } }
class MyEx(msg: String) : RuntimeException(msg) { fun who() = log.info { "ex" } }
fun main() { MyList().who(); MyEx("x").who() }
