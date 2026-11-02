package com.fattmerchant.omni.networking

import org.junit.Assert.assertEquals
import org.junit.Test

class UserAgentValueTest {
    private fun clean(raw: String?) = UserAgentValue.of(raw).text

    @Test fun printableAsciiPassesThrough() = assertEquals("Example Field 4.2.0+x", clean("Example Field 4.2.0+x"))

    @Test fun nonAsciiBecomesUnderscore() = assertEquals("Caf_ POS", clean("Café POS"))

    @Test fun controlCharactersAndDeleteBecomeUnderscores() =
        assertEquals("Line_Break_Del_", clean("Line\nBreak\tDel\u007f"))

    @Test fun separatorsAreRemoved() = assertEquals("a b cd", clean("a; b (c)=d"))

    @Test fun backslashIsRemoved() = assertEquals("a b", clean("a\\ b\\"))

    @Test fun surroundingWhitespaceIsTrimmed() = assertEquals("pos", clean("  pos  "))

    @Test fun valueIsCappedAt256Characters() = assertEquals("x".repeat(256), clean("x".repeat(300)))

    @Test fun whitespaceAtTheCapIsTrimmed() = assertEquals("x".repeat(255), clean("x".repeat(255) + " y"))

    @Test fun eachCodePointBecomesOneUnderscore() = assertEquals("Pixel 9 _", clean("Pixel 9 \uD83D\uDE80"))

    @Test fun nullIsUnknown() = assertEquals("unknown", clean(null))

    @Test fun blankIsUnknown() = assertEquals("unknown", clean(" "))

    @Test fun onlySeparatorsIsUnknown() = assertEquals("unknown", clean("()"))
}
