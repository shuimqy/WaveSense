package com.llglh.wavesense.app.ui.fragment

import android.graphics.Color
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.github.mikephil.charting.components.LimitLine
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.components.YAxis
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.llglh.wavesense.R
import com.llglh.wavesense.app.network.BaseListResponse
import com.llglh.wavesense.app.network.RetrofitClient
import com.llglh.wavesense.databinding.FragmentMonitorBinding
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class MonitorFragment : Fragment() {

    private var _binding: FragmentMonitorBinding? = null
    private val binding get() = _binding!!

    // 定时器，用于轮询数据
    private val handler = Handler(Looper.getMainLooper())
    // 轮询间隔：2秒 (和你的 mock_device.py 保持一致，效果最好)
    private val updateInterval = 2000L

    private val updateTask = object : Runnable {
        override fun run() {
            fetchRealTimeData()
            handler.postDelayed(this, updateInterval)
        }
    }

    // 图表相关变量
    private var chartXIndex = 0f // X轴的计数器

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentMonitorBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // 1. 初始化图表样式
        setupChartStyle()

        // 2. 初始化空数据
        initChartData()

        // 3. 开始轮询
        startMonitoring()
    }

    // --- 🔥 核心：配置图表变成 ECG 风格 ---
    private fun setupChartStyle() {
        val chart = binding.ecgChart

        // 基础设置
        chart.description.isEnabled = false // 不显示描述文字
        chart.legend.isEnabled = false      // 不显示图例
        chart.setTouchEnabled(false)        // 禁止手指触摸缩放等，纯展示
        chart.setViewPortOffsets(0f, 0f, 0f, 0f) // 让图表充满整个控件区域
        chart.setBackgroundColor(Color.parseColor("#FAFAFA")) // 设置一个非常淡的背景色

        // X轴设置 (底部时间轴)
        val xAxis = chart.xAxis
        xAxis.position = XAxis.XAxisPosition.BOTTOM_INSIDE
        xAxis.setDrawGridLines(false) // 不画网格竖线
        xAxis.setDrawLabels(false)    // 不画X轴数字，让它看起来更像纯波形
        xAxis.textColor = Color.GRAY

        // Y轴设置 (左侧心率轴)
        val leftAxis = chart.axisLeft
        leftAxis.setDrawGridLines(false) // 不画网格横线，更有科技感
        leftAxis.textColor = Color.GRAY
        leftAxis.axisMinimum = 40f // Y轴最小值：40 BPM
        leftAxis.axisMaximum = 150f // Y轴最大值：150 BPM
        leftAxis.setDrawZeroLine(false)

        // 添加一条红色的警戒线 (例如 120 BPM)
        val limitLine = LimitLine(120f, "高心率警戒")
        limitLine.lineWidth = 1f
        limitLine.lineColor = Color.parseColor("#FF5252") // 红色
        limitLine.textColor = Color.parseColor("#FF5252")
        limitLine.textSize = 10f
        leftAxis.addLimitLine(limitLine)

        // 右侧Y轴禁用
        chart.axisRight.isEnabled = false

        chart.invalidate() // 刷新生效
    }

    // 初始化一条空的数据线
    private fun initChartData() {
        // 创建一个空的数据集，命名为 "Heart Rate"
        val set = LineDataSet(null, "Heart Rate")

        // 配置线条样式
        set.mode = LineDataSet.Mode.CUBIC_BEZIER // 关键：设置成平滑曲线，不像折线那么生硬
        set.cubicIntensity = 0.2f
        set.setDrawFilled(true)  // 设置填充颜色
        set.setDrawCircles(false) // 不画数据点的小圆圈
        set.lineWidth = 2f        // 线条宽度
        set.color = ContextCompat.getColor(requireContext(), R.color.teal_200) // 线条颜色 (你可以换成你喜欢的绿色或蓝色)
        set.fillColor = ContextCompat.getColor(requireContext(), R.color.teal_200) // 填充颜色
        set.fillAlpha = 50 // 填充透明度
        set.setDrawValues(false) // 不在线上显示数值

        // 将数据集放入 LineData
        val data = LineData(set)
        binding.ecgChart.data = data
    }

    private fun startMonitoring() {
        handler.removeCallbacks(updateTask)
        handler.post(updateTask)
    }

    private fun fetchRealTimeData() {
        val request = mapOf("user_id" to "1")
        RetrofitClient.api.getLatestData(request).enqueue(object : Callback<BaseListResponse<Map<String, String>>> {
            override fun onResponse(
                call: Call<BaseListResponse<Map<String, String>>>,
                response: Response<BaseListResponse<Map<String, String>>>
            ) {
                if (response.isSuccessful && response.body()?.code == 200) {
                    val list = response.body()?.data
                    if (!list.isNullOrEmpty()) {
                        val item = list[0]
                        val bpmStr = item["bpm"] ?: "0"
                        val bpmVal = bpmStr.toFloatOrNull() ?: 0f

                        // 更新 UI 和图表
                        updateUI(bpmStr, bpmVal)
                    }
                }
            }

            override fun onFailure(call: Call<BaseListResponse<Map<String, String>>>, t: Throwable) {
                Log.e("MonitorFragment", "网络请求失败: ${t.message}")
                binding.tvStatus.text = "● 网络连接断开"
                binding.tvStatus.setTextColor(Color.GRAY)
            }
        })
    }

    // --- 🔥 核心：往图表里塞数据并让它滚动 ---
    private fun updateUI(bpmStr: String, bpmVal: Float) {
        // 1. 更新大数字显示
        binding.tvHeartRate.text = bpmStr

        // 2. 更新状态文字颜色
        if (bpmVal > 120) {
            binding.tvStatus.text = "● 心率过高警告!"
            binding.tvStatus.setTextColor(Color.RED)
            // 可以考虑在这里把图表线条颜色也改成红色
        } else {
            binding.tvStatus.text = "● 设备运行正常"
            binding.tvStatus.setTextColor(Color.parseColor("#4CAF50"))
        }

        // 3. 往图表里添加新数据点
        val data = binding.ecgChart.data
        if (data != null) {
            var set = data.getDataSetByIndex(0)
            if (set == null) {
                set = LineDataSet(null, "Heart Rate")
                data.addDataSet(set)
            }

            // 添加一个新的点 (X是递增的序号, Y是心率值)
            data.addEntry(Entry(chartXIndex++, bpmVal), 0)

            // 通知图表数据更新了
            data.notifyDataChanged()
            binding.ecgChart.notifyDataSetChanged()

            // 关键：实现“滚动”效果
            // 设置图表最多显示多少个点 (比如 30 个点)
            binding.ecgChart.setVisibleXRangeMaximum(30f)
            // 将视图移动到最新的点，实现向左滚动的效果
            binding.ecgChart.moveViewToX(data.entryCount.toFloat())
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        handler.removeCallbacks(updateTask)
        _binding = null
    }
}