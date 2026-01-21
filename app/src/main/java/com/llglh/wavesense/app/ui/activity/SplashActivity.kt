package com.llglh.wavesense.app.ui.activity

import android.annotation.SuppressLint
import android.content.Intent
import android.graphics.Typeface
import android.os.Bundle
import android.os.CountDownTimer
import android.view.animation.BounceInterpolator
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.llglh.wavesense.R
import com.llglh.wavesense.databinding.ActivitySplashBinding

@SuppressLint("CustomSplashScreen")
class SplashActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySplashBinding
    private lateinit var countDownTimer: CountDownTimer

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivitySplashBinding.inflate(layoutInflater)
        setContentView(binding.root)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // 设置字体
        val customFont = Typeface.createFromAsset(assets, "fonts/a1.ttf")
        binding.titleTv.typeface = customFont

        startAnimation()
        countDown()
    }

    private fun countDown() {
        countDownTimer = object : CountDownTimer(5000, 1000) {
            override fun onTick(millisUntilFinished: Long) {
                binding.timeTv.text = (millisUntilFinished / 1000 + 1).toString()
            }

            override fun onFinish() {
                val sp = getSharedPreferences("user_info", MODE_PRIVATE)

                // 🚀 核心修改区域开始
                if (sp.getBoolean("is_login", false)) {
                    // 情况1：已登录 -> 直接进主页
                    val intent = Intent(this@SplashActivity, MainActivity::class.java)
                    // ⚠️ 关键代码：清空任务栈，让 MainActivity 成为新的起点
                    intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK
                    startActivity(intent)
                } else {
                    // 情况2：未登录 -> 去登录页
                    val intent = Intent(this@SplashActivity, LoginActivity::class.java)
                    startActivity(intent)
                    finish() // ⚠️ 关键代码：必须调用 finish() 关闭引导页，否则按返回键会回到这里
                }
                // 🚀 核心修改区域结束
            }
        }.start()
    }

    private fun startAnimation() {
        binding.titleTv.apply {
            alpha = 0f
            translationY = 500f
            scaleX = 0.8f
            scaleY = 0.8f
            rotation = 90f
            animate()
                .alpha(1f)
                .translationY(0f)
                .rotation(0f)
                .setDuration(1500)
                .withEndAction {
                    animate()
                        .scaleX(1.5f)
                        .scaleY(1.5f)
                        .setDuration(800)
                        .setInterpolator(BounceInterpolator())
                        .start()
                }
                .start()
        }
    }

    // 建议加上：防止用户在倒计时没结束时按退出，导致倒计时还在后台跑
    override fun onDestroy() {
        super.onDestroy()
        if (::countDownTimer.isInitialized) {
            countDownTimer.cancel()
        }
    }
}