package com.example.quanlykhachsan.data.local.dao

import androidx.lifecycle.LiveData
import androidx.room.*
import com.example.quanlykhachsan.data.local.entity.TraPhong

@Dao
interface TraPhongDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(traPhong: TraPhong): Long

    @Update
    suspend fun update(traPhong: TraPhong)

    @Delete
    suspend fun delete(traPhong: TraPhong)

    @Query("SELECT * FROM tra_phong ORDER BY ngayThanhToan DESC")
    fun getAll(): LiveData<List<TraPhong>>

    @Query("SELECT * FROM tra_phong WHERE maTraPhong = :id")
    suspend fun getById(id: Int): TraPhong?

    @Query("SELECT * FROM tra_phong WHERE maDatPhong = :datPhongId")
    fun getByDatPhong(datPhongId: Int): LiveData<List<TraPhong>>
}
