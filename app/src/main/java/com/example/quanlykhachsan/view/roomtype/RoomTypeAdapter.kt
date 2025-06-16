package com.example.quanlykhachsan.view.roomtype

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.quanlykhachsan.databinding.ItemRoomTypeBinding
import com.example.quanlykhachsan.data.local.entity.LoaiPhong

class RoomTypeAdapter(
    private val onClick: (LoaiPhong) -> Unit
) : ListAdapter<LoaiPhong, RoomTypeAdapter.VH>(Diff) {

    companion object {
        private val Diff = object : DiffUtil.ItemCallback<LoaiPhong>() {
            override fun areItemsTheSame(o: LoaiPhong, n: LoaiPhong) = o.maLoaiPhong == n.maLoaiPhong
            override fun areContentsTheSame(o: LoaiPhong, n: LoaiPhong) = o == n
        }
    }

    inner class VH(private val b: ItemRoomTypeBinding) : RecyclerView.ViewHolder(b.root) {
        fun bind(rt: LoaiPhong) = with(b) {
            tvId.text    = rt.maLoaiPhong.toString()
            tvName.text = rt.tenLoaiPhong
            tvPrice.text = rt.gia.toString()
            root.setOnClickListener { onClick(rt) }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
        VH(ItemRoomTypeBinding.inflate(LayoutInflater.from(parent.context), parent, false))

    override fun onBindViewHolder(holder: VH, position: Int) =
        holder.bind(getItem(position))
}
