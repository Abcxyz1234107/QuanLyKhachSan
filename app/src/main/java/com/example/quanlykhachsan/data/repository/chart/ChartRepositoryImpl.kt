package com.example.quanlykhachsan.data.repository.chart

import com.example.quanlykhachsan.data.local.dao.TraPhongDao
import java.time.LocalDate
import java.time.ZoneId
import java.time.Instant
import javax.inject.Inject

class ChartRepositoryImpl @Inject constructor(
    private val dao: TraPhongDao
) : ChartRepository {

    override suspend fun getMonthlyRevenue(year: Int): Map<Int, Double> {
        val zone = ZoneId.systemDefault()
        val startMs = LocalDate.of(year, 1, 1).atStartOfDay(zone).toInstant().toEpochMilli()
        val endMs = LocalDate.of(year + 1, 1, 1).atStartOfDay(zone).toInstant().toEpochMilli() - 1
        val list = dao.getByDateRange(startMs, endMs)

        // Gom doanh thu theo tháng (1-12)
        return list.groupBy { tra ->
            Instant.ofEpochMilli(tra.ngayThanhToan.time)
                .atZone(zone).monthValue
        }.mapValues { (_, items) ->
            items.sumOf { it.tongTien }
        }
    }
}
