package com.sren.core

import java.text.Normalizer
import java.util.Locale

object TextNormalizer {
    fun normalize(value: String): String {
        return Normalizer.normalize(value, Normalizer.Form.NFD)
            .replace("\\p{M}+".toRegex(), "")
            .lowercase(Locale.ROOT)
            .trim()
    }
}
