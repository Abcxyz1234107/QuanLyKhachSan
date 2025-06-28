package com.example.quanlykhachsan.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.quanlykhachsan.data.local.entity.LoaiPhong
import com.example.quanlykhachsan.data.local.dao.LoaiPhongDao
import com.example.quanlykhachsan.data.local.dao.PhongDao
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class RoomTypeViewModel @Inject constructor(
    private val dao: LoaiPhongDao,
    private val phongDao: PhongDao
) : ViewModel() {

    /* ----------- LiveData chính ----------- */
    private val sourceList: LiveData<List<LoaiPhong>> = dao.getAll()
    val roomTypes = MediatorLiveData<List<LoaiPhong>>()

    /* ---------- Trạng thái filter ---------- */
    private val isFilterEnabled = MutableLiveData(false)
    private val minPrice        = MutableLiveData(0.0)
    private val maxPrice        = MutableLiveData(Double.MAX_VALUE)

    /* ----------- Message gửi UI ----------- */
    private val _message = MutableLiveData<String>()
    val message: LiveData<String> get() = _message

    /* ---------- Selection ---------- */
    internal var current: LoaiPhong? = null

    init {
        listOf(sourceList, isFilterEnabled, minPrice, maxPrice).forEach {
            roomTypes.addSource(it) { applyFilter() }
        }
        applyFilter()
    }

    /* --------- Các nút thao tác --------- */
    fun add(name: CharSequence?, price: Float) = viewModelScope.launch {
        if (name.isNullOrBlank() || price <= 0f) {
            _message.value = "Tên và giá phải hợp lệ"
            return@launch
        }
        dao.insert(LoaiPhong(tenLoaiPhong = name.toString(), gia = price.toDouble()))
        _message.value = "Thêm thành công"
        reset()
    }

    fun editCurrent(name: CharSequence?, price: Float) = viewModelScope.launch {
        val target = current
        if (target == null) {
            _message.value = "Chọn một dòng để sửa"
            return@launch
        }
        if (name.isNullOrBlank() || price <= 0f) {
            _message.value = "Tên và giá phải hợp lệ"
            return@launch
        }
        dao.update(target.copy(tenLoaiPhong = name.toString(), gia = price.toDouble()))
        _message.value = "Sửa thành công"
        reset()
    }

    fun deleteCurrent() = viewModelScope.launch {
        val target = current
        if (target == null) {
            _message.value = "Chọn một dòng để xoá"
            return@launch
        }

        val ref = phongDao.countByLoaiPhong(target.maLoaiPhong)
        if (ref > 0) {
            _message.value = "Không thể xoá: còn $ref phòng thuộc loại này"
            return@launch
        }

        dao.delete(target)
        _message.value = "Xoá thành công"
        reset()
    }

    /* --------- Selection & Filter --------- */
    fun onItemSelected(item: LoaiPhong) { current = item }
    fun reset() { current = null }

    fun setFilter(enabled: Boolean, min: Double, max: Double) {
        isFilterEnabled.value = enabled
        minPrice.value        = min
        maxPrice.value        = max
    }
    private fun applyFilter() {
        val raw  = sourceList.value ?: emptyList()
        val list = if (isFilterEnabled.value == true) {
            val mn = minPrice.value ?: 0.0
            val mx = maxPrice.value ?: Double.MAX_VALUE
            raw.filter { it.gia in mn..mx }
        } else raw
        roomTypes.value = list
    }
}