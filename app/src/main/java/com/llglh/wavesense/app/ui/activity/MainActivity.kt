package com.llglh.wavesense.app.ui.activity

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.fragment.app.Fragment
import com.llglh.wavesense.R
import com.llglh.wavesense.app.network.Alarm
import com.llglh.wavesense.app.network.BaseListResponse
import com.llglh.wavesense.app.network.RetrofitClient
import com.llglh.wavesense.app.ui.fragment.HistoryFragment
import com.llglh.wavesense.app.ui.fragment.MonitorFragment
import com.llglh.wavesense.app.ui.fragment.ProfileFragment
import com.llglh.wavesense.databinding.ActivityMainBinding
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import android.app.PendingIntent
import android.content.Intent
import com.llglh.wavesense.app.ui.activity.AlarmDetailActivity
class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private lateinit var fragmentList: List<Fragment>

    // --- 🚨 全局报警监测变量 ---
    private val handler = Handler(Looper.getMainLooper())
    private val checkInterval = 5000L
    private var lastAlarmId = -1

    private val checkTask = object : Runnable {
        override fun run() {
            checkNewAlarms()
            handler.postDelayed(this, checkInterval)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // 1. 初始化 Fragment 列表
        fragmentList = listOf(
            MonitorFragment(),
            HistoryFragment(),
            ProfileFragment()
        )

        // 2. 设置底部导航栏点击事件
        binding.navView.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.navigation_monitor -> {
                    showFragment(fragmentList[0])
                    true
                }
                R.id.navigation_history -> {
                    showFragment(fragmentList[1])
                    true
                }
                R.id.navigation_profile -> {
                    showFragment(fragmentList[2])
                    true
                }
                else -> false
            }
        }

        // 3. 默认显示首页 (这次用新的方法)
        showFragment(fragmentList[0])

        // 4. 启动报警监测

        startGlobalMonitoring()
    }

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        // 更新当前 Activity 的 Intent，这一步很重要
        setIntent(intent)
        // 处理跳转
        handleNavigationIntent(intent)
    }
    // 👇 专门处理跳转逻辑的函数
    private fun handleNavigationIntent(intent: Intent?) {
        val target = intent?.getStringExtra("NAVIGATE_TO")

        if (target == "HISTORY") {
            // 🔥 这里的 R.id.navigation_history 一定要和你 menu xml 里的 ID 一致！！
            // 只要改了 selectedItemId，BottomNavigationView 的监听器就会自动帮你切换 Fragment
            binding.navView.selectedItemId = R.id.navigation_history
        }
    }

    // --- 👇 核心修改：使用 hide/show 而不是 replace ---
    private fun showFragment(targetFragment: Fragment) {
        val transaction = supportFragmentManager.beginTransaction()

        // 1. 先隐藏所有已经添加过的 Fragment
        fragmentList.forEach { fragment ->
            if (fragment != targetFragment && fragment.isAdded) {
                transaction.hide(fragment)
            }
        }

        // 2. 如果目标 Fragment 没添加过，就 add；如果添加过，就 show
        if (!targetFragment.isAdded) {
            // 第一次添加
            transaction.add(R.id.fragment_container, targetFragment)
        } else {
            // 之前添加过，直接显示出来（它的状态和图表都在！）
            transaction.show(targetFragment)
        }

        transaction.commit()
    }

    // --- 🚨 下面是报警逻辑 (保持不变) ---

    private fun startGlobalMonitoring() {
        handler.removeCallbacks(checkTask)
        handler.post(checkTask)
    }

    private fun checkNewAlarms() {
        val userId = getSharedPreferences("user_info", MODE_PRIVATE)
            .getInt("user_id", 1)
        val request = mapOf("user_id" to userId.toString())
        RetrofitClient.api.getAlarms(request).enqueue(object : Callback<BaseListResponse<Alarm>> {
            override fun onResponse(call: Call<BaseListResponse<Alarm>>, response: Response<BaseListResponse<Alarm>>) {
                if (response.isSuccessful && response.body()?.code == 200) {
                    val list = response.body()?.data
                    if (!list.isNullOrEmpty()) {
                        val newestAlarm = list[0]
                        val currentId = newestAlarm.id ?: 0
                        if (lastAlarmId != -1 && currentId > lastAlarmId) {
                            showNotification(newestAlarm)
                        }
                        lastAlarmId = currentId
                    }
                }
            }
            override fun onFailure(call: Call<BaseListResponse<Alarm>>, t: Throwable) {
                Log.e("MainActivity", "报警检测失败: ${t.message}")
            }
        })
    }

    private fun createNotificationChannel(soundUri: Uri?) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            // 🔥 关键修改：Channel ID 加上 uri 的哈希值，确保铃声变了 ID 就变
            val channelId = "WAVE_ALARM_ID_${soundUri.toString().hashCode()}"

            val name = "健康报警"
            val importance = NotificationManager.IMPORTANCE_HIGH

            val channel = NotificationChannel(channelId, name, importance).apply {
                description = "实时检测异常报警"
                enableVibration(true)

                // 🔥 关键修改：在创建渠道时就设置声音！
                if (soundUri != null) {
                    val audioAttributes = android.media.AudioAttributes.Builder()
                        .setContentType(android.media.AudioAttributes.CONTENT_TYPE_SONIFICATION)
                        .setUsage(android.media.AudioAttributes.USAGE_ALARM)
                        .build()
                    setSound(soundUri, audioAttributes)
                }
            }

            val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            manager.createNotificationChannel(channel)
        }
    }

    // 👇 1. 添加这个注解，屏蔽掉那个不智能的报错
    @android.annotation.SuppressLint("MissingPermission")
    private fun showNotification(alarm: Alarm) {
        val prefs = getSharedPreferences("app_settings", Context.MODE_PRIVATE)
        val isVibrate = prefs.getBoolean("pref_vibrate", true)
        val duration = prefs.getInt("pref_duration", 30)
        val ringtoneStr = prefs.getString("pref_ringtone", "")

        val vibratePattern = if (isVibrate) longArrayOf(0, 500, 200, 500) else longArrayOf(0)

        val soundUri = if (ringtoneStr.isNullOrEmpty()) {
            android.provider.Settings.System.DEFAULT_ALARM_ALERT_URI
        } else {
            Uri.parse(ringtoneStr)
        }

        createNotificationChannel(soundUri)
        val dynamicChannelId = "WAVE_ALARM_ID_${soundUri.toString().hashCode()}"

        // 🔥 新增：构建跳转 Intent
        val notifyId = System.currentTimeMillis().toInt() // 生成一个唯一的 ID
        val intent = Intent(this, AlarmDetailActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
            putExtra("EXTRA_TYPE", alarm.type)
            putExtra("EXTRA_DESC", alarm.description)
            putExtra("EXTRA_TIME", alarm.time)
            putExtra("EXTRA_NOTIFY_ID", notifyId) // 把 ID 传过去，方便那边取消
        }

        // PendingIntent 用于通知点击
        val pendingIntent = PendingIntent.getActivity(
            this,
            notifyId,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // 权限检查 (Android 13+)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.POST_NOTIFICATIONS), 101)
                return
            }
        }

        val builder = NotificationCompat.Builder(this, dynamicChannelId)
            .setSmallIcon(android.R.drawable.stat_notify_error)
            .setContentTitle("⚠️ 紧急警报: ${alarm.type}")
            .setContentText(alarm.description)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setVibrate(vibratePattern)
            .setSound(soundUri)
            .setTimeoutAfter(duration * 1000L)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent) // 👈 绑定点击事件！
            .setFullScreenIntent(pendingIntent, true) // 👈 重要：设为全屏通知（如果是锁屏状态会直接跳出来）

        NotificationManagerCompat.from(this).notify(notifyId, builder.build())
    }
    override fun onDestroy() {
        super.onDestroy()
        handler.removeCallbacks(checkTask)
    }
}