package com.example.quanlykhachsan.data.local.dao

import androidx.lifecycle.LiveData
import androidx.room.*
import com.example.quanlykhachsan.data.local.entity.Phong

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
}
