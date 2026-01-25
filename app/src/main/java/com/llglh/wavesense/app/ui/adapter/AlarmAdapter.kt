package com.llglh.wavesense.app.ui.adapter

import android.graphics.Color
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.llglh.wavesense.databinding.ItemAlarmBinding
import com.llglh.wavesense.app.network.Alarm

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
            if (item.type == "fall") {
                tvTitle.text = "⚠️ 跌倒报警"
                tvTitle.setTextColor(Color.RED)
                // ivIcon.setImageResource(R.drawable.ic_fall) // 如果你有图标的话
            } else {
                tvTitle.text = "💓 体征异常"
                tvTitle.setTextColor(Color.parseColor("#FF9800")) // 橙色
            }
        }
    }

    override fun getItemCount() = list.size

    fun updateList(newList: List<Alarm>) {
        list = newList
        notifyDataSetChanged()
    }
}