package com.example.quanlykhachsan.viewmodel

import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.quanlykhachsan.data.local.dao.NhanVienDao
import com.example.quanlykhachsan.data.local.entity.NhanVien
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.*
import java.util.concurrent.atomic.AtomicBoolean
import androidx.lifecycle.Observer

class SingleLiveEvent<T> : MutableLiveData<T>() {
    private val pending = AtomicBoolean(false)

    override fun observe(owner: LifecycleOwner, observer: Observer<in T>) {
        super.observe(owner, Observer { t ->
            if (pending.compareAndSet(true, false)) {
                observer.onChanged(t)
            }
        })
    }
    override fun setValue(t: T?) {
        pending.set(true)
        super.setValue(t)
    }
}

@HiltViewModel
class StaffViewModel @Inject constructor(
    private val dao: NhanVienDao
) : ViewModel() {

    val staffList: LiveData<List<NhanVien>> = dao.getAll()

    private val _selectedNhanVien = MutableLiveData<NhanVien?>()
    val selectedNhanVien: LiveData<NhanVien?> = _selectedNhanVien

    /** Sự kiện toast 1-lần cho Fragment */
    val toastEvent = SingleLiveEvent<String>()

    fun setSelectedNhanVien(nv: NhanVien?) { _selectedNhanVien.value = nv }

    /* ----------  THÊM  ---------- */
    fun addNhanVien(name: String, phone: String) {
        if (name.isBlank() || phone.isBlank()) {
            toastEvent.value = "Vui lòng nhập đủ Họ tên & SĐT!"
            return
        }
        viewModelScope.launch(Dispatchers.IO) {
            // Kiểm tra trùng tên
            if (dao.getAll().value?.any { it.tenNhanVien == name } == true) {
                toastEvent.postValue("Tên nhân viên đã tồn tại!")
                return@launch
            }

            val maxId = dao.getMaxMaNhanVien() ?: 0
            val newId = maxId + 1
            val nv = NhanVien(
                maNhanVien  = newId,
                tenNhanVien = name,
                soDienThoai = phone,
                chucVu = "",
                luongCoBan = 0.0,
                lichLamViec = "",
                ngayBatDauLamViec = Date()
            )

            dao.insert(nv)
            reorderNhanVien()
            toastEvent.postValue("Đã thêm nhân viên [$name]")
        }
    }

    /* ----------  SỬA  ---------- */
    fun updateNhanVien(name: String, phone: String) {
        val current = _selectedNhanVien.value
        if (current == null) {
            toastEvent.value = "Chọn 1 nhân viên để sửa!"
            return
        }
        if (name.isBlank() || phone.isBlank()) {
            toastEvent.value = "Họ tên & SĐT không được để trống!"
            return
        }
        viewModelScope.launch(Dispatchers.IO) {
            dao.update(current.copy(tenNhanVien = name, soDienThoai = phone))
            reorderNhanVien()
            toastEvent.postValue("Đã cập nhật thông tin nhân viên [$name]")
        }
    }

    /* ----------  XÓA  ---------- */
    fun deleteNhanVien() {
        val current = _selectedNhanVien.value ?: run {
            toastEvent.value = "Chọn nhân viên cần xóa!"
            return
        }
        viewModelScope.launch(Dispatchers.IO) {
            dao.delete(current)
            dao.shiftIdsAfter(current.maNhanVien)
            reorderNhanVien()
            _selectedNhanVien.postValue(null) // Clear selection & thông báo
            toastEvent.postValue("Đã xóa nhân viên [${current.maNhanVien}]")
        }
    }

    private suspend fun reorderNhanVien() {
        val sorted = dao.getAllOnce()
        sorted.forEachIndexed { index, nv ->
            dao.update(nv.copy(maNhanVien = index + 1))
        }
    }
}