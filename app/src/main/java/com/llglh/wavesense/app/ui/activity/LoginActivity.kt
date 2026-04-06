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
import com.llglh.wavesense.app.network.LoginRequest
import com.llglh.wavesense.app.network.LoginResponse
import com.llglh.wavesense.app.network.RetrofitClient
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
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



    // ... (onCreate 和 setupListener 保持不变) ...

    private fun login() {
        val username = binding.usernameInput.text.toString()
        val password = binding.passwordInput.text.toString()

        // --- 1. 保持你原有的非空校验 ---
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
        // --- 2. 保持你原有的长度校验 ---
        if (password.length < 6 || password.length > 12) {
            binding.passwordLayout.error = "密码长度必须在6-12位之间"
            return
        }

        // --- 3. 核心改造：发起云端网络请求 ---
        // (旧代码 dbHelper.login 被弃用)

        val request = LoginRequest(username, password)

        RetrofitClient.api.login(request).enqueue(object : Callback<LoginResponse> {
            override fun onResponse(call: Call<LoginResponse>, response: Response<LoginResponse>) {
                val result = response.body()

                // 判断 HTTP 状态码是否为 200 且业务 code 为 200
                // ... (在 onResponse 成功的回调里) ...

                if (response.isSuccessful && result != null && result.code == 200) {
                    Toasty.success(this@LoginActivity, "云端登录成功！").show()

                    // 保存数据 (包括 user_id)
                    getSharedPreferences("user_info", MODE_PRIVATE).edit {
                        putBoolean("is_login", true)
                        putInt("user_id", result.data?.id ?: 1)
                        putString("username", result.data?.username)
                        putString("role", result.data?.role)
                    }

                    // 🚀 【关键修改】跳转并清空返回栈
                    val intent = Intent(this@LoginActivity, MainActivity::class.java)
                    // FLAG_ACTIVITY_CLEAR_TASK: 清空任务栈里所有旧的 Activity
                    // FLAG_ACTIVITY_NEW_TASK: 开启一个新的任务栈
                    intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK
                    startActivity(intent)

                    // 这一句其实不需要了(因为用了 CLEAR_TASK)，但留着双保险也没事
                    // finish()
                } else {
                    // 😭 登录失败 (密码错误或账号不存在)
                    binding.usernameLayout.error = result?.msg ?: "登录失败"
                    Toasty.error(this@LoginActivity, result?.msg ?: "账号或密码错误").show()
                }
            }

            override fun onFailure(call: Call<LoginResponse>, t: Throwable) {
                // 🔌 网络完全不通 (如服务器没开，IP填错，手机没网)
                t.printStackTrace()
                Toasty.error(this@LoginActivity, "连接服务器失败，请检查网络").show()
            }
        })
    }
}