package com.example.quanlykhachsan.data.repository.loaiphong

import androidx.lifecycle.LiveData
import com.example.quanlykhachsan.data.local.entity.LoaiPhong

interface LoaiPhongRepository {
    fun getAll(): LiveData<List<LoaiPhong>>
    suspend fun insert(loaiPhong: LoaiPhong): Long
    suspend fun update(loaiPhong: LoaiPhong)
    suspend fun delete(loaiPhong: LoaiPhong)
}