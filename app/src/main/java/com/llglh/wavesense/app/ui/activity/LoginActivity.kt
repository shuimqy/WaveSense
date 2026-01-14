package com.llglh.wavesense.app.ui.activity

import android.content.Intent
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.edit
import androidx.core.widget.doAfterTextChanged
import com.llglh.wavesense.app.db.DataBaseHelper
import com.llglh.wavesense.databinding.ActivityLoginBinding
import es.dmoral.toasty.Toasty

class LoginActivity : AppCompatActivity() {
    private lateinit var binding: ActivityLoginBinding
    private lateinit var dbHelper: DataBaseHelper
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityLoginBinding.inflate(layoutInflater)
        dbHelper = DataBaseHelper(this)
        setContentView(binding.root)
//        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
//            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
//            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
//            insets
//        }
        setupListener()
    }

    //监听输入框,变化后更新错误信息
    private fun setupListener() {
        //用户名监听
        binding.usernameInput.doAfterTextChanged {
            if (!it.isNullOrEmpty() && binding.usernameLayout.error != null) {
                binding.usernameLayout.error = null
            }
        }
        //密码监听
        binding.passwordInput.doAfterTextChanged {
            if (!it.isNullOrEmpty() && binding.passwordLayout.error != null) {
                binding.passwordLayout.error = null
            }
        }
        binding.loginBtn.setOnClickListener{
            login()
        }
        binding.jumpRegister.setOnClickListener {
            startActivity(Intent(this, RegisterActivity::class.java))
        }
    }

    private fun login() {
        val username = binding.usernameInput.text.toString()
        val password = binding.passwordInput.text.toString()
        //非空校验
        if (username.isBlank() || password.isBlank()) {
            if (username.isBlank()) {
                binding.usernameLayout.error = "用户名不能为空"
                 return
            }
            if (password.isBlank()) {
                binding.passwordLayout.error = "密码不能为空"
                 return
            }
        }
        //密码位数6-12位
        if (password.length < 6 || password.length > 12) {
            binding.passwordLayout.error = "密码长度必须在6-12位之间"
             return
        }
        //开始登录
        if (dbHelper.login(username, password)) {
            //缓存登录状态
            val sp = getSharedPreferences("user_info", MODE_PRIVATE)
            sp.edit {
                putBoolean("is_login", true)
            }
            Toasty.success(this, "登录成功").show()
            startActivity(Intent(this, MainActivity::class.java))
            finish()
        }
        else {
            binding.usernameLayout.error = "用户名或密码错误"
        }
    }
}