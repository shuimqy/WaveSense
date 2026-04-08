package com.llglh.wavesense.app.ui.fragment

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.github.mikephil.charting.components.LimitLine
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.llglh.wavesense.R
import com.llglh.wavesense.app.service.VitalsMonitorService
import com.llglh.wavesense.databinding.FragmentMonitorBinding

class MonitorFragment : Fragment() {

    private var _binding: FragmentMonitorBinding? = null
    private val binding get() = _binding!!
    private var chartXIndex = 0f

    // 接收来自 Service 的实时数据广播
    private val vitalsReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            val bpmStr = intent?.getStringExtra("bpm") ?: "0"
            val bpmVal = intent?.getFloatExtra("bpmVal", 0f) ?: 0f
            updateUI(bpmStr, bpmVal)
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentMonitorBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupChartStyle()
        initChartData()

        // 1. 启动后台守护服务 (核心)
        startMonitorService()

        // 2. 注册广播监听器
        val filter = IntentFilter("VITALS_UPDATE_ACTION")
        ContextCompat.registerReceiver(
            requireContext(),
            vitalsReceiver,
            filter,
            ContextCompat.RECEIVER_NOT_EXPORTED
        )
    }

    private fun startMonitorService() {
        val serviceIntent = Intent(requireContext(), VitalsMonitorService::class.java)
        ContextCompat.startForegroundService(requireContext(), serviceIntent)
    }

    private fun setupChartStyle() {
        val chart = binding.ecgChart
        chart.description.isEnabled = false
        chart.legend.isEnabled = false
        chart.setTouchEnabled(false)
        chart.setViewPortOffsets(0f, 0f, 0f, 0f)
        chart.setBackgroundColor(Color.parseColor("#FAFAFA"))

        val xAxis = chart.xAxis
        xAxis.position = XAxis.XAxisPosition.BOTTOM_INSIDE
        xAxis.setDrawGridLines(false)
        xAxis.setDrawLabels(false)

        val leftAxis = chart.axisLeft
        leftAxis.setDrawGridLines(false)
        leftAxis.textColor = Color.GRAY
        leftAxis.axisMinimum = 40f
        leftAxis.axisMaximum = 150f
        leftAxis.setDrawZeroLine(false)

        val limitLine = LimitLine(120f, "高心率警戒")
        limitLine.lineWidth = 1f
        limitLine.lineColor = Color.parseColor("#FF5252")
        limitLine.textColor = Color.parseColor("#FF5252")
        limitLine.textSize = 10f
        leftAxis.addLimitLine(limitLine)
        chart.axisRight.isEnabled = false
        chart.invalidate()
    }

    private fun initChartData() {
        val set = LineDataSet(null, "Heart Rate")
        set.mode = LineDataSet.Mode.CUBIC_BEZIER
        set.cubicIntensity = 0.2f
        set.setDrawFilled(true)
        set.setDrawCircles(false)
        set.lineWidth = 2f
        set.color = ContextCompat.getColor(requireContext(), R.color.teal_200)
        set.fillColor = ContextCompat.getColor(requireContext(), R.color.teal_200)
        set.fillAlpha = 50
        set.setDrawValues(false)
        binding.ecgChart.data = LineData(set)
    }

    private fun updateUI(bpmStr: String, bpmVal: Float) {
        binding.tvHeartRate.text = bpmStr

        if (bpmVal > 120) {
            binding.tvStatus.text = "● 设备运行正常"
            binding.tvStatus.setTextColor(ContextCompat.getColor(requireContext(), R.color.status_normal))
        } else {
            binding.tvStatus.text = "● 设备运行正常"
            binding.tvStatus.setTextColor(ContextCompat.getColor(requireContext(), R.color.status_normal))
        }

        val data = binding.ecgChart.data
        if (data != null) {
            var set = data.getDataSetByIndex(0)
            if (set == null) {
                set = LineDataSet(null, "Heart Rate")
                data.addDataSet(set)
            }
            data.addEntry(Entry(chartXIndex++, bpmVal), 0)
            data.notifyDataChanged()
            binding.ecgChart.notifyDataSetChanged()
            binding.ecgChart.setVisibleXRangeMaximum(30f)
            binding.ecgChart.moveViewToX(data.entryCount.toFloat())
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        // 离开页面时解绑广播，但后台服务依然在跑！
        requireActivity().unregisterReceiver(vitalsReceiver)
        _binding = null
    }
}