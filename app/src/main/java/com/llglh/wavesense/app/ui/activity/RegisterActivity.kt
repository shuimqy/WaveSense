package com.llglh.wavesense.app.ui.activity

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.widget.doAfterTextChanged
import com.llglh.wavesense.databinding.ActivityRegisterBinding
import com.llglh.wavesense.app.network.LoginResponse
import com.llglh.wavesense.app.network.RegisterRequest
import com.llglh.wavesense.app.network.RetrofitClient
import es.dmoral.toasty.Toasty
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class RegisterActivity : AppCompatActivity() {
    private lateinit var binding: ActivityRegisterBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityRegisterBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupListener()
    }

    private fun setupListener() {
        // 1. 输入框监听：输入时自动清除错误提示
        binding.usernameInput.doAfterTextChanged {
            if (!it.isNullOrEmpty()) binding.usernameLayout.error = null
        }
        binding.passwordInput.doAfterTextChanged {
            if (!it.isNullOrEmpty()) binding.passwordLayout.error = null
        }
        // ✅ 修正：这里用 passwordAgainInput 对应你的XML
        binding.passwordAgainInput.doAfterTextChanged {
            if (!it.isNullOrEmpty()) binding.passwordAgainLayout.error = null
        }

        // 2. 注册按钮点击事件
        binding.registerBtn.setOnClickListener {
            register()
        }

        // 3. 返回登录页点击事件
        binding.jumpLogin.setOnClickListener {
            finish() // 关闭当前页面，自动返回上一页
        }
    }

    private fun register() {
        val username = binding.usernameInput.text.toString().trim()
        val password = binding.passwordInput.text.toString().trim()
        // ✅ 修正：这里用 passwordAgainInput
        val confirmPassword = binding.passwordAgainInput.text.toString().trim()

        // --- 步骤1：本地校验 ---

        // 非空校验
        if (username.isEmpty()) {
            binding.usernameLayout.error = "用户名不能为空"
            return
        }
        if (password.isEmpty()) {
            binding.passwordLayout.error = "密码不能为空"
            return
        }

        // 长度校验
        if (password.length < 6 || password.length > 12) {
            binding.passwordLayout.error = "密码长度必须在6-12位之间"
            return
        }

        // 确认密码校验
        // ✅ 修正：这里用 passwordAgainLayout
        if (password != confirmPassword) {
            binding.passwordAgainLayout.error = "两次输入的密码不一致"
            return
        }

        // --- 步骤2：发起云端注册请求 ---

        Toasty.info(this, "正在提交注册...").show()

        val request = RegisterRequest(
            username = username,
            password = password,
            role = "family" // 默认角色
        )

        RetrofitClient.api.register(request).enqueue(object : Callback<LoginResponse> {
            override fun onResponse(call: Call<LoginResponse>, response: Response<LoginResponse>) {
                val result = response.body()

                if (response.isSuccessful && result != null && result.code == 200) {
                    // 🎉 注册成功
                    Toasty.success(this@RegisterActivity, "注册成功！请登录").show()
                    finish()
                } else {
                    // 😭 注册失败
                    val errorMsg = result?.msg ?: "注册失败"
                    binding.usernameLayout.error = errorMsg
                    Toasty.error(this@RegisterActivity, errorMsg).show()
                }
            }

            override fun onFailure(call: Call<LoginResponse>, t: Throwable) {
                t.printStackTrace()
                Toasty.error(this@RegisterActivity, "连接服务器失败").show()
            }
        })
    }
}