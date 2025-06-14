package com.example.quanlykhachsan.data.local.dao

import androidx.lifecycle.LiveData
import androidx.room.*
import com.example.quanlykhachsan.data.local.entity.NguoiDungPhanMem

@Dao
interface NguoiDungPhanMemDao {
    @Insert
    suspend fun insert(user: NguoiDungPhanMem): Long

    @Update
    suspend fun update(user: NguoiDungPhanMem)

    @Delete
    suspend fun delete(user: NguoiDungPhanMem)

    @Query("SELECT * FROM nguoi_dung_phan_mem")
    fun getAll(): LiveData<List<NguoiDungPhanMem>>

    @Query("SELECT * FROM nguoi_dung_phan_mem WHERE maNguoiDung = :id")
    suspend fun getById(id: Int): NguoiDungPhanMem?

    @Query("SELECT COUNT(*) FROM nguoi_dung_phan_mem " +
                "WHERE tenTaiKhoan = :username AND matKhau = :password"
    )
    suspend fun countUser(username: String, password: String): Int

}