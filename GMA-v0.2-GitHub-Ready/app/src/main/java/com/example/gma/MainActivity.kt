package com.example.gma

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.provider.Settings
import android.widget.*

class MainActivity : Activity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val layout = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL; setPadding(30, 30, 30, 30) }
        val scroll = ScrollView(this).apply { addView(layout) }
        fun text(s: String) { layout.addView(TextView(this).apply { this.text = s; textSize = 17f; setPadding(0, 12, 0, 12) }) }
        fun button(s: String, action: () -> Unit) { layout.addView(Button(this).apply { text = s; setOnClickListener { action() } }) }
        text("GMA v0.2｜OPPO / ColorOS 測試版\n僅在明確按下開始後執行。請先在無關緊要的畫面測試。")
        button("1. 開啟無障礙設定") { startActivity(Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS)) }
        text("腳本座標使用橫向畫面的百分比 (0–100)。只有橫向模式才可執行。")
        val editor = EditText(this).apply {
            minLines = 7; setHorizontallyScrolling(false)
            setText("# tap x% y%\ntap 82 75\nwait 600\ntap 91 55\nwait 900\nswipe 40 75 65 75 350")
        }
        layout.addView(editor)
        text("支援：tap x y、swipe x1 y1 x2 y2 durationMs、wait ms。每次最多 40 步，執行 1–5 次；最長 60 秒。")
        val repeats = EditText(this).apply { inputType = 2; setText("1"); hint = "循環次數 1–5" }
        layout.addView(repeats)
        button("2. 驗證並開始腳本") {
            val service = AutomationService.instance
            if (service == null) { Toast.makeText(this, "請先啟用 GMA 無障礙服務", Toast.LENGTH_LONG).show(); return@button }
            val result = ScriptParser.parse(editor.text.toString())
            if (result.error != null) { Toast.makeText(this, result.error, Toast.LENGTH_LONG).show(); return@button }
            val n = repeats.text.toString().toIntOrNull()
            if (n == null || n !in 1..5) { Toast.makeText(this, "循環次數必須是 1–5", Toast.LENGTH_LONG).show(); return@button }
            val message = service.startScript(result.steps, n)
            Toast.makeText(this, message, Toast.LENGTH_LONG).show()
        }
        button("立即停止（App 內）") { AutomationService.instance?.stop(); Toast.makeText(this, "已送出停止指令", Toast.LENGTH_SHORT).show() }
        text("執行時會顯示紅色『停止 GMA』浮動按鈕；切換畫面也能停止。\n未實作：畫面辨識、智慧戰鬥、任務尋路、全自動副本。第三方遊戲自動化可能違反服務條款。")
        setContentView(scroll)
    }
}
