package com.example.quanlykhachsan.view.room

import android.content.Context
import android.widget.ArrayAdapter
import android.widget.Filter

/**
 * ArrayAdapter tự động cập nhật data,
 * và KHÔNG để filter nội bộ AutoCompleteTextView tự can thiệp.
 */
class SuggestionsAdapter(
    context: Context,
    resource: Int,
    private val items: MutableList<String>
) : ArrayAdapter<String>(context, resource, items) {

    override fun getFilter(): Filter = object : Filter() {
        override fun performFiltering(constraint: CharSequence?): FilterResults {
            // Trả luôn current items, không lọc thêm
            return FilterResults().also {
                it.values = items
                it.count = items.size
            }
        }

        override fun publishResults(constraint: CharSequence?, results: FilterResults?) {
            // items đã được update thủ công bên ngoài rồi, chỉ cần báo UI
            notifyDataSetChanged()
        }
    }

    /** Gọi từ ngoài để cập nhật list mới */
    fun updateData(newItems: List<String>) {
        items.clear()
        items.addAll(newItems)
        notifyDataSetChanged()
    }
}
