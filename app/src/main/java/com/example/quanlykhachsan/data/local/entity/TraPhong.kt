
package com.example.quanlykhachsan.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import java.util.Date

@Entity(
    tableName = "tra_phong",
    foreignKeys = [
        ForeignKey(
            entity = DatPhong::class,
            parentColumns = ["maDatPhong"],
            childColumns = ["maDatPhong"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("maDatPhong")]
)
data class TraPhong(
    @PrimaryKey(autoGenerate = true)
    val maTraPhong: Int = 0,
    val maDatPhong: Int,
    val tongTien: Double,
    val hinhThucThanhToan: String,
    val ngayThanhToan: Date,
    val ghiChu: String?
)
