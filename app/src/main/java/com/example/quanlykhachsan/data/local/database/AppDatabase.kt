package com.example.quanlykhachsan.data.local.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.example.quanlykhachsan.data.local.Converters
import com.example.quanlykhachsan.data.local.dao.*
import com.example.quanlykhachsan.data.local.entity.*
import com.example.quanlykhachsan.data.local.model.*

@Database(
    entities = [
        NguoiDungPhanMem::class,
        NhanVien::class,
        LoaiPhong::class,
        Phong::class,
        DatPhong::class,
        TraPhong::class
    ],
    version = 1,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {

    abstract fun nguoiDungPhanMemDao(): NguoiDungPhanMemDao
    abstract fun nhanVienDao(): NhanVienDao
    abstract fun loaiPhongDao(): LoaiPhongDao
    abstract fun phongDao(): PhongDao
    abstract fun datPhongDao(): DatPhongDao
    abstract fun traPhongDao(): TraPhongDao

    companion object {
        @Volatile private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase =
            INSTANCE ?: synchronized(this) {
                Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "quan_ly_khach_san_db"
                )

                    .build()
                    .also { INSTANCE = it }
            }
    }
}
