package com.llglh.wavesense.app.ui.activity

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.llglh.wavesense.R
import com.llglh.wavesense.app.ui.fragment.HistoryFragment
import com.llglh.wavesense.app.ui.fragment.MonitorFragment
import com.llglh.wavesense.app.ui.fragment.ProfileFragment
import com.llglh.wavesense.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private lateinit var fragmentList: List<Fragment>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // 请求通知权限 (Android 13+)
        requestNotificationPermission()

        // 1. 初始化 Fragment 列表
        fragmentList = listOf(
            MonitorFragment(),
            HistoryFragment(),
            ProfileFragment()
        )

        // 2. 设置底部导航栏点击事件
        binding.navView.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.navigation_monitor -> { showFragment(fragmentList[0]); true }
                R.id.navigation_history -> { showFragment(fragmentList[1]); true }
                R.id.navigation_profile -> { showFragment(fragmentList[2]); true }
                else -> false
            }
        }

        // 3. 默认显示首页
        showFragment(fragmentList[0])

        // 4. 处理从通知跳转过来的 Intent
        handleNavigationIntent(intent)
    }

    private fun requestNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS)
                != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(
                    this,
                    arrayOf(Manifest.permission.POST_NOTIFICATIONS),
                    101
                )
            }
        }
    }

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        setIntent(intent)
        handleNavigationIntent(intent)
    }

    private fun handleNavigationIntent(intent: Intent?) {
        val target = intent?.getStringExtra("NAVIGATE_TO")
        if (target == "HISTORY") {
            binding.navView.selectedItemId = R.id.navigation_history
        }
    }

    private fun showFragment(targetFragment: Fragment) {
        val transaction = supportFragmentManager.beginTransaction()
        fragmentList.forEach { fragment ->
            if (fragment != targetFragment && fragment.isAdded) {
                transaction.hide(fragment)
            }
        }
        if (!targetFragment.isAdded) {
            transaction.add(R.id.fragment_container, targetFragment)
        } else {
            transaction.show(targetFragment)
        }
        transaction.commit()
    }
}
