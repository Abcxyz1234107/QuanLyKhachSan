package com.example.quanlykhachsan.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "loai_phong")
data class LoaiPhong(
    @PrimaryKey(autoGenerate = true)
    val maLoaiPhong: Int = 0,
    val tenLoaiPhong: String,
    val gia: Double
)
