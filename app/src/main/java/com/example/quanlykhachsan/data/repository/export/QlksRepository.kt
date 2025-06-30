package com.example.quanlykhachsan.data.repository.export

import com.example.quanlykhachsan.data.local.dao.DatPhongDao
import com.example.quanlykhachsan.data.local.dao.LoaiPhongDao
import com.example.quanlykhachsan.data.local.dao.NhanVienDao
import com.example.quanlykhachsan.data.local.dao.PhongDao
import com.example.quanlykhachsan.data.local.dao.TraPhongDao
import dagger.hilt.android.scopes.ViewModelScoped
import jakarta.inject.Inject

@ViewModelScoped
class QlksRepository @Inject constructor(
    private val datPhongDao: DatPhongDao,
    private val traPhongDao: TraPhongDao,
    private val phongDao: PhongDao,
    private val loaiPhongDao: LoaiPhongDao,
    private val nhanVienDao: NhanVienDao
) {
    suspend fun allDatPhong()  = datPhongDao.getAllExport()
    suspend fun allTraPhong()  = traPhongDao.getAllExport()
    suspend fun allPhong()     = phongDao.getAllExport()
    suspend fun allLoaiPhong() = loaiPhongDao.getAllExport()
    suspend fun allNhanVien()  = nhanVienDao.getAllExport()
}
