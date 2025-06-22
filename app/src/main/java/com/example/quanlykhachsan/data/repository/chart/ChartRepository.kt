package com.example.quanlykhachsan.data.repository.chart

interface ChartRepository {
    /** @return Map<Month(1‒12), Doanh thu>  */
    suspend fun getMonthlyRevenue(year: Int): Map<Int, Double>
}
