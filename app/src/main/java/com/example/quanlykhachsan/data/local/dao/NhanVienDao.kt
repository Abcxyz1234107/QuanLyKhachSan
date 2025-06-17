package com.example.quanlykhachsan.data.local.dao

import androidx.lifecycle.LiveData
import androidx.room.*
import com.example.quanlykhachsan.data.local.entity.NhanVien

@Dao
interface NhanVienDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(nhanVien: NhanVien): Long

    @Update
    suspend fun update(nhanVien: NhanVien)

    @Delete
    suspend fun delete(nhanVien: NhanVien)

    @Query("SELECT * FROM nhan_vien ORDER BY tenNhanVien")
    fun getAll(): LiveData<List<NhanVien>>

    @Query("SELECT * FROM nhan_vien WHERE maNhanVien = :id")
    suspend fun getById(id: Int): NhanVien?

    @Query("SELECT COUNT(*) FROM nhan_vien") suspend fun count(): Int
}