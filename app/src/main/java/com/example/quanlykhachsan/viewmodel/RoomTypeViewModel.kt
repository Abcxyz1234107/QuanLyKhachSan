package com.example.quanlykhachsan.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.quanlykhachsan.data.local.entity.LoaiPhong
import kotlinx.coroutines.launch

class RoomTypeViewModel : ViewModel() {

    private val _roomTypes = MutableLiveData<List<LoaiPhong>>(emptyList())
    val roomTypes: LiveData<List<LoaiPhong>> get() = _roomTypes

    private var current: LoaiPhong? = null
    private var filterEnabled = false

    /* --------- Các hàm thao tác ---------- */
    fun add(name: CharSequence?, price: Float) = viewModelScope.launch {
        // TODO: thêm vào Room DB
    }

    fun editCurrent(name: CharSequence?, price: Float) = viewModelScope.launch {

    }

    fun deleteCurrent() = viewModelScope.launch {

    }

    fun onItemSelected(rt: LoaiPhong) { current = rt }

    fun reset() {
        current = null
    }

    fun setFilterEnabled(enabled: Boolean) {
        filterEnabled = enabled
        // TODO: lọc danh sách
    }
}
