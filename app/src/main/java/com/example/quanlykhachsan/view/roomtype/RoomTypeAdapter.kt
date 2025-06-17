package com.example.quanlykhachsan.view.roomtype

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.quanlykhachsan.data.local.entity.LoaiPhong
import com.example.quanlykhachsan.databinding.ItemRoomTypeBinding

class RoomTypeAdapter(
    private val onSelect: (LoaiPhong?) -> Unit
) : ListAdapter<LoaiPhong, RoomTypeAdapter.VH>(Diff) {

    private var selectedPos = RecyclerView.NO_POSITION

    inner class VH(private val b: ItemRoomTypeBinding) :
        RecyclerView.ViewHolder(b.root) {

        init {
            b.root.setOnClickListener {
                val prev = selectedPos
                val pos = absoluteAdapterPosition
                if (pos == RecyclerView.NO_POSITION) return@setOnClickListener

                if (pos == selectedPos) {
                    // Bỏ chọn
                    selectedPos = RecyclerView.NO_POSITION
                    notifyItemChanged(prev)
                    onSelect(null)
                } else {
                    // Chọn dòng mới
                    selectedPos = pos
                    notifyItemChanged(prev)
                    notifyItemChanged(selectedPos)
                    onSelect(getItem(selectedPos))
                }
            }
        }

        fun bind(item: LoaiPhong, isSelected: Boolean) = with(b) {
            root.isSelected = isSelected
            tvId.text = item.maLoaiPhong.toString()
            tvName.text = item.tenLoaiPhong
            tvPrice.text = item.gia.toString()
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
        VH(ItemRoomTypeBinding.inflate(LayoutInflater.from(parent.context), parent, false))

    override fun onBindViewHolder(holder: VH, position: Int) =
        holder.bind(getItem(position), position == selectedPos)

    companion object {
        private val Diff = object : DiffUtil.ItemCallback<LoaiPhong>() {
            override fun areItemsTheSame(o: LoaiPhong, n: LoaiPhong) =
                o.maLoaiPhong == n.maLoaiPhong

            override fun areContentsTheSame(o: LoaiPhong, n: LoaiPhong) = o == n
        }
    }
}