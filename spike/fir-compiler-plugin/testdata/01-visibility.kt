package app

class VisibleService           { fun show() = log.info { "hello" } }
private class HiddenService    { fun show() = log.info { "hello" } }
internal class InternalService { fun show() = log.info { "hello" } }

open class Base {
    protected class Config     { fun show() = log.info { "hello" } }
    fun show() = Config().show()
}

fun process() {
    class Temp                 { fun show() = log.info { "hello" } }
    Temp().show()
}

fun main() {
    VisibleService().show(); HiddenService().show(); InternalService().show()
    Base().show(); process()
}
