package com.fattmerchant.omni.networking

@JvmInline
internal value class UserAgentValue private constructor(val text: String) {
    companion object {
        // A guard, not a format rule: real values are far shorter. It stops an oversized
        // host app value from making a server reject every request that carries the header.
        private const val MAX_LENGTH = 256

        // A backslash escapes the next character in an RFC 9110 comment, so it could hide a ")".
        private const val REMOVED = ";()=\\"

        fun of(raw: String?): UserAgentValue {
            // HTTP headers are ASCII only, and OkHttp throws on any other character.
            val cleaned = buildString {
                raw.orEmpty().codePoints().forEach { codePoint ->
                    val c = if (codePoint in 0x20..0x7E) codePoint.toChar() else '_'
                    if (c !in REMOVED) append(c)
                }
            }.trim().take(MAX_LENGTH).trim()
            return UserAgentValue(cleaned.ifEmpty { "unknown" })
        }
    }
}

internal data class HostApp(
    val id: UserAgentValue,
    val version: UserAgentValue,
    val build: Long,
    val name: UserAgentValue,
    val installer: UserAgentValue?,
    val debuggable: Boolean,
    val targetSdk: Int
)

internal data class Device(
    val osRelease: UserAgentValue,
    val apiLevel: Int,
    val manufacturer: UserAgentValue,
    val model: UserAgentValue
)

internal data class SdkUserAgent(val sdkVersion: UserAgentValue, val app: HostApp?, val device: Device) {
    fun headerValue(): String {
        val appFields = app?.let {
            listOf(
                "app" to it.id.text,
                "version" to it.version.text,
                "build" to "${it.build}",
                "name" to it.name.text,
                "installer" to (it.installer?.text ?: "none"),
                "debug" to if (it.debuggable) "1" else "0",
                "target" to "${it.targetSdk}"
            )
        }.orEmpty()
        val deviceFields = listOf(
            "os" to "Android ${device.osRelease.text}/${device.apiLevel}",
            "device" to "${device.manufacturer.text} ${device.model.text}"
        )
        return (appFields + deviceFields)
            .joinToString("; ", prefix = "stax-sdk-android/${sdkVersion.text} (", postfix = ")") { (key, value) -> "$key=$value" }
    }
}
