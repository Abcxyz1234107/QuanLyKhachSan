package com.example.quanlykhachsan.data.repository.chart

import com.example.quanlykhachsan.data.local.dao.TraPhongDao
import com.example.quanlykhachsan.data.local.model.RoomTypeRevenue
import java.time.LocalDate
import java.time.ZoneId
import java.time.Instant
import javax.inject.Inject

class ChartRepositoryImpl @Inject constructor(
    private val dao: TraPhongDao
) : ChartRepository {

    override suspend fun getMonthlyRevenue(year: Int): Map<Int, Double> {
        // 1. Khoảng thời gian của năm (00:00 01-01 đến 23:59 31-12 LOCAL)
        val zone   = ZoneId.systemDefault()
        val start  = LocalDate.of(year, 1, 1)
            .atStartOfDay(zone).toInstant().toEpochMilli()
        val end    = LocalDate.of(year + 1, 1, 1)
            .atStartOfDay(zone).toInstant().toEpochMilli() - 1

        val list = dao.getByDateRange(start, end)
        val cal = java.util.Calendar.getInstance()

        return list.groupBy { tra ->
            cal.time = tra.ngayThanhToan
            cal.get(java.util.Calendar.MONTH) + 1
        }.mapValues { (_, items) ->
            items.sumOf { it.tongTien }
        }
    }
    override suspend fun getRoomTypeRevenue(year: Int, month: Int): List<RoomTypeRevenue> {
        val zone   = java.time.ZoneId.systemDefault()
        val start  = java.time.LocalDate.of(year, month, 1)
            .atStartOfDay(zone).toInstant().toEpochMilli()
        val end    = java.time.LocalDate.of(year, month, 1)
            .plusMonths(1)
            .atStartOfDay(zone).toInstant().toEpochMilli() - 1

        return dao.getRevenueByRoomTypeRange(start, end)
    }

    override suspend fun getYearDetail(year: Int): YearDetail {
        val zone = java.time.ZoneId.systemDefault()

        fun firstMs(y: Int) = java.time.LocalDate.of(y,1,1)
            .atStartOfDay(zone).toInstant().toEpochMilli()
        fun lastMs(y: Int) = java.time.LocalDate.of(y+1,1,1)
            .atStartOfDay(zone).toInstant().toEpochMilli() - 1

        val totalThis = dao.getTotalRevenueRange(firstMs(year), firstMs(year+1)-1)
        val totalPrev = dao.getTotalRevenueRange(firstMs(year-1), firstMs(year)-1)

        val listType = dao.getRevenueByRoomTypeRange(firstMs(year), firstMs(year+1)-1)
        val max = listType.maxByOrNull { it.total }
        val min = listType.minByOrNull { it.total }

        return YearDetail(
            total   = totalThis,
            diff    = totalThis - totalPrev,
            maxType = max,
            minType = min
        )
    }


}
