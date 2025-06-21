package com.example.quanlykhachsan.data.repository.phong

import com.example.quanlykhachsan.data.local.entity.Phong
import com.example.quanlykhachsan.data.local.model.PhongWithLoaiPhong
import kotlinx.coroutines.flow.Flow

interface PhongRepository {
    fun getRoomsWithType(): Flow<List<PhongWithLoaiPhong>>
    suspend fun getById(id: Int): Phong?
    suspend fun insert(phong: Phong): Long
    suspend fun update(phong: Phong)
    suspend fun delete(phong: Phong)
}
