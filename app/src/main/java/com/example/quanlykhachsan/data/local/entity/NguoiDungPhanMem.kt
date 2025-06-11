package com.example.quanlykhachsan.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "nguoi_dung_phan_mem")
data class NguoiDungPhanMem(
    @PrimaryKey(autoGenerate = true)
    val maNguoiDung: Int = 0,
    val tenTaiKhoan: String,
    val matKhau: String,
    val tenNguoiDung: String,
    val email: String,
    val soDienThoai: String
)
