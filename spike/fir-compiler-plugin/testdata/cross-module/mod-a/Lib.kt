package liba
open class Base { fun who() = log.info { "from ${this::class.simpleName}" } }
