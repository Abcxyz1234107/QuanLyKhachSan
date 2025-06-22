package com.example.quanlykhachsan.viewmodel

import androidx.lifecycle.*
import com.example.quanlykhachsan.data.repository.chart.ChartRepository
import com.github.mikephil.charting.data.PieEntry
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.time.LocalDate
import java.time.ZoneId
import java.time.Instant
import javax.inject.Inject


@HiltViewModel
class ChartViewModel @Inject constructor(
    private val repo: ChartRepository
) : ViewModel() {

    /* ───── LiveData doanh thu 4 quý ───── */
    private val selectedYear = MutableLiveData(LocalDate.now().year)
    val quarterlyEntries: LiveData<Map<Int, List<PieEntry>>> =
        selectedYear.switchMap { year ->
            liveData {
                // 1. Lấy doanh thu từng tháng
                val monthly = withContext(Dispatchers.IO) {
                    repo.getMonthlyRevenue(year)
                }

                // 2. Gom theo quý → PieEntry
                val qMap = mutableMapOf<Int, MutableList<PieEntry>>()
                monthly.forEach { (month, total) ->
                    val q = (month - 1) / 3 + 1          // 1‒4
                    qMap.getOrPut(q) { mutableListOf() }
                        .add(PieEntry(total.toFloat(), "Tháng $month"))
                }
                emit(qMap)                               // LiveData<Map<Int,List<PieEntry>>>
            }
        }

    /* ───── Khác ───── */
    val valueTextSize = MutableLiveData(12f)
    val sliceColors   = MutableLiveData<List<Int>>()

    fun setValueTextSize(sizeDp: Float) = valueTextSize.postValue(sizeDp)
    fun setSliceColors(colors: List<Int>) = sliceColors.postValue(colors)
    fun setYear(year: Int) { if (year != selectedYear.value) selectedYear.value = year }
}