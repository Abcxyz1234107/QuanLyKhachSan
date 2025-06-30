package com.example.quanlykhachsan.data.local.dao

import androidx.lifecycle.LiveData
import androidx.room.*
import com.example.quanlykhachsan.data.local.entity.DatPhong
import com.example.quanlykhachsan.data.local.entity.LoaiPhong
import kotlinx.coroutines.flow.Flow

@Dao
interface LoaiPhongDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(loaiPhong: LoaiPhong): Long

    @Update
    suspend fun update(loaiPhong: LoaiPhong)

    @Delete
    suspend fun delete(loaiPhong: LoaiPhong)

    @Query("SELECT * FROM loai_phong ORDER BY tenLoaiPhong")
    fun getAll(): LiveData<List<LoaiPhong>>

    @Query("SELECT tenLoaiPhong FROM loai_phong ORDER BY tenLoaiPhong")
    fun getAllNames(): Flow<List<String>>

    @Query("SELECT * FROM loai_phong WHERE maLoaiPhong = :id")
    suspend fun getById(id: Int): LoaiPhong?

    @Query("SELECT * FROM loai_phong ORDER BY maLoaiPhong")
    suspend fun getAllSync(): List<LoaiPhong>

    @Query("SELECT COUNT(DISTINCT maLoaiPhong) FROM loai_phong")
    suspend fun countDistinct(): Int

    @Query("SELECT * FROM loai_phong")
    suspend fun getAllExport(): List<LoaiPhong>
}
