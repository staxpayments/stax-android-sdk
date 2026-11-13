package com.fattmerchant.omni

open class Environment {
    object LIVE : Environment()
    object DEV : Environment()
    data class QA(val qaBuildHash: String) : Environment() {
        init {
            require(qaBuildHash.matches(BUILD_HASH)) { "qaBuildHash may contain only a-z, 0-9 and -" }
        }

        private companion object {
            val BUILD_HASH = Regex("^[a-z0-9-]+$")
        }
    }
}
