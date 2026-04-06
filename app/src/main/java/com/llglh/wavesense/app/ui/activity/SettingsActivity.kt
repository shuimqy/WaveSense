package com.llglh.wavesense.app.ui.activity

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.media.RingtoneManager
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.Switch
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.llglh.wavesense.R
import com.llglh.wavesense.app.network.RetrofitClient
import com.llglh.wavesense.app.network.SettingsResponse
import com.llglh.wavesense.app.network.UpdateSettingsRequest
import es.dmoral.toasty.Toasty
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class SettingsActivity : AppCompatActivity() {

    private lateinit var etDuration: EditText
    private lateinit var switchVibrate: Switch
    private lateinit var tvRingtoneName: TextView
    private var currentRingtoneUri: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)

        etDuration = findViewById(R.id.etDuration)
        switchVibrate = findViewById(R.id.switchVibrate)
        tvRingtoneName = findViewById(R.id.tvRingtoneName)

        loadLocalSettings()
        fetchSettingsFromCloud()

        findViewById<Button>(R.id.btnRingtone).setOnClickListener {
            val intent = Intent(RingtoneManager.ACTION_RINGTONE_PICKER)
            intent.putExtra(RingtoneManager.EXTRA_RINGTONE_TYPE, RingtoneManager.TYPE_ALARM)
            intent.putExtra(RingtoneManager.EXTRA_RINGTONE_TITLE, "选择报警铃声")
            if (currentRingtoneUri.isNotEmpty()) {
                intent.putExtra(RingtoneManager.EXTRA_RINGTONE_EXISTING_URI, Uri.parse(currentRingtoneUri))
            }
            startActivityForResult(intent, 999)
        }

        findViewById<Button>(R.id.btnSave).setOnClickListener {
            saveSettings()
        }
    }

    private fun loadLocalSettings() {
        val prefs = getSharedPreferences("app_settings", Context.MODE_PRIVATE)
        val duration = prefs.getInt("pref_duration", 30)
        val isVibrate = prefs.getBoolean("pref_vibrate", true)
        currentRingtoneUri = prefs.getString("pref_ringtone", "") ?: ""
        etDuration.setText(duration.toString())
        switchVibrate.isChecked = isVibrate
        updateRingtoneNameUI(currentRingtoneUri)
    }

    private fun fetchSettingsFromCloud() {
        val userId = getSharedPreferences("user_info", MODE_PRIVATE)
            .getInt("user_id", 1)
        val request = mapOf("user_id" to userId.toString())
        RetrofitClient.api.getSettings(request).enqueue(object : Callback<SettingsResponse> {
            override fun onResponse(call: Call<SettingsResponse>, response: Response<SettingsResponse>) {
                if (response.isSuccessful && response.body()?.code == 200) {
                    response.body()?.data?.let {
                        etDuration.setText(it.alarm_duration.toString())
                        switchVibrate.isChecked = (it.is_vibrate == 1)
                        currentRingtoneUri = it.ringtone_uri ?: ""
                        updateRingtoneNameUI(currentRingtoneUri)
                        saveToLocal(it.alarm_duration, it.is_vibrate == 1, currentRingtoneUri)
                    }
                }
            }
            override fun onFailure(call: Call<SettingsResponse>, t: Throwable) { }
        })
    }

    private fun saveSettings() {
        val durationStr = etDuration.text.toString()
        if (durationStr.isEmpty()) {
            Toasty.warning(this, "请输入时长").show()
            return
        }
        val duration = durationStr.toInt()
        val isVibrate = switchVibrate.isChecked

        // 1. 本地保存
        saveToLocal(duration, isVibrate, currentRingtoneUri)

        // 2. 构造请求对象 (使用 UpdateSettingsRequest 替代 Map)
        val userId = getSharedPreferences("user_info", MODE_PRIVATE)
            .getInt("user_id", 1)
        val request = UpdateSettingsRequest(
            user_id = userId,
            alarm_duration = duration,
            is_vibrate = if (isVibrate) 1 else 0,
            ringtone_uri = currentRingtoneUri
        )

        // 3. 发送请求
        RetrofitClient.api.updateSettings(request).enqueue(object : Callback<SettingsResponse> {
            override fun onResponse(call: Call<SettingsResponse>, response: Response<SettingsResponse>) {
                if (response.isSuccessful && response.body()?.code == 200) {
                    Toasty.success(this@SettingsActivity, "设置已保存").show()
                    finish()
                } else {
                    // 🔥 关键修改：提取真实的错误原因！
                    val errorMsg = if (response.body() != null) {
                        response.body()?.msg // 如果是 200 但 code!=200
                    } else {
                        // 如果是 500/404，从 errorBody 读取原生报错
                        response.errorBody()?.string() ?: "服务器内部错误 (${response.code()})"
                    }
                    Log.e("SettingsActivity", "保存失败: $errorMsg") // 在 Logcat 打印
                    Toasty.error(this@SettingsActivity, "保存失败: $errorMsg").show()
                }
            }
            override fun onFailure(call: Call<SettingsResponse>, t: Throwable) {
                Log.e("SettingsActivity", "网络错误", t)
                Toasty.error(this@SettingsActivity, "网络错误: ${t.message}").show()
            }
        })
    }

    private fun saveToLocal(duration: Int, isVibrate: Boolean, ringtone: String) {
        val prefs = getSharedPreferences("app_settings", Context.MODE_PRIVATE)
        prefs.edit().apply {
            putInt("pref_duration", duration)
            putBoolean("pref_vibrate", isVibrate)
            putString("pref_ringtone", ringtone)
            apply()
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == 999 && resultCode == Activity.RESULT_OK) {
            val uri: Uri? = data?.getParcelableExtra(RingtoneManager.EXTRA_RINGTONE_PICKED_URI)
            currentRingtoneUri = uri?.toString() ?: ""
            updateRingtoneNameUI(currentRingtoneUri)
        }
    }

    private fun updateRingtoneNameUI(uriStr: String) {
        if (uriStr.isEmpty()) {
            tvRingtoneName.text = "当前: 默认铃声"
        } else {
            val ringtone = RingtoneManager.getRingtone(this, Uri.parse(uriStr))
            val name = ringtone?.getTitle(this) ?: "未知铃声"
            tvRingtoneName.text = "当前: $name"
        }
    }
}