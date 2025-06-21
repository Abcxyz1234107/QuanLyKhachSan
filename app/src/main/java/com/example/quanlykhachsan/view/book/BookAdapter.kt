package com.example.quanlykhachsan.view.book

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.quanlykhachsan.databinding.ItemBookBinding
import com.example.quanlykhachsan.data.local.entity.DatPhong
import java.text.SimpleDateFormat
import java.util.*

class BookAdapter(private val onSelect: (DatPhong) -> Unit) : RecyclerView.Adapter<BookAdapter.VH>() {

    private val sdf = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
    private val items = mutableListOf<DatPhong>()
    private var selectedPos = RecyclerView.NO_POSITION

    fun submit(list: List<DatPhong>) {
        items.clear()
        items.addAll(list)
        notifyDataSetChanged()
    }

    class VH(val bd: ItemBookBinding) : RecyclerView.ViewHolder(bd.root)

    override fun onCreateViewHolder(p: ViewGroup, t: Int): VH =
        VH(ItemBookBinding.inflate(LayoutInflater.from(p.context), p, false))

    override fun getItemCount() = items.size

    override fun onBindViewHolder(h: VH, i: Int) {
        val it = items[i]
        h.bd.tvId.text          = it.maPhong.toString()
        h.bd.tvPhone.text       = it.soDienThoai
        h.bd.tvDateOut.text = it.ngayTraPhong?.let { d -> sdf.format(d) } ?: "-"

        /* hightlight */
        h.itemView.isSelected = i == selectedPos
        val item = items[i]
        h.itemView.setOnClickListener {
            val prev = selectedPos
            selectedPos = h.absoluteAdapterPosition
            notifyItemChanged(prev)
            notifyItemChanged(selectedPos)
            onSelect(item)
        }
    }
}
