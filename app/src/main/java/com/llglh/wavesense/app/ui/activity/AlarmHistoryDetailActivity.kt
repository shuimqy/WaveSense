package com.llglh.wavesense.app.ui.activity

import android.graphics.Color
import android.os.Bundle
import android.widget.Button
import android.widget.ImageButton
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.llglh.wavesense.R

class AlarmHistoryDetailActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_alarm_history_detail)

        // 获取传递的数据
        val type = intent.getStringExtra("EXTRA_TYPE") ?: "未知异常"
        val desc = intent.getStringExtra("EXTRA_DESC") ?: ""
        val time = intent.getStringExtra("EXTRA_TIME") ?: ""
        val level = intent.getIntExtra("EXTRA_LEVEL", 1)

        // 设置警报类型
        findViewById<TextView>(R.id.tvAlarmType).text = type

        // 设置时间
        findViewById<TextView>(R.id.tvAlarmTime).text = time

        // 设置描述
        findViewById<TextView>(R.id.tvAlarmDescription).text = desc

        // 设置警报级别
        val tvLevel = findViewById<TextView>(R.id.tvAlarmLevel)
        val tvLevelDesc = findViewById<TextView>(R.id.tvLevelDesc)

        when (level) {
            2 -> {
                tvLevel.text = "严重"
                tvLevel.setTextColor(Color.parseColor("#D32F2F"))
                tvLevelDesc.text = "需要立即处理"
            }
            else -> {
                tvLevel.text = "警告"
                tvLevel.setTextColor(Color.parseColor("#FF9800"))
                tvLevelDesc.text = "请注意查看"
            }
        }

        // 返回按钮
        findViewById<ImageButton>(R.id.btnBack).setOnClickListener {
            finish()
        }

        // 关闭按钮
        findViewById<Button>(R.id.btnClose).setOnClickListener {
            finish()
        }
    }
}
