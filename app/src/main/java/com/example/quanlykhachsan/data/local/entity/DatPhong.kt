
package com.example.quanlykhachsan.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import java.util.Date

@Entity(
    tableName = "dat_phong",
    foreignKeys = [
        ForeignKey(
            entity = Phong::class,
            parentColumns = ["maPhong"],
            childColumns = ["maPhong"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("maPhong")]
)
data class DatPhong(
    @PrimaryKey(autoGenerate = true)
    val maDatPhong: Int = 0,
    val maPhong: Int,
    val tenKhach: String,
    val soCCCD: String,
    val soDienThoai: String,
    val ngayDatPhong: Date,
    val ngayNhanPhong: Date,
    val ngayTraPhong: Date?,
    val tinhTrangDatPhong: String,
    val ghiChu: String?
)
