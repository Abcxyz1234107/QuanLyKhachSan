package com.example.quanlykhachsan.view.book

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.quanlykhachsan.databinding.ItemBookBinding
import com.example.quanlykhachsan.data.local.entity.DatPhong
import java.text.SimpleDateFormat
import java.util.*

class BookAdapter(private val onItemClick: (DatPhong?) -> Unit) : RecyclerView.Adapter<BookAdapter.VH>() {

    private val sdf = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
    private val items = mutableListOf<DatPhong>()
    private var selectedPos: Int = RecyclerView.NO_POSITION

    fun submit(list: List<DatPhong>) {
        items.clear()
        items.addAll(list)
        notifyDataSetChanged()
    }

    inner class VH(private val bd: ItemBookBinding)
        : RecyclerView.ViewHolder(bd.root) {

        fun bind(item: DatPhong) = with(bd) {
            tvId.text    = item.maPhong.toString()
            tvPhone.text = item.soDienThoai
            tvStatus.text = item.tinhTrangDatPhong

            /* highlight */
            root.isSelected = absoluteAdapterPosition == selectedPos

            // Xử lý click
            root.setOnClickListener {
                selectedPos = if (selectedPos == absoluteAdapterPosition)
                    RecyclerView.NO_POSITION else absoluteAdapterPosition
                onItemClick(
                    if (selectedPos == RecyclerView.NO_POSITION) null else item
                )
                notifyDataSetChanged()
            }
        }
    }

    override fun onCreateViewHolder(p: ViewGroup, t: Int) = VH(ItemBookBinding.inflate(LayoutInflater.from(p.context), p, false))
    override fun onBindViewHolder(h: VH, i: Int) = h.bind(items[i])

    override fun getItemCount() = items.size

}
