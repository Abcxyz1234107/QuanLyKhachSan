package com.example.quanlykhachsan.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "phong",
    foreignKeys = [
        ForeignKey(
            entity = LoaiPhong::class,
            parentColumns = ["maLoaiPhong"],
            childColumns = ["maLoaiPhong"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("maLoaiPhong")]
)
data class Phong(
    @PrimaryKey(autoGenerate = true)
    val maPhong: Int = 0,
    val maLoaiPhong: Int,
    val trangThaiPhong: String
)
