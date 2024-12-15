// LoggerIOS.kt
package com.yourapp.myapp.iosLogs

import com.yourapp.myapp.commonLogs.Logger
import platform.Foundation.NSLog


class LoggerIOS : Logger {
    override fun logInfo(message: String) {
        NSLog(message)  // Логируем информацию на iOS
    }

    override fun logError(message: String) {
        NSLog("Error: $message")  // Логируем ошибку на iOS
    }
}

