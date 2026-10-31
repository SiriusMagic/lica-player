package com.zirakusa.stone.data.local.mediastore

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class LibraryStampTest {

    @Test
    fun `same version and newer generation reads only the changes`() {
        assertTrue(LibraryStamp("v1", 12).canDiffFrom(LibraryStamp("v1", 10)))
    }

    @Test
    fun `new MediaStore version needs a full scan`() {
        assertFalse(LibraryStamp("v2", 12).canDiffFrom(LibraryStamp("v1", 10)))
    }

    @Test
    fun `without generations (Android 10 and older) there is no diff`() {
        val none = LibraryStamp.NO_GENERATION
        assertFalse(LibraryStamp("sig", none).canDiffFrom(LibraryStamp("sig", none)))
    }

    @Test
    fun `same or older generation is not a diff`() {
        assertFalse(LibraryStamp("v1", 10).canDiffFrom(LibraryStamp("v1", 10)))
    }
}
