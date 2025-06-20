package com.example.quanlykhachsan.data.repository.phong

import com.example.quanlykhachsan.data.local.dao.PhongDao
import com.example.quanlykhachsan.data.local.model.PhongWithLoaiPhong
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PhongRepositoryImpl @Inject constructor(
    private val dao: PhongDao
) : PhongRepository {
    override fun getRoomsWithType(): Flow<List<PhongWithLoaiPhong>> =
        dao.getRoomsWithType()
}
