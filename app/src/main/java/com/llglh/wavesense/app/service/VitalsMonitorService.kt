package com.llglh.wavesense.app.service

import android.app.*
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.Build
import android.os.IBinder
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import android.util.Log
import androidx.core.app.NotificationCompat
import com.llglh.wavesense.R
import com.llglh.wavesense.app.ui.activity.MainActivity
import okhttp3.*
import org.json.JSONObject
import java.util.concurrent.TimeUnit

class VitalsMonitorService : Service() {

    private var webSocket: WebSocket? = null
    private val client = OkHttpClient.Builder()
        .pingInterval(10, TimeUnit.SECONDS)
        .build()

    private val CHANNEL_ID_SILENT = "silent_monitor_channel"
    private val CHANNEL_ID_ALERT = "high_alert_channel"

    private val FOREGROUND_NOTIFICATION_ID = 1001
    private val ALERT_NOTIFICATION_ID = 1002

    override fun onCreate() {
        super.onCreate()
        createNotificationChannels()
        startForeground(FOREGROUND_NOTIFICATION_ID, createSilentNotification("正在连接监控设备..."))
        startWebSocket()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        return START_STICKY
    }

    private fun startWebSocket() {
        // ⚠️ 请务必修改为你真实的服务器局域网 IP
        val wsUrl = "ws://124.222.238.77:5050/ws/vitals"
        val request = Request.Builder().url(wsUrl).build()

        webSocket = client.newWebSocket(request, object : WebSocketListener() {
            override fun onOpen(webSocket: WebSocket, response: Response) {
                updateSilentNotification("✅ 设备已连接，后台实时监护中")
            }

            override fun onMessage(webSocket: WebSocket, text: String) {
                try {
                    val json = JSONObject(text)
                    val bpmStr = json.optString("bpm", "0")
                    val bpmVal = bpmStr.toFloatOrNull() ?: 0f

                    // 1. 无脑转发数据给 UI 界面画图
                    val intent = Intent("VITALS_UPDATE_ACTION")
                    intent.setPackage(packageName) // 确保穿透 Android 14 广播限制
                    intent.putExtra("bpm", bpmStr)
                    intent.putExtra("bpmVal", bpmVal)
                    sendBroadcast(intent)

                    // 2. 🚨 听后端的命令：如果有 alert_msg 字段，说明后端判定异常了，直接弹窗！
                    if (json.has("alert_msg")) {
                        val alertMessage = json.getString("alert_msg")
                        triggerHighAlert(alertMessage)
                    }

                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }

            override fun onClosed(webSocket: WebSocket, code: Int, reason: String) {
                updateSilentNotification("❌ 监护连接已断开")
            }

            override fun onFailure(webSocket: WebSocket, t: Throwable, response: Response?) {
                updateSilentNotification("⚠️ 网络异常，正在尝试重连...")
                Log.e("MonitorService", "WebSocket Error: ", t)
                Thread.sleep(3000)
                startWebSocket()
            }
        })
    }

    private fun createNotificationChannels() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

            val silentChannel = NotificationChannel(CHANNEL_ID_SILENT, "后台运行状态", NotificationManager.IMPORTANCE_LOW)
            silentChannel.description = "维持 App 在后台运行"

            val alertChannel = NotificationChannel(CHANNEL_ID_ALERT, "紧急健康预警", NotificationManager.IMPORTANCE_HIGH)
            alertChannel.description = "当体征异常时触发全局通知"
            alertChannel.enableLights(true)
            alertChannel.lightColor = Color.RED
            alertChannel.enableVibration(true)
            alertChannel.vibrationPattern = longArrayOf(0, 500, 200, 500)

            notificationManager.createNotificationChannels(listOf(silentChannel, alertChannel))
        }
    }

    private fun createSilentNotification(contentText: String): Notification {
        val pendingIntent = Intent(this, MainActivity::class.java).let {
            PendingIntent.getActivity(this, 0, it, PendingIntent.FLAG_IMMUTABLE)
        }
        return NotificationCompat.Builder(this, CHANNEL_ID_SILENT)
            .setContentTitle("生命体征监护系统")
            .setContentText(contentText)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentIntent(pendingIntent)
            .build()
    }

    private fun updateSilentNotification(text: String) {
        val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        manager.notify(FOREGROUND_NOTIFICATION_ID, createSilentNotification(text))
    }

    private fun triggerHighAlert(warningMessage: String) {
        val pendingIntent = Intent(this, MainActivity::class.java).let {
            PendingIntent.getActivity(this, 0, it, PendingIntent.FLAG_IMMUTABLE)
        }

        val alertNotification = NotificationCompat.Builder(this, CHANNEL_ID_ALERT)
            .setContentTitle("🚨 紧急预警！")
            .setContentText(warningMessage)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setColor(Color.RED)
            .setPriority(NotificationCompat.PRIORITY_MAX)
            .setCategory(NotificationCompat.CATEGORY_ALARM)
            .setFullScreenIntent(pendingIntent, true)
            .setAutoCancel(true)
            .build()

        val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        manager.notify(ALERT_NOTIFICATION_ID, alertNotification)

        triggerVibration()
    }

    private fun triggerVibration() {
        val vibrator = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val vibratorManager = getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as VibratorManager
            vibratorManager.defaultVibrator
        } else {
            @Suppress("DEPRECATION")
            getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
        }
        if (vibrator.hasVibrator()) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                vibrator.vibrate(VibrationEffect.createOneShot(1000, VibrationEffect.DEFAULT_AMPLITUDE))
            } else {
                @Suppress("DEPRECATION")
                vibrator.vibrate(1000)
            }
        }
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onDestroy() {
        super.onDestroy()
        webSocket?.cancel()
    }
}