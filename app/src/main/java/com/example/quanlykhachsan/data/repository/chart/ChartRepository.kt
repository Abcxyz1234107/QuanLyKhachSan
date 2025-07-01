package com.example.quanlykhachsan.data.repository.chart

import com.example.quanlykhachsan.data.local.model.RoomTypeRevenue

interface ChartRepository {
    /** @return Map<Month(1‒12), Doanh thu>  */
    suspend fun getMonthlyRevenue(year: Int): Map<Int, Double>
    suspend fun getRoomTypeRevenue(year: Int, month: Int): List<RoomTypeRevenue>
    suspend fun getYearDetail(year: Int): YearDetail
}

data class YearDetail(
    val total: Double,
    val diff: Double, // chênh lệch so với năm trước
    val maxType: RoomTypeRevenue?,
    val minType: RoomTypeRevenue?
)