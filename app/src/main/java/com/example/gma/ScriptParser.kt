package com.example.gma

sealed class Step {
    data class Tap(val x: Float, val y: Float): Step()
    data class Swipe(val x1: Float, val y1: Float, val x2: Float, val y2: Float, val duration: Long): Step()
    data class Wait(val duration: Long): Step()
}
data class ParseResult(val steps: List<Step>, val error: String? = null)
object ScriptParser {
    fun parse(source: String): ParseResult {
        val steps = mutableListOf<Step>()
        for ((i, raw) in source.lines().withIndex()) {
            val line = raw.substringBefore('#').trim()
            if (line.isEmpty()) continue
            val t = line.split(Regex("\\s+"))
            fun percent(index: Int): Float? = t.getOrNull(index)?.toFloatOrNull()?.takeIf { it.isFinite() && it in 0f..100f }
            val step: Step? = when(t.first().lowercase()) {
                "tap" -> if (t.size == 3 && percent(1) != null && percent(2) != null) Step.Tap(percent(1)!!, percent(2)!!) else null
                "swipe" -> {
                    val d = t.getOrNull(5)?.toLongOrNull()
                    if (t.size == 6 && (1..4).all { percent(it) != null } && d != null && d in 100..1500)
                        Step.Swipe(percent(1)!!, percent(2)!!, percent(3)!!, percent(4)!!, d) else null
                }
                "wait" -> t.getOrNull(1)?.toLongOrNull()?.takeIf { t.size == 2 && it in 100..5000 }?.let { Step.Wait(it) }
                else -> null
            }
            if (step == null) return ParseResult(emptyList(), "第 ${i + 1} 行格式或範圍錯誤")
            steps.add(step)
            if (steps.size > 40) return ParseResult(emptyList(), "最多允許 40 步")
        }
        if (steps.isEmpty()) return ParseResult(emptyList(), "請輸入腳本")
        return ParseResult(steps)
    }
}
