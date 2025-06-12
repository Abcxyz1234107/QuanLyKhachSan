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

    @Query("SELECT * FROM dat_phong ORDER BY ngayDatPhong DESC")
    fun getAll(): LiveData<List<DatPhong>>

    @Query("SELECT * FROM dat_phong WHERE maDatPhong = :id")
    suspend fun getById(id: Int): DatPhong?

    @Query("SELECT * FROM dat_phong WHERE maPhong = :roomId")
    fun getByPhong(roomId: Int): LiveData<List<DatPhong>>
}
