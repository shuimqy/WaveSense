package com.llglh.wavesense.app.ui.activity

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.fragment.app.Fragment
import com.llglh.wavesense.R
import com.llglh.wavesense.app.ui.fragment.HistoryFragment
import com.llglh.wavesense.app.ui.fragment.ProfileFragment
import com.llglh.wavesense.app.ui.fragment.MonitorFragment
import com.llglh.wavesense.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private lateinit var fragmentlist:List<Fragment>
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        fragmentlist = listOf(
            MonitorFragment(),
            HistoryFragment(),
            ProfileFragment()
        )


        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        binding.bottomNavBar.setOnItemSelectedListener {
            when (it.itemId) {
                R.id.home -> {
                    showFragment(fragmentlist[0])
                    true

                }
                R.id.news -> {
                    showFragment(fragmentlist[1])
                    true
                }
                R.id.profile -> {
                    showFragment(fragmentlist[2])
                    true
                }
                else -> {
                    false
                }
            }
        }
        showFragment(fragmentlist[0])
    }
    fun showFragment(fragment: Fragment) {
        supportFragmentManager.beginTransaction().replace(R.id.fragmentContainerView, fragment).commit()
    }
}