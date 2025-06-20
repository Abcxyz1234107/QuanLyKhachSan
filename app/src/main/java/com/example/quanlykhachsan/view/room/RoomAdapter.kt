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
    private val onClick: (RoomItem) -> Unit
) : ListAdapter<RoomItem, RoomAdapter.RoomVH>(DIFF) {

    private var selectedId: Int? = null

    inner class RoomVH(private val b: ItemQlroomBinding) :
        RecyclerView.ViewHolder(b.root) {
        fun bind(item: RoomItem) = with(b) {
            tvId.text = item.id.toString()
            tvName.text = item.typeName

            // highlight nếu đang chọn
            root.isSelected = item.id == selectedId

            root.setOnClickListener {
                selectedId = if (selectedId == item.id) null else item.id
                onClick(item)
                notifyDataSetChanged()
            }
        }
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
