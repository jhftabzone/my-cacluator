package com.example.calculator.engine

import java.util.Locale
import kotlin.random.Random

enum class WordSize(val bits: Int, val label: String, val mask: ULong) {
    BYTE(8, "8-BIT (BYTE)", 0xFFuL),
    WORD(16, "16-BIT (WORD)", 0xFFFFuL),
    DWORD(32, "32-BIT (DWORD)", 0xFFFFFFFFuL),
    QWORD(64, "64-BIT (QWORD)", 0xFFFFFFFFFFFFFFFFuL)
}

enum class RadixType(val base: Int, val label: String, val prefix: String) {
    HEX(16, "HEX", "0x"),
    DEC(10, "DEC", ""),
    OCT(8, "OCT", "0o"),
    BIN(2, "BIN", "0b")
}

data class ProgrammerState(
    val currentValue: Long = 0L,
    val wordSize: WordSize = WordSize.QWORD,
    val activeRadix: RadixType = RadixType.DEC
) {
    val maskedValue: ULong
        get() = (currentValue.toULong()) and wordSize.mask

    val hexString: String
        get() = maskedValue.toString(16).uppercase(Locale.ROOT).padStart(getHexPad(), '0')

    val decString: String
        get() {
            return when (wordSize) {
                WordSize.BYTE -> currentValue.toByte().toString()
                WordSize.WORD -> currentValue.toShort().toString()
                WordSize.DWORD -> currentValue.toInt().toString()
                WordSize.QWORD -> currentValue.toString()
            }
        }

    val octString: String
        get() = maskedValue.toString(8).padStart(getOctPad(), '0')

    val binString: String
        get() = maskedValue.toString(2).padStart(wordSize.bits, '0')

    val bitCount: Int
        get() = java.lang.Long.bitCount(maskedValue.toLong())

    private fun getHexPad(): Int = when (wordSize) {
        WordSize.BYTE -> 2
        WordSize.WORD -> 4
        WordSize.DWORD -> 8
        WordSize.QWORD -> 16
    }

    private fun getOctPad(): Int = when (wordSize) {
        WordSize.BYTE -> 3
        WordSize.WORD -> 6
        WordSize.DWORD -> 11
        WordSize.QWORD -> 22
    }
}

object ProgrammerEngine {

    fun applyBitwise(a: Long, b: Long, op: String, wordSize: WordSize): Long {
        val mask = wordSize.mask.toLong()
        val res = when (op.uppercase(Locale.ROOT)) {
            "AND" -> (a and b) and mask
            "OR" -> (a or b) and mask
            "XOR" -> (a xor b) and mask
            "NOT" -> (a.inv()) and mask
            "NAND" -> ((a and b).inv()) and mask
            "NOR" -> ((a or b).inv()) and mask
            "SHL", "<<" -> (a shl b.toInt()) and mask
            "SHR", ">>" -> (a ushr b.toInt()) and mask
            "MOD" -> if (b != 0L) (a % b) and mask else 0L
            else -> a
        }
        return signExtend(res, wordSize)
    }

    fun toggleBit(current: Long, bitIndex: Int, wordSize: WordSize): Long {
        if (bitIndex !in 0 until wordSize.bits) return current
        val toggled = current xor (1L shl bitIndex)
        return signExtend(toggled, wordSize)
    }

    fun signExtend(value: Long, wordSize: WordSize): Long {
        return when (wordSize) {
            WordSize.BYTE -> value.toByte().toLong()
            WordSize.WORD -> value.toShort().toLong()
            WordSize.DWORD -> value.toInt().toLong()
            WordSize.QWORD -> value
        }
    }

    fun checkPrime(n: Long): Pair<Boolean, String> {
        if (n <= 1L) return Pair(false, "NOT PRIME (<= 1)")
        if (n <= 3L) return Pair(true, "PRIME NODE")
        if (n % 2L == 0L || n % 3L == 0L) return Pair(false, "DIVISIBLE BY 2 OR 3")

        var i = 5L
        while (i * i <= n) {
            if (n % i == 0L || n % (i + 2L) == 0L) {
                return Pair(false, "COMPOSITE (FACTOR $i)")
            }
            i += 6L
        }
        return Pair(true, "SECURE PRIME")
    }

    fun generateRandom(min: Long = 0L, max: Long = 1000L): Long {
        val safeMin = minOf(min, max)
        val safeMax = maxOf(min, max)
        return Random.nextLong(safeMin, safeMax + 1)
    }
}
