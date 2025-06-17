package com.example.quanlykhachsan.view.dashboard

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.quanlykhachsan.databinding.ItemRoomBinding
import com.example.quanlykhachsan.viewmodel.PhongUI
import android.graphics.Color
import androidx.core.content.ContextCompat
import com.example.quanlykhachsan.R

class PhongAdapter :
    ListAdapter<PhongUI, PhongAdapter.VH>(object : DiffUtil.ItemCallback<PhongUI>() {
        override fun areItemsTheSame(o: PhongUI, n: PhongUI) = o.tenPhong == n.tenPhong
        override fun areContentsTheSame(o: PhongUI, n: PhongUI) = o == n
    }) {

    inner class VH(val binding: ItemRoomBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH =
        VH(ItemRoomBinding.inflate(LayoutInflater.from(parent.context), parent, false))

    override fun onBindViewHolder(holder: VH, position: Int) {
        val ui = getItem(position)
        with(holder.binding) {
            tvTenPhong.text  = ui.tenPhong
            tvLoaiPhong.text = ui.tenLoai ?: "Không rõ"

            when (ui.trangThai) {
                "Đã đặt" -> {
                    // màu đỏ, icon đặt
                    cardRoom.setCardBackgroundColor(ContextCompat.getColor(root.context, R.color.status_reserved))
                    ivTrangThai.setImageResource(R.drawable.ic_room_reserved)
                    tvTrangThai.text = "Đã đặt"
                }
                "Trống" -> {
                    // màu xanh, icon trống
                    cardRoom.setCardBackgroundColor(ContextCompat.getColor(root.context, R.color.status_empty))
                    ivTrangThai.setImageResource(R.drawable.ic_room_empty)
                    tvTrangThai.text = "Trống"
                }
                "Đang sử dụng" -> {
                    // màu xám, icon sử dụng
                    cardRoom.setCardBackgroundColor(ContextCompat.getColor(root.context, R.color.status_in_use))
                    ivTrangThai.setImageResource(R.drawable.ic_room_inuse)
                    tvTrangThai.text = "Đang sử dụng"
                }
                else -> {
                    // fallback
                    cardRoom.setCardBackgroundColor(Color.LTGRAY)
                    ivTrangThai.setImageResource(R.drawable.ic_room_empty)
                    tvTrangThai.text = ui.trangThai
                }
            }
        }
    }
}
