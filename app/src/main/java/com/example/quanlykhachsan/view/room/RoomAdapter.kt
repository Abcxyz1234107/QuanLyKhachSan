package com.example.quanlykhachsan.view.room

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.quanlykhachsan.R
import com.example.quanlykhachsan.databinding.ItemQlroomBinding

/**  Item hiển thị: chỉ Mã & Tên loại phòng  */
data class RoomItem(val id: Int, val typeName: String)

class RoomAdapter(
    private val onClick: (RoomItem?) -> Unit
) : ListAdapter<RoomItem, RoomAdapter.RoomVH>(DIFF) {

    private var selectedId: Int? = null

    inner class RoomVH(private val b: ItemQlroomBinding) :
        RecyclerView.ViewHolder(b.root) {
        fun bind(item: RoomItem) = with(b) {
            tvId.text = item.id.toString()
            tvName.text = item.typeName
            root.isSelected = item.id == selectedId // highlight nếu đang chọn

            root.setOnClickListener {
                val wasSelected = item.id == selectedId
                selectedId = if (wasSelected) null else item.id
                onClick(if (wasSelected) null else item)   // gửi null khi bỏ chọn
                notifyDataSetChanged()
            }
        }
    }

    fun clearSelection() {
        selectedId = null
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
        RoomVH(ItemQlroomBinding.inflate(
            LayoutInflater.from(parent.context), parent, false))

    override fun onBindViewHolder(holder: RoomVH, position: Int) =
        holder.bind(getItem(position))

    companion object {
        private val DIFF = object : DiffUtil.ItemCallback<RoomItem>() {
            override fun areItemsTheSame(o: RoomItem, n: RoomItem) = o.id == n.id
            override fun areContentsTheSame(o: RoomItem, n: RoomItem) = o == n
        }
    }
}
