package com.llglh.wavesense.app.ui.adapter

import android.content.Intent
import android.graphics.Color
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.llglh.wavesense.databinding.ItemAlarmBinding
import com.llglh.wavesense.app.network.Alarm
import com.llglh.wavesense.app.ui.activity.AlarmHistoryDetailActivity

class AlarmAdapter(private var list: List<Alarm>) : RecyclerView.Adapter<AlarmAdapter.ViewHolder>() {

    class ViewHolder(val binding: ItemAlarmBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemAlarmBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = list[position]

        holder.binding.apply {
            tvDesc.text = item.description
            tvTime.text = item.time

            // 根据类型显示不同颜色或标题
            val displayType = if (item.type == "fall") {
                tvTitle.text = "⚠️ 跌倒报警"
                tvTitle.setTextColor(Color.RED)
                "跌倒报警"
            } else {
                tvTitle.text = "💓 体征异常"
                tvTitle.setTextColor(Color.parseColor("#FF9800")) // 橙色
                "体征异常"
            }

            // 点击事件：跳转到详情页
            root.setOnClickListener {
                val context = it.context
                val intent = Intent(context, AlarmHistoryDetailActivity::class.java).apply {
                    putExtra("EXTRA_TYPE", displayType)
                    putExtra("EXTRA_DESC", item.description)
                    putExtra("EXTRA_TIME", item.time)
                    putExtra("EXTRA_LEVEL", item.level)
                }
                context.startActivity(intent)
            }
        }
    }

    override fun getItemCount() = list.size

    fun updateList(newList: List<Alarm>) {
        list = newList
        notifyDataSetChanged()
    }
}