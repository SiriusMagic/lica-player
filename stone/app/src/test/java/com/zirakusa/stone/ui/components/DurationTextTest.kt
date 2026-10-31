package com.zirakusa.stone.ui.components

import org.junit.Assert.assertEquals
import org.junit.Test

class DurationTextTest {

    @Test
    fun `minutes and seconds`() {
        assertEquals("0:00", 0L.toReadableDuration())
        assertEquals("1:01", 61_000L.toReadableDuration())
    }

    @Test
    fun `hours when needed`() {
        assertEquals("1:00:05", 3_605_000L.toReadableDuration())
    }
}
