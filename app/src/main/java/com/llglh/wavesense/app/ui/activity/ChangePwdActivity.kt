package com.llglh.wavesense.app.ui.activity

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.llglh.wavesense.databinding.ActivityChangePwdBinding
import com.llglh.wavesense.app.network.ChangePwdRequest
import com.llglh.wavesense.app.network.LoginResponse
import com.llglh.wavesense.app.network.RetrofitClient
import es.dmoral.toasty.Toasty
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class ChangePwdActivity : AppCompatActivity() {
    private lateinit var binding: ActivityChangePwdBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityChangePwdBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupListener()
    }

    private fun setupListener() {
        binding.btnCancel.setOnClickListener { finish() }

        binding.btnSubmit.setOnClickListener {
            submitChange()
        }
    }

    private fun submitChange() {
        val oldPwd = binding.oldPwdInput.text.toString().trim()
        val newPwd = binding.newPwdInput.text.toString().trim()
        val confirmPwd = binding.confirmPwdInput.text.toString().trim()

        // 1. 本地校验
        if (oldPwd.isEmpty() || newPwd.isEmpty()) {
            Toasty.error(this, "密码不能为空").show()
            return
        }
        if (newPwd.length < 6 || newPwd.length > 12) {
            binding.newPwdLayout.error = "密码长度需6-12位"
            return
        }
        if (newPwd != confirmPwd) {
            binding.confirmPwdLayout.error = "两次新密码不一致"
            return
        }
        if (oldPwd == newPwd) {
            Toasty.warning(this, "新密码不能和旧密码一样").show()
            return
        }

        // 2. 获取当前用户名
        val sp = getSharedPreferences("user_info", MODE_PRIVATE)
        val username = sp.getString("username", "") ?: ""

        if (username.isEmpty()) {
            Toasty.error(this, "用户状态异常，请重新登录").show()
            return
        }

        // 3. 发送请求
        val request = ChangePwdRequest(username, oldPwd, newPwd)
        Toasty.info(this, "正在修改...").show()

        RetrofitClient.api.changePassword(request).enqueue(object : Callback<LoginResponse> {
            override fun onResponse(call: Call<LoginResponse>, response: Response<LoginResponse>) {
                val result = response.body()
                if (response.isSuccessful && result != null && result.code == 200) {
                    Toasty.success(this@ChangePwdActivity, "修改成功！请重新登录").show()

                    // 4. 强制下线逻辑
                    sp.edit().clear().apply() // 清空登录状态

                    // 跳转回登录页，并清空所有页面栈
                    val intent = Intent(this@ChangePwdActivity, LoginActivity::class.java)
                    intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                    startActivity(intent)
                } else {
                    Toasty.error(this@ChangePwdActivity, result?.msg ?: "修改失败").show()
                }
            }

            override fun onFailure(call: Call<LoginResponse>, t: Throwable) {
                Toasty.error(this@ChangePwdActivity, "网络连接失败").show()
            }
        })
    }
}