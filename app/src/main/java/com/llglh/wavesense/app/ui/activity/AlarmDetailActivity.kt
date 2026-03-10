package com.llglh.wavesense.app.ui.activity

import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.llglh.wavesense.R

class AlarmDetailActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_alarm_detail)

        val type = intent.getStringExtra("EXTRA_TYPE") ?: "未知异常"
        val desc = intent.getStringExtra("EXTRA_DESC") ?: ""
        val time = intent.getStringExtra("EXTRA_TIME") ?: ""
        val notificationId = intent.getIntExtra("EXTRA_NOTIFY_ID", 0)

        findViewById<TextView>(R.id.tvTitle).text = "⚠️ $type"
        findViewById<TextView>(R.id.tvDescription).text = desc
        findViewById<TextView>(R.id.tvTime).text = "报警时间: $time"

        findViewById<Button>(R.id.btnConfirm).setOnClickListener {
            // 1. 停止警报 (取消通知)
            val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            manager.cancel(notificationId)

            // 2. 跳转到 MainActivity
            val intent = Intent(this, MainActivity::class.java)

            // 🔥 关键 Flag：
            // FLAG_ACTIVITY_CLEAR_TOP: 如果 MainActivity 已经在下面，就把上面的 AlarmDetailActivity 清掉，露出来
            // FLAG_ACTIVITY_SINGLE_TOP: 如果 MainActivity 露出来后发现就是自己，就不重建，而是调用 onNewIntent
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP

            // 🔥 关键暗号：告诉主页我要去哪里
            intent.putExtra("NAVIGATE_TO", "HISTORY")

            startActivity(intent)

            // 3. 关闭当前页 (双保险)
            finish()
        }
    }
}