package com.example.calculator.engine

import kotlin.random.Random

data class CyberChallengeQuestion(
    val id: String,
    val title: String,
    val prompt: String,
    val options: List<String>,
    val correctIndex: Int,
    val explanation: String,
    val rewardScore: Int = 100
)

data class CyberChallengeState(
    val activeQuestion: CyberChallengeQuestion? = null,
    val score: Int = 0,
    val streak: Int = 0,
    val bestStreak: Int = 0,
    val level: Int = 1,
    val answeredCount: Int = 0,
    val feedbackMessage: String? = null,
    val isCorrect: Boolean? = null
) {
    val rank: String
        get() = when {
            score >= 2000 -> "ROOT ARCHITECT // OMEGA"
            score >= 1200 -> "NEURAL GHOST // ALPHA"
            score >= 600 -> "CIPHER BREAKER // DELTA"
            score >= 200 -> "GRID INFILTRATOR // BETA"
            else -> "RECON NOVICE // INIT"
        }
}

object CyberChallengeEngine {

    fun generateQuestion(level: Int): CyberChallengeQuestion {
        val qType = Random.nextInt(4)
        return when (qType) {
            0 -> generateArithmetic(level)
            1 -> generateOperatorDeduction(level)
            2 -> generateRadixChallenge(level)
            else -> generateModuloOrPower(level)
        }
    }

    private fun generateArithmetic(level: Int): CyberChallengeQuestion {
        val a = Random.nextInt(12 * level, 35 * level + 10)
        val b = Random.nextInt(5 * level, 20 * level + 5)
        val isMult = (level > 2 && Random.nextBoolean()) || level > 4

        val (ans, expr) = if (isMult) {
            val smA = Random.nextInt(4 * level, 12 * level)
            val smB = Random.nextInt(3, 9)
            Pair(smA * smB, "$smA × $smB")
        } else {
            if (Random.nextBoolean()) {
                Pair(a + b, "$a + $b")
            } else {
                val hi = maxOf(a, b)
                val lo = minOf(a, b)
                Pair(hi - lo, "$hi − $lo")
            }
        }

        val options = mutableSetOf(ans.toString())
        while (options.size < 4) {
            val delta = Random.nextInt(-15, 16)
            if (delta != 0) options.add((ans + delta).toString())
        }
        val shuffled = options.shuffled()

        return CyberChallengeQuestion(
            id = System.nanoTime().toString(),
            title = "DATA PACKET DECRYPTION",
            prompt = "Compute target checksum:\n$expr = ?",
            options = shuffled,
            correctIndex = shuffled.indexOf(ans.toString()),
            explanation = "$expr = $ans"
        )
    }

    private fun generateOperatorDeduction(level: Int): CyberChallengeQuestion {
        val ops = listOf("+", "−", "×", "÷")
        val chosenOp = ops.random()
        val a = when (chosenOp) {
            "×" -> Random.nextInt(3, 12)
            "÷" -> Random.nextInt(2, 9) * Random.nextInt(3, 10)
            else -> Random.nextInt(15, 90)
        }
        val b = when (chosenOp) {
            "×" -> Random.nextInt(3, 9)
            "÷" -> Random.nextInt(2, 6)
            else -> Random.nextInt(5, 50)
        }
        val result = when (chosenOp) {
            "+" -> a + b
            "−" -> a - b
            "×" -> a * b
            "÷" -> a / b
            else -> a + b
        }

        return CyberChallengeQuestion(
            id = System.nanoTime().toString(),
            title = "LOGIC GATE OVERRIDE",
            prompt = "Identify missing operator:\n$a  [ ? ]  $b  =  $result",
            options = ops,
            correctIndex = ops.indexOf(chosenOp),
            explanation = "$a $chosenOp $b = $result"
        )
    }

    private fun generateRadixChallenge(level: Int): CyberChallengeQuestion {
        val num = Random.nextInt(8, if (level > 2) 128 else 32)
        val isHex = Random.nextBoolean()
        val target = if (isHex) "0x" + num.toString(16).uppercase() else "0b" + num.toString(2)
        val prompt = if (isHex) {
            "Decode HEX register $target into decimal:"
        } else {
            "Decode BIN stream $target into decimal:"
        }

        val ans = num.toString()
        val options = mutableSetOf(ans)
        while (options.size < 4) {
            val delta = Random.nextInt(-6, 7) * (if (isHex) 2 else 1)
            if (delta != 0 && (num + delta) >= 0) options.add((num + delta).toString())
        }
        val shuffled = options.shuffled()

        return CyberChallengeQuestion(
            id = System.nanoTime().toString(),
            title = "RADIX CIPHER CRACK",
            prompt = prompt,
            options = shuffled,
            correctIndex = shuffled.indexOf(ans),
            explanation = "$target = $num in base 10"
        )
    }

    private fun generateModuloOrPower(level: Int): CyberChallengeQuestion {
        val isPower = Random.nextBoolean() && level >= 2
        if (isPower) {
            val base = Random.nextInt(2, 5)
            val exp = when (base) {
                2 -> Random.nextInt(4, 9)
                3 -> Random.nextInt(2, 5)
                else -> Random.nextInt(2, 4)
            }
            var ans = 1
            for (i in 0 until exp) ans *= base
            val options = mutableSetOf(ans.toString())
            while (options.size < 4) {
                val delta = Random.nextInt(-10, 11)
                if (delta != 0 && ans + delta > 0) options.add((ans + delta).toString())
            }
            val shuffled = options.shuffled()
            return CyberChallengeQuestion(
                id = System.nanoTime().toString(),
                title = "QUANTUM EXPONENT NODE",
                prompt = "Calculate power surge:\n$base^$exp = ?",
                options = shuffled,
                correctIndex = shuffled.indexOf(ans.toString()),
                explanation = "$base^$exp = $ans"
            )
        } else {
            val div = Random.nextInt(4, 11)
            val num = Random.nextInt(20, 100)
            val rem = num % div
            val options = mutableSetOf(rem.toString())
            for (candidate in 0 until div) {
                options.add(candidate.toString())
                if (options.size == 4) break
            }
            val shuffled = options.shuffled()
            return CyberChallengeQuestion(
                id = System.nanoTime().toString(),
                title = "MODULAR HASH COLLISION",
                prompt = "Calculate remainder:\n$num mod $div = ?",
                options = shuffled,
                correctIndex = shuffled.indexOf(rem.toString()),
                explanation = "$num = ${num / div} × $div + $rem  =>  $rem"
            )
        }
    }
}
