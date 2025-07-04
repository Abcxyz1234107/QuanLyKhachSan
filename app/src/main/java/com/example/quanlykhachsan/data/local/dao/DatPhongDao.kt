package com.example.quanlykhachsan.data.local.dao

import androidx.lifecycle.LiveData
import androidx.room.*
import com.example.quanlykhachsan.data.local.entity.DatPhong

@Dao
interface DatPhongDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(datPhong: DatPhong): Long

    @Update
    suspend fun update(datPhong: DatPhong)

    @Delete
    suspend fun delete(datPhong: DatPhong)

    @Query("SELECT * FROM dat_phong ORDER BY ngayNhanPhong DESC")
    fun getAll(): LiveData<List<DatPhong>>

    @Query("SELECT * FROM dat_phong WHERE maDatPhong = :id")
    suspend fun getById(id: Int): DatPhong?

    @Query("SELECT * FROM dat_phong WHERE maPhong = :roomId")
    fun getByPhong(roomId: Int): LiveData<List<DatPhong>>

    @Query("SELECT * FROM dat_phong WHERE maPhong = :roomId")
    suspend fun getByPhongSync(roomId: Int): List<DatPhong>

    @Query("SELECT COUNT(*) FROM dat_phong WHERE maPhong = :maPhong")
    suspend fun countByRoomId(maPhong: Int): Int

    @Query("SELECT * FROM dat_phong")
    suspend fun getAllExport(): List<DatPhong>
}
