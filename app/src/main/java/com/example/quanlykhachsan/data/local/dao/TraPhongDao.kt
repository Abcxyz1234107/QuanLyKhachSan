package com.example.quanlykhachsan.data.local.dao

import androidx.lifecycle.LiveData
import androidx.room.*
import com.example.quanlykhachsan.data.local.entity.TraPhong
import com.example.quanlykhachsan.data.local.model.MonthRevenue
import com.example.quanlykhachsan.data.local.model.RoomTypeRevenue
import java.time.LocalDate
import java.time.ZoneId
import java.time.Instant


@Dao
interface TraPhongDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(traPhong: TraPhong): Long

    @Update
    suspend fun update(traPhong: TraPhong)

    @Delete
    suspend fun delete(traPhong: TraPhong)

    @Query("SELECT * FROM tra_phong ORDER BY ngayThanhToan DESC")
    fun getAll(): LiveData<List<TraPhong>>

    @Query("SELECT * FROM tra_phong WHERE maTraPhong = :id")
    suspend fun getById(id: Int): TraPhong?

    @Query("SELECT * FROM tra_phong WHERE maDatPhong = :datPhongId")
    fun getByDatPhong(datPhongId: Int): LiveData<List<TraPhong>>

    @Query(
        """
    SELECT 
        -- dùng 'localtime' để đổi sang múi giờ thiết bị
        CAST(strftime('%m', datetime(ngayThanhToan / 1000, 'unixepoch', 'localtime')) AS INT) AS month,
        SUM(tongTien)                                                                 AS total
    FROM tra_phong
    WHERE CAST(strftime('%Y', datetime(ngayThanhToan / 1000, 'unixepoch', 'localtime')) AS INT) = :year
    GROUP BY month
    ORDER BY month
    """
    )
    suspend fun getMonthlyRevenue(year: Int): List<MonthRevenue>

    @Query(
        "SELECT * " +
                "FROM tra_phong " +
                "WHERE ngayThanhToan BETWEEN :startMillis AND :endMillis"
    )
    suspend fun getByDateRange(startMillis: Long,endMillis: Long): List<TraPhong>

    @Query("SELECT COUNT(*) FROM tra_phong WHERE maDatPhong = :maDatPhong")
    suspend fun countByDatPhongId(maDatPhong: Int): Int

    @Query(
        """
    SELECT lp.tenLoaiPhong        AS tenLoaiPhong,
           SUM(tp.tongTien)       AS total
    FROM tra_phong tp
    JOIN dat_phong  dp ON dp.maDatPhong = tp.maDatPhong
    JOIN phong      p  ON p.maPhong     = dp.maPhong
    JOIN loai_phong lp ON lp.maLoaiPhong = p.maLoaiPhong
    WHERE tp.ngayThanhToan BETWEEN :startMs AND :endMs
    GROUP BY lp.tenLoaiPhong
    ORDER BY total DESC
    """
    )
    suspend fun getRevenueByRoomTypeRange(
        startMs: Long,
        endMs: Long
    ): List<RoomTypeRevenue>
}
