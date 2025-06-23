package com.example.quanlykhachsan.data.local.dao

import androidx.lifecycle.LiveData
import androidx.room.*
import com.example.quanlykhachsan.data.local.entity.Phong
import com.example.quanlykhachsan.data.local.model.PhongWithLoaiPhong
import kotlinx.coroutines.flow.Flow

@Dao
interface PhongDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(phong: Phong): Long

    @Update
    suspend fun update(phong: Phong)

    @Delete
    suspend fun delete(phong: Phong)

    @Query("SELECT * FROM phong ORDER BY maPhong")
    fun getAll(): LiveData<List<Phong>>

    @Query("SELECT * FROM phong WHERE maPhong = :id")
    suspend fun getById(id: Int): Phong?

    @Query("SELECT * FROM phong WHERE trangThaiPhong = :status")
    fun getByTrangThai(status: String): LiveData<List<Phong>>

    @Query("SELECT * FROM phong ORDER BY maPhong")
    suspend fun getAllSync(): List<Phong>

    // Lấy mã phòng + tên loại phòng
    @Query("""
    SELECT  p.maPhong              AS maPhong,
            lp.tenLoaiPhong        AS tenLoaiPhong
    FROM    phong p
            JOIN loai_phong lp ON lp.maLoaiPhong = p.maLoaiPhong
    ORDER BY p.maPhong
""")
    fun getRoomsWithType(): Flow<List<PhongWithLoaiPhong>>

    @Query("SELECT COUNT(*) FROM phong WHERE maLoaiPhong = :maLoaiPhong")
    suspend fun countByLoaiPhong(maLoaiPhong: Int): Int
}
