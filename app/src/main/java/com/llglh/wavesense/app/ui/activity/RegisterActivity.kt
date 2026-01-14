package com.llglh.wavesense.app.ui.activity

import android.content.Intent
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.widget.doAfterTextChanged
import com.llglh.wavesense.R
import com.llglh.wavesense.app.db.DataBaseHelper
import com.llglh.wavesense.databinding.ActivityRegisterBinding
import es.dmoral.toasty.Toasty

class RegisterActivity : AppCompatActivity() {

    private lateinit var binding: ActivityRegisterBinding
    private lateinit var dbHelper: DataBaseHelper
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityRegisterBinding.inflate(layoutInflater)
        dbHelper = DataBaseHelper(this)
        setContentView(binding.root)
//        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
//            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
//            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
//            insets
//        }
        setupListener()
        binding.registerBtn.setOnClickListener {
            register()
        }
    }

    //监听输入框,变化后更新错误信息
    private fun setupListener() {
        binding.usernameInput.doAfterTextChanged {
            if(!it.isNullOrEmpty()&&binding.usernameLayout.error!=null){
                binding.usernameLayout.error = null
            }
        }
        binding.passwordInput.doAfterTextChanged {
            if(!it.isNullOrEmpty()&&binding.passwordLayout.error!=null){
                binding.passwordLayout.error = null
            }
        }
        binding.passwordAgainInput.doAfterTextChanged {
            if(!it.isNullOrEmpty()&&binding.passwordAgainLayout.error!=null){
                binding.passwordAgainLayout.error = null
            }
        }
    }


    private fun register() {
        //1.获取输入框的内容
        val username = binding.usernameInput.text.toString()
        val password = binding.passwordInput.text.toString()
        val passwordConfirm = binding.passwordAgainInput.text.toString()
        //2.判断输入框内容是否为空
        when{
            username.isEmpty() -> {
                binding.usernameLayout.error = "用户名不能为空"
        }
            password.isEmpty() -> {
                binding.passwordLayout.error = "密码不能为空"
            }
            passwordConfirm.isEmpty() -> {
                binding.passwordAgainLayout.error = "请再次输入密码"
            }
            password != passwordConfirm -> {
                binding.passwordAgainLayout.error = "两次输入的密码不一致"
            }
        }
        //检测用户是否注册
        if (dbHelper.checkUserExist(username)) {
            Toasty.error(this, "用户已存在").show()
            return
        }
        if (!dbHelper.registerUser(username, password)) {
            Toasty.error(this, "注册失败").show()
            return
        }
        Toasty.success(this, "注册成功").show()
        //跳转页面
        startActivity(Intent(this, LoginActivity::class.java))
        finish()

        //密码在6-12位之间
        if(password.length !in 6..12){
            binding.passwordLayout.error = "密码长度必须在6-12位之间"
            binding.passwordAgainLayout.error = "密码长度必须在6-12位之间"
        }
    }
}