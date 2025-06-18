package com.example.quanlykhachsan.viewmodel

import androidx.lifecycle.*
import com.github.mikephil.charting.data.PieEntry
import dagger.hilt.android.lifecycle.HiltViewModel
import java.time.LocalDate
import javax.inject.Inject

@HiltViewModel
class ChartViewModel @Inject constructor(
    // TODO: inject repository sau khi có
) : ViewModel() {

    private val selectedYear = MutableLiveData(LocalDate.now().year)

    val pieEntries: LiveData<List<PieEntry>> = selectedYear.map { year ->
        // Dummy demo data: tháng 9, 10, 11, 12
        listOf(
            PieEntry(1_400_000f, "Tháng 9"),
            PieEntry(1_500_000f, "Tháng 10"),
            PieEntry(3_000_000f, "Tháng 11"),
            PieEntry(7_690_000f, "Tháng 12"),
        )
    }

    val valueTextSize = MutableLiveData(12f)  // dp
    val sliceColors   = MutableLiveData<List<Int>>()  //  màu ARGB

    // -------------------- Dữ liệu 4 quý --------------------
    data class Quarter(val index: Int, val label: String)

    private val quarters = listOf(
        Quarter(1, "Quý I"), Quarter(2, "Quý II"),
        Quarter(3, "Quý III"), Quarter(4, "Quý IV")
    )

    /** Map<QuarterIndex, List<PieEntry>> */
    val quarterlyEntries: LiveData<Map<Int, List<PieEntry>>> =
        selectedYear.switchMap { year ->
            liveData {
                // TODO: Replace dummy with repository.fetchQuarterRevenue(year)
                val dummy = mapOf(
                    1 to listOf(
                        PieEntry(900_000f, "Tháng 1"),
                        PieEntry(1_200_000f, "Tháng 2"),
                        PieEntry(1_100_000f, "Tháng 3")
                    ),
                    2 to listOf(
                        PieEntry(1_600_000f, "Tháng 4"),
                        PieEntry(1_900_000f, "Tháng 5"),
                        PieEntry(1_300_000f, "Tháng 6")
                    ),
                    3 to listOf(
                        PieEntry(2_640_000f, "Tháng 7"),
                        PieEntry(3_870_000f, "Tháng 8"),
                        PieEntry(5_150_000f, "Tháng 9")
                    ),
                    4 to listOf(
                        PieEntry(3_000_000f, "Tháng 10"),
                        PieEntry(7_690_000f, "Tháng 12")
                    )
                )
                emit(dummy.filterValues { it.isNotEmpty() })         // loại quý trống
            }
        }

    fun setValueTextSize(sizeDp: Float) = valueTextSize.postValue(sizeDp)
    fun setSliceColors(colors: List<Int>)  = sliceColors.postValue(colors)

    fun setYear(year: Int) {
        if (year != selectedYear.value) selectedYear.value = year
    }
}
