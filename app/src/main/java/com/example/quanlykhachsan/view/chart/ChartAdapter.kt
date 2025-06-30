package com.example.quanlykhachsan.view.chart

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.quanlykhachsan.databinding.ItemRoomRevenueBinding
import com.example.quanlykhachsan.data.local.model.RoomTypeRevenue
import java.text.NumberFormat
import java.util.Locale

class ChartAdapter(
    private var items: List<RoomTypeRevenue> = emptyList()
) : RecyclerView.Adapter<ChartAdapter.VH>() {

    private val vnCur = NumberFormat.getCurrencyInstance(Locale("vi", "VN"))

    inner class VH(val bd: ItemRoomRevenueBinding) : RecyclerView.ViewHolder(bd.root)

    override fun onCreateViewHolder(p: ViewGroup, v: Int): VH =
        VH(ItemRoomRevenueBinding.inflate(LayoutInflater.from(p.context), p, false))

    override fun getItemCount() = items.size

    override fun onBindViewHolder(h: VH, i: Int) = with(h.bd) {
        tvRoomType.text = items[i].tenLoaiPhong
        tvRevenue.text  = vnCur.format(items[i].total)
    }

    fun submit(newItems: List<RoomTypeRevenue>) {
        items = newItems
        notifyDataSetChanged()
    }
}
