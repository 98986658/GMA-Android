# GMA v0.2 — Android 原始碼（未編譯）

針對 OPPO Find X9 Pro / ColorOS 的測試性 Android 專案，支援 Android 12+，不要求 Root。

## 已實作
- 開啟 Android 無障礙設定頁，使用者手動啟用服務。
- 在橫向螢幕以百分比座標執行 `tap`、`swipe`、`wait` 腳本。
- 腳本解析驗證：最多 40 步，循環 1–5 次，最多運行 60 秒。
- 執行時顯示無障礙覆蓋層的紅色停止按鈕；App 內也可停止。
- 切換到直向、手勢取消或服務中斷會停止後續操作。

## 腳本格式
```
# 座標是橫向螢幕的百分比，非像素
tap 82 75
wait 600
swipe 40 75 65 75 350
```
`tap x y`：0–100；`swipe x1 y1 x2 y2 durationMs`：100–1500ms；`wait ms`：100–5000ms。

## 編譯
用 Android Studio 開啟此資料夾；使用 Android SDK 35、JDK 17，讓 Gradle 同步後 Build > Build APK(s)。本 ZIP 未包含 Gradle wrapper 或已編譯 APK，需本機有 Android Studio / Gradle 工具鏈。

## OPPO / ColorOS 設定
1. 設定 > 關於本機，查看 ColorOS 版本（選單名稱依版本可能不同）。
2. 設定中搜尋「無障礙」或「協助工具」，手動啟用 GMA 服務。若系統限制側載 App 的受限設定，須自行確認信任後依系統提示操作。
3. 橫向開啟遊戲，先在安全的非遊戲畫面測試座標。
4. 若 ColorOS 回收服務，重新開啟 App 並確認服務是否仍啟用；不建議繞過系統安全限制。

## 尚未實作 / 限制
沒有敵人辨識、自動尋路、自動刷素材、自動副本、自動委託、角色技能狀態判斷或智慧戰鬥。固定座標不能保證適配所有遊戲 UI 或視窗。AccessibilityService 的觸控注入可能被遊戲或系統拒絕；Android 版本與 ColorOS 行為需實機測試。第三方遊戲自動化可能違反遊戲服務條款。

## 安全提醒
無障礙服務有較高的操作權限；只安裝可信來源，勿輸入帳號密碼到自動化腳本。本程式不要求網路權限，不上傳畫面或帳號資料。
