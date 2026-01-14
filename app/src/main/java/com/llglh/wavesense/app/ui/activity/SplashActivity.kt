package com.llglh.wavesense.app.ui.activity

import android.content.Intent
import android.graphics.Typeface
import android.os.Bundle
import android.os.CountDownTimer
import android.view.animation.BounceInterpolator
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.llglh.wavesense.R
import com.llglh.wavesense.databinding.ActivitySplashBinding
import es.dmoral.toasty.Toasty

class SplashActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySplashBinding
    private lateinit var countDownTimer: CountDownTimer
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding=ActivitySplashBinding.inflate(layoutInflater)
        setContentView(binding.root)
//        setContentView(R.layout.activity_splash)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        val customFont =Typeface.createFromAsset(assets,"fonts/a1.ttf")
        binding.titleTv.typeface=customFont

        startAnimation()
        countDown()
    }

    private fun countDown() {
        countDownTimer=object :CountDownTimer(5000,1000){
            override fun onTick(millisUntilFinished: Long) {
                binding.timeTv.text=(millisUntilFinished/1000+1).toString()

            }

            override fun onFinish() {
                val sp=getSharedPreferences("user_info", MODE_PRIVATE)
                if (sp.getBoolean("is_login",false)){
                    val intent= Intent(this@SplashActivity, MainActivity::class.java)
                    startActivity(intent)
                }
                else{
                    //跳转登录页面
                    val intent= Intent(this@SplashActivity, LoginActivity::class.java)
                    startActivity(intent)
                }
            }
        }.start()
    }

    private fun startAnimation() {
        binding.titleTv.apply {
            alpha=0f
            translationY=500f
            scaleX=0.8f
            scaleY=0.8f
            rotation=90f
            animate()
                .alpha(1f)
                .translationY(0f)
                .rotation(0f)
                .setDuration(1500)
                .withEndAction{
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
}