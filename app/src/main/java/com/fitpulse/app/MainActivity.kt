package com.fitpulse.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge

import androidx.compose.runtime.*
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.fitpulse.app.security.SecurityManager
import java.util.concurrent.TimeUnit

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        val securityManager = SecurityManager(this)

        // Schedule Daily Notifications via WorkManager
        val notifRequest = PeriodicWorkRequestBuilder<com.fitpulse.app.worker.NotificationWorker>(4, TimeUnit.HOURS).build()
        WorkManager.getInstance(this).enqueueUniquePeriodicWork(
            "FitPulseNotifications",
            ExistingPeriodicWorkPolicy.KEEP,
            notifRequest
        )

        setContent {
            var isDark by remember { mutableStateOf(securityManager.isDarkTheme()) }

            CompositionLocalProvider(
                LocalThemeSwitcher provides { 
                    isDark = it
                    securityManager.setDarkTheme(it)
                },
                LocalIsDarkTheme provides isDark
            ) {
                AppTheme(useDarkTheme = isDark) {
                    AppNavigation()
                }
            }
        }
    }
}
