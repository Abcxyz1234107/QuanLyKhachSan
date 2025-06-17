package com.example.quanlykhachsan.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.quanlykhachsan.data.local.dao.NhanVienDao
import com.example.quanlykhachsan.data.local.entity.NhanVien
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class StaffViewModel @Inject constructor(nhanVienDao: NhanVienDao) : ViewModel() {
    val staffList: LiveData<List<NhanVien>> = nhanVienDao.getAll()

    private val _selectedNhanVien = MutableLiveData<NhanVien?>()
    val selectedNhanVien: LiveData<NhanVien?> = _selectedNhanVien

    fun setSelectedNhanVien(nv: NhanVien?) {
        _selectedNhanVien.value = nv
    }
}
