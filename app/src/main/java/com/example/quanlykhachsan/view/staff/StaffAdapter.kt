package com.example.quanlykhachsan.view.staff

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.quanlykhachsan.R
import com.example.quanlykhachsan.data.local.entity.NhanVien
import com.example.quanlykhachsan.databinding.ItemStaffBinding

class StaffAdapter(private val onItemClick: (NhanVien) -> Unit) : ListAdapter<NhanVien, StaffAdapter.StaffVH>(Diff) {

    private var selectedId: Int? = null

    inner class StaffVH(private val binding: ItemStaffBinding)
        : RecyclerView.ViewHolder(binding.root) {

        fun bind(item: NhanVien) = with(binding) {
            tvMaNhanVien.text  = item.maNhanVien.toString()
            tvTenNhanVien.text = item.tenNhanVien
            tvSoDienThoai.text = item.soDienThoai

            // Highlight nếu được chọn
            root.isSelected = item.maNhanVien == selectedId

            // Xử lý click
            root.setOnClickListener {
                if (selectedId == item.maNhanVien) {
                    selectedId = null // bỏ chọn
                } else {
                    selectedId = item.maNhanVien
                }
                onItemClick(item)
                notifyDataSetChanged()
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) = StaffVH(ItemStaffBinding.inflate(LayoutInflater.from(parent.context), parent, false))
    override fun onBindViewHolder(holder: StaffVH, position: Int) = holder.bind(getItem(position))

    private object Diff : DiffUtil.ItemCallback<NhanVien>() {
        override fun areItemsTheSame(o: NhanVien, n: NhanVien) = o.maNhanVien == n.maNhanVien
        override fun areContentsTheSame(o: NhanVien, n: NhanVien) = o == n
    }
}
