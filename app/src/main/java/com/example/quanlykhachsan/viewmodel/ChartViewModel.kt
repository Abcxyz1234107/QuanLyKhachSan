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

    fun setYear(year: Int) {
        if (year != selectedYear.value) selectedYear.value = year
    }
}
