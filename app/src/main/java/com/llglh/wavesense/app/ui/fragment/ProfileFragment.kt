package com.llglh.wavesense.app.ui.fragment

import android.content.Context.MODE_PRIVATE
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.llglh.wavesense.app.ui.activity.ChangePwdActivity
import com.llglh.wavesense.databinding.FragmentProfileBinding
import com.llglh.wavesense.app.ui.activity.LoginActivity
import com.llglh.wavesense.app.ui.activity.SettingsActivity
import es.dmoral.toasty.Toasty

class ProfileFragment : Fragment() {

    private var _binding: FragmentProfileBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentProfileBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // 1. 初始化显示数据
        initView()

        // 2. 设置点击事件
        setupListener()
    }

    private fun initView() {
        // 从 SP 中读取登录时保存的信息
        val sp = requireActivity().getSharedPreferences("user_info", MODE_PRIVATE)
        val username = sp.getString("username", "未登录")
        val role = sp.getString("role", "visitor")

        // 显示到界面上
        binding.tvUsername.text = username

        // 翻译一下角色名 (family -> 家属)
        val roleName = when(role) {
            "family" -> "家属 / 监护人"
            "nurse" -> "医护人员"
            "admin" -> "系统管理员"
            else -> "访客"
        }
        binding.tvRole.text = roleName
    }

    private fun setupListener() {
        // 我的设备
        binding.btnMyDevice.setOnClickListener {
            Toasty.info(requireContext(), "正在开发中...").show()
        }

        // 修改密码
        binding.btnChangePwd.setOnClickListener {
            Toasty.info(requireContext(), "后续将跳转修改密码页").show()
        }

        // 关于
        binding.btnAbout.setOnClickListener {
            Toasty.normal(requireContext(), "WaveSense v1.0 \n基于物联网的跌倒检测系统").show()
        }

        // 🚀 退出登录 (核心功能)
        binding.btnLogout.setOnClickListener {
            // 1. 清除本地存储的登录状态
            val sp = requireActivity().getSharedPreferences("user_info", MODE_PRIVATE)
            sp.edit().clear().apply() // clear() 会把所有存的数据都删掉

            // 2. 提示
            Toasty.success(requireContext(), "已退出登录").show()

            // 3. 跳转回登录页，并清空任务栈 (防止按返回键又回来)
            val intent = Intent(requireActivity(), LoginActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
        }
        // 在 setupListener 里
        binding.btnChangePwd.setOnClickListener {
            // 跳转到修改密码页
            startActivity(Intent(requireActivity(), ChangePwdActivity::class.java))
        }
        binding.btnAlarmPreference.setOnClickListener {
            // 跳转到报警设置页
            startActivity(Intent(requireActivity(), SettingsActivity::class.java))
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}