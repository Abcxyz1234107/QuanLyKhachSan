package com.example.quanlykhachsan.data.repository.phong

import com.example.quanlykhachsan.data.local.dao.PhongDao
import com.example.quanlykhachsan.data.local.entity.Phong
import com.example.quanlykhachsan.data.local.model.PhongWithLoaiPhong
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PhongRepositoryImpl @Inject constructor(
    private val dao: PhongDao
) : PhongRepository {
    override fun getRoomsWithType(): Flow<List<PhongWithLoaiPhong>> = dao.getRoomsWithType()
    override suspend fun getById(id: Int) = dao.getById(id)
    override suspend fun insert(phong: Phong) = dao.insert(phong)
    override suspend fun update(phong: Phong) = dao.update(phong)
    override suspend fun delete(phong: Phong) = dao.delete(phong)
}
