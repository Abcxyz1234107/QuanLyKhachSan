package com.example.quanlykhachsan.data.repository.loaiphong

import com.example.quanlykhachsan.data.local.dao.LoaiPhongDao
import com.example.quanlykhachsan.data.local.entity.LoaiPhong
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class LoaiPhongRepositoryImpl @Inject constructor(
    private val dao: LoaiPhongDao
) : LoaiPhongRepository {

    override fun getAll() = dao.getAll()

    override suspend fun insert(loaiPhong: LoaiPhong) =
        withContext(Dispatchers.IO) { dao.insert(loaiPhong) }

    override suspend fun update(loaiPhong: LoaiPhong) =
        withContext(Dispatchers.IO) { dao.update(loaiPhong) }

    override suspend fun delete(loaiPhong: LoaiPhong) =
        withContext(Dispatchers.IO) { dao.delete(loaiPhong) }
}
