package com.example.quanlykhachsan.view.payment

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.quanlykhachsan.databinding.ItemPaymentBinding
import com.example.quanlykhachsan.viewmodel.PaymentViewModel.PaymentItem
import java.util.*

class PaymentAdapter :
    ListAdapter<PaymentItem, PaymentAdapter.PaymentVH>(diff) {

    /** item đang chọn */
    private var selectedPos = RecyclerView.NO_POSITION
    var onItemClick: ((PaymentItem?) -> Unit)? = null

    inner class PaymentVH(private val binding: ItemPaymentBinding)
        : RecyclerView.ViewHolder(binding.root) {
        fun bind(item: PaymentItem, isSelected: Boolean) = with(binding) {
            tvId.text            = item.roomId.toString()
            tvTotalPayment.text  = "%.0f".format(item.total)
            tvPaymentType.text   = item.payType
            /* Highlight nhờ bg_room_type_item */
            root.isSelected = isSelected   // bg selector đổi màu khi selected=true
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PaymentVH {
        val binding = ItemPaymentBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        val vh = PaymentVH(binding)
        vh.itemView.setOnClickListener {
            val pos = vh.bindingAdapterPosition
            if (pos == RecyclerView.NO_POSITION) return@setOnClickListener

            val oldPos = selectedPos
            selectedPos = if (pos == selectedPos) RecyclerView.NO_POSITION else pos

            if (oldPos != RecyclerView.NO_POSITION) notifyItemChanged(oldPos)
            if (selectedPos != RecyclerView.NO_POSITION) notifyItemChanged(selectedPos)

            val item = if (selectedPos != RecyclerView.NO_POSITION)
                getItem(selectedPos) else null
            onItemClick?.invoke(item)
        }
        return vh
    }

    override fun onBindViewHolder(holder: PaymentVH, position: Int) =
        holder.bind(getItem(position), position == selectedPos)

    companion object {
        private val diff = object : DiffUtil.ItemCallback<PaymentItem>() {
            override fun areItemsTheSame(o: PaymentItem, n: PaymentItem) =
                o.roomId == n.roomId && o.payType == n.payType
            override fun areContentsTheSame(o: PaymentItem, n: PaymentItem) = o == n
        }
    }
}