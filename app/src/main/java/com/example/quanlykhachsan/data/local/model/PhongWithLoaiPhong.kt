package com.example.quanlykhachsan.data.local.model

import androidx.room.ColumnInfo

data class PhongWithLoaiPhong(
    @ColumnInfo(name = "maPhong")
    val maPhong: Int,
    @ColumnInfo(name = "tenLoaiPhong")
    val tenLoaiPhong: String
)