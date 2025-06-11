package com.example.quanlykhachsan.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.Date

@Entity(tableName = "nhan_vien")
data class NhanVien(
    @PrimaryKey(autoGenerate = true)
    val maNhanVien: Int = 0,
    val tenNhanVien: String,
    val chucVu: String,
    val luongCoBan: Double,
    val ngayBatDauLamViec: Date,
    val lichLamViec: String,
    val soDienThoai: String
)
