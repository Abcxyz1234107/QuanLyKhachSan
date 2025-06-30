package com.example.quanlykhachsan.data.repository.chart

import com.example.quanlykhachsan.data.local.model.RoomTypeRevenue

interface ChartRepository {
    /** @return Map<Month(1‒12), Doanh thu>  */
    suspend fun getMonthlyRevenue(year: Int): Map<Int, Double>
    suspend fun getRoomTypeRevenue(year: Int, month: Int): List<RoomTypeRevenue>
}
