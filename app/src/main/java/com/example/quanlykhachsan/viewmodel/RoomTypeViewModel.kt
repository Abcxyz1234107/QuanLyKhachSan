package com.example.quanlykhachsan.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.quanlykhachsan.data.local.entity.LoaiPhong
import com.example.quanlykhachsan.data.local.dao.LoaiPhongDao
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class RoomTypeViewModel @Inject constructor(
    private val dao: LoaiPhongDao
) : ViewModel() {

    // LiveData tự động emit khi DB thay đổi
    val roomTypes: LiveData<List<LoaiPhong>> = dao.getAll()

    private var current: LoaiPhong? = null

    // Thêm loại phòng mới
    fun add(name: CharSequence?, price: Float) = viewModelScope.launch {
        val entity = LoaiPhong(
            //maLoaiPhong = maLoaiPhong max HIỆN TẠI + 1
            tenLoaiPhong = name?.toString().orEmpty(),
            gia = price.toDouble()
        )
        dao.insert(entity)
    }

    // Sửa loại phòng đang chọn
    fun editCurrent(name: CharSequence?, price: Float) = viewModelScope.launch {
        current?.let {
            val updated = it.copy(
                tenLoaiPhong = name?.toString().orEmpty(),
                gia = price.toDouble()
            )
            dao.update(updated)
        }
    }

    // Xóa loại phòng đang chọn
    fun deleteCurrent() = viewModelScope.launch {
        current?.let { dao.delete(it) }
    }

    // Gán loại phòng khi click item
    fun onItemSelected(rt: LoaiPhong) {
        current = rt
    }

    // Reset selection
    fun reset() {
        current = null
    }

    // Bật/tắt filter (TODO)
    fun setFilterEnabled(enabled: Boolean) {
        // TODO: triển khai filter nếu cần
    }
}