package com.example.quanlykhachsan.data.repository.phong

import com.example.quanlykhachsan.data.local.model.PhongWithLoaiPhong
import kotlinx.coroutines.flow.Flow

interface PhongRepository {
    fun getRoomsWithType(): Flow<List<PhongWithLoaiPhong>>
}
