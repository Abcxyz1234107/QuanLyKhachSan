package com.example.quanlykhachsan.view.dashboard

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.quanlykhachsan.databinding.ItemPhongBinding
import com.example.quanlykhachsan.viewmodel.PhongUI

class PhongAdapter :
    ListAdapter<PhongUI, PhongAdapter.VH>(object : DiffUtil.ItemCallback<PhongUI>() {
        override fun areItemsTheSame(o: PhongUI, n: PhongUI) = o.tenPhong == n.tenPhong
        override fun areContentsTheSame(o: PhongUI, n: PhongUI) = o == n
    }) {

    inner class VH(val binding: ItemPhongBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH =
        VH(ItemPhongBinding.inflate(LayoutInflater.from(parent.context), parent, false))

    override fun onBindViewHolder(holder: VH, position: Int) {
        getItem(position).let { ui ->
            holder.binding.tvTenPhong.text   = ui.tenPhong
            holder.binding.tvLoaiPhong.text  = ui.tenLoai ?: "Không rõ"
            holder.binding.tvTrangThai.text  = ui.trangThai
        }
    }
}
