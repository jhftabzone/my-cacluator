package com.example

import com.example.calculator.engine.*
import org.junit.Assert.*
import org.junit.Test

class MathParserTest {

    @Test
    fun testOperatorPrecedence() {
        val parser = MathParser(AngleMode.DEG)
        val res = parser.evaluate("2 + 3 * 4")
        assertTrue(res is EvalResult.Success)
        assertEquals("14", (res as EvalResult.Success).formatted)
    }

    @Test
    fun testNestedParentheses() {
        val parser = MathParser(AngleMode.DEG)
        val res = parser.evaluate("(2 + 3) * (4 + 1)")
        assertTrue(res is EvalResult.Success)
        assertEquals("25", (res as EvalResult.Success).formatted)
    }

    @Test
    fun testTrigonometryDeg() {
        val parser = MathParser(AngleMode.DEG)
        val resSin = parser.evaluate("sin(90)")
        assertTrue(resSin is EvalResult.Success)
        assertEquals("1", (resSin as EvalResult.Success).formatted)

        val resCos = parser.evaluate("cos(0)")
        assertTrue(resCos is EvalResult.Success)
        assertEquals("1", (resCos as EvalResult.Success).formatted)
    }

    @Test
    fun testFactorialAndPower() {
        val parser = MathParser(AngleMode.DEG)
        val resFact = parser.evaluate("5!")
        assertTrue(resFact is EvalResult.Success)
        assertEquals("120", (resFact as EvalResult.Success).formatted)

        val resPow = parser.evaluate("2^8")
        assertTrue(resPow is EvalResult.Success)
        assertEquals("256", (resPow as EvalResult.Success).formatted)
    }

    @Test
    fun testDivisionByZero() {
        val parser = MathParser(AngleMode.DEG)
        val res = parser.evaluate("42 / 0")
        assertTrue(res is EvalResult.Error)
        assertTrue((res as EvalResult.Error).message.contains("DIV BY ZERO"))
    }

    @Test
    fun testProgrammerBitwise() {
        val andRes = ProgrammerEngine.applyBitwise(0b1100L, 0b1010L, "AND", WordSize.BYTE)
        assertEquals(0b1000L, andRes)

        val orRes = ProgrammerEngine.applyBitwise(0b1100L, 0b1010L, "OR", WordSize.BYTE)
        assertEquals(0b1110L, orRes)

        val xorRes = ProgrammerEngine.applyBitwise(0b1100L, 0b1010L, "XOR", WordSize.BYTE)
        assertEquals(0b0110L, xorRes)
    }

    @Test
    fun testPrimeCheck() {
        val (isPrime17, _) = ProgrammerEngine.checkPrime(17L)
        assertTrue(isPrime17)

        val (isPrime18, _) = ProgrammerEngine.checkPrime(18L)
        assertFalse(isPrime18)
    }
}
