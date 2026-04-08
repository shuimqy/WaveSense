package com.llglh.wavesense.app.service

import android.app.*
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.media.AudioAttributes
import android.net.Uri
import android.os.Build
import android.os.IBinder
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import android.util.Log
import androidx.core.app.NotificationCompat
import com.llglh.wavesense.R
import com.llglh.wavesense.app.ui.activity.AlarmDetailActivity
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

    private val FOREGROUND_NOTIFICATION_ID = 1001

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
            notificationManager.createNotificationChannel(silentChannel)
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
        // 读取用户在 SettingsActivity 中保存的铃声和震动配置
        val prefs = getSharedPreferences("app_settings", Context.MODE_PRIVATE)
        val ringtoneStr = prefs.getString("pref_ringtone", "")
        val isVibrate = prefs.getBoolean("pref_vibrate", true)

        val soundUri: Uri = if (ringtoneStr.isNullOrEmpty()) {
            android.provider.Settings.System.DEFAULT_ALARM_ALERT_URI
        } else {
            Uri.parse(ringtoneStr)
        }

        // 用铃声 URI 哈希作为 Channel ID，确保换铃声时能生效（Channel 创建后声音不可改）
        val dynamicChannelId = "wave_alert_${soundUri.toString().hashCode()}"
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            if (manager.getNotificationChannel(dynamicChannelId) == null) {
                val channel = NotificationChannel(dynamicChannelId, "紧急健康预警", NotificationManager.IMPORTANCE_HIGH).apply {
                    description = "当体征异常时触发全局通知"
                    enableLights(true)
                    lightColor = Color.RED
                    enableVibration(isVibrate)
                    if (isVibrate) vibrationPattern = longArrayOf(0, 500, 200, 500)
                    val audioAttr = AudioAttributes.Builder()
                        .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                        .setUsage(AudioAttributes.USAGE_ALARM)
                        .build()
                    setSound(soundUri, audioAttr)
                }
                manager.createNotificationChannel(channel)
            }
        }

        // 点击通知跳转到 AlarmDetailActivity
        val notifyId = System.currentTimeMillis().toInt()
        val detailIntent = Intent(this, AlarmDetailActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
            putExtra("EXTRA_TYPE", "生命体征异常")
            putExtra("EXTRA_DESC", warningMessage)
            putExtra("EXTRA_TIME", java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss", java.util.Locale.getDefault()).format(java.util.Date()))
            putExtra("EXTRA_NOTIFY_ID", notifyId)
        }
        val pendingIntent = PendingIntent.getActivity(
            this, notifyId, detailIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val alertNotification = NotificationCompat.Builder(this, dynamicChannelId)
            .setContentTitle("🚨 紧急预警！")
            .setContentText(warningMessage)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setColor(Color.RED)
            .setPriority(NotificationCompat.PRIORITY_MAX)
            .setCategory(NotificationCompat.CATEGORY_ALARM)
            .setSound(soundUri)
            .setVibrate(if (isVibrate) longArrayOf(0, 500, 200, 500) else longArrayOf(0))
            .setContentIntent(pendingIntent)
            .setFullScreenIntent(pendingIntent, true)   // 锁屏/前台时弹出全屏覆盖
            .setAutoCancel(true)
            .build()

        val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        manager.notify(notifyId, alertNotification)

        if (isVibrate) triggerVibration()
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