package com.llglh.wavesense.app.ui.fragment

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.llglh.wavesense.databinding.FragmentHistoryBinding
import com.llglh.wavesense.app.network.Alarm
import com.llglh.wavesense.app.network.BaseListResponse
import com.llglh.wavesense.app.network.RetrofitClient
import com.llglh.wavesense.app.ui.adapter.AlarmAdapter
import es.dmoral.toasty.Toasty
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class HistoryFragment : Fragment() {

    private var _binding: FragmentHistoryBinding? = null
    private val binding get() = _binding!!
    private lateinit var adapter: AlarmAdapter

    // 1. 定义定时器 (Handler)
    private val handler = Handler(Looper.getMainLooper())
    // 2. 定义自动刷新间隔 (这里设为 5000毫秒 = 5秒)
    private val refreshInterval = 5000L

    // 3. 定义刷新任务
    private val refreshTask = object : Runnable {
        override fun run() {
            fetchAlarms(isAutoRefresh = true) // 传入标记，如果是自动刷新就不弹窗打扰用户
            // 循环调用
            handler.postDelayed(this, refreshInterval)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHistoryBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // 初始化 RecyclerView
        binding.recyclerView.layoutManager = LinearLayoutManager(context)
        adapter = AlarmAdapter(emptyList())
        binding.recyclerView.adapter = adapter

        // 首次进入立即加载一次
        fetchAlarms(isAutoRefresh = false)
    }

    // 在页面可见时开始刷新
    override fun onResume() {
        super.onResume()
        startAutoRefresh()
    }

    // 在页面不可见时停止刷新 (省电)
    override fun onPause() {
        super.onPause()
        stopAutoRefresh()
    }

    private fun startAutoRefresh() {
        handler.removeCallbacks(refreshTask)
        handler.postDelayed(refreshTask, refreshInterval)
    }

    private fun stopAutoRefresh() {
        handler.removeCallbacks(refreshTask)
    }

    // 修改 fetchAlarms，增加一个参数来区分是“手动打开”还是“自动静默刷新”
    private fun fetchAlarms(isAutoRefresh: Boolean) {
        val request = mapOf("user_id" to "1")

        RetrofitClient.api.getAlarms(request).enqueue(object : Callback<BaseListResponse<Alarm>> {
            override fun onResponse(
                call: Call<BaseListResponse<Alarm>>,
                response: Response<BaseListResponse<Alarm>>
            ) {
                if (response.isSuccessful && response.body()?.code == 200) {
                    val alarms = response.body()?.data ?: emptyList()

                    // 只有当数据不为空时才更新列表
                    // 如果你想做得更完美，可以比较一下新旧数据是否一样，不一样再刷新
                    if (alarms.isNotEmpty()) {
                        adapter.updateList(alarms)
                    } else if (!isAutoRefresh) {
                        // 只有刚进页面没数据时才提示，自动刷新时没数据不要一直弹窗
                        Toasty.info(requireContext(), "暂无报警记录").show()
                    }
                } else {
                    if (!isAutoRefresh) {
                        Toasty.error(requireContext(), "获取失败").show()
                    }
                }
            }

            override fun onFailure(call: Call<BaseListResponse<Alarm>>, t: Throwable) {
                if (!isAutoRefresh) {
                    Toasty.error(requireContext(), "网络错误: ${t.message}").show()
                }
            }
        })
    }

    override fun onDestroyView() {
        super.onDestroyView()
        // 彻底销毁时也要停止
        stopAutoRefresh()
        _binding = null
    }
}