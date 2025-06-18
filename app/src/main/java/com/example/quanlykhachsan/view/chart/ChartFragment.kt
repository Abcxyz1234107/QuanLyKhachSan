package com.example.quanlykhachsan.view.chart

import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.example.quanlykhachsan.R
import com.example.quanlykhachsan.databinding.FragmentChartBinding
import com.example.quanlykhachsan.viewmodel.ChartViewModel
import com.github.mikephil.charting.components.Legend
import com.github.mikephil.charting.data.PieData
import com.github.mikephil.charting.data.PieDataSet
import com.github.mikephil.charting.data.PieEntry
import com.github.mikephil.charting.formatter.ValueFormatter
import dagger.hilt.android.AndroidEntryPoint
import java.text.NumberFormat
import java.time.LocalDate
import java.util.Locale

@AndroidEntryPoint
class ChartFragment : Fragment(R.layout.fragment_chart) {

    private val viewModel: ChartViewModel by viewModels()
    private lateinit var binding: FragmentChartBinding
    private val vnCurrency: NumberFormat =
        NumberFormat.getCurrencyInstance(Locale("vi", "VN"))

    // ───────── onViewCreated ─────────
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = FragmentChartBinding.bind(view)

        configPieChart()
        initYearSpinner()
        observeChartData()
    }

    // Cấu hình mặc định cho PieChart
    private fun configPieChart() = with(binding.pieChart) {
        setUsePercentValues(false)        // hiển thị giá trị tuyệt đối, không %
        description.isEnabled = false
        isDrawHoleEnabled = true
        holeRadius = 40f
        setEntryLabelTextSize(12f)

        legend.apply {
            verticalAlignment = Legend.LegendVerticalAlignment.CENTER
            horizontalAlignment = Legend.LegendHorizontalAlignment.RIGHT
            orientation = Legend.LegendOrientation.VERTICAL
            isWordWrapEnabled = true
        }
    }

    // Khởi tạo Spinner chọn năm (10 năm gần nhất)
    private fun initYearSpinner() {
        val currentYear = LocalDate.now().year
        val years = (currentYear downTo currentYear - 9).toList()
        binding.spYear.adapter =
            ArrayAdapter(requireContext(), android.R.layout.simple_spinner_dropdown_item, years)

        binding.spYear.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>, view: View?, position: Int, id: Long
            ) {
                viewModel.setYear(years[position])
            }

            override fun onNothingSelected(parent: AdapterView<*>) = Unit
        }

        // Chọn mặc định năm hiện tại
        binding.spYear.setSelection(0)
    }

    // Lắng nghe LiveData PieEntry từ ViewModel
    private fun observeChartData() {
        viewModel.pieEntries.observe(viewLifecycleOwner) { entries ->
            val dataSet = PieDataSet(entries, "").apply {
                setDrawValues(true)
                valueFormatter = object : ValueFormatter() {
                    override fun getPieLabel(value: Float, entry: PieEntry?): String =
                        vnCurrency.format(value.toLong())          // hiển thị VNĐ
                }
            }
            binding.pieChart.data = PieData(dataSet)
            binding.pieChart.invalidate() // refresh
        }
    }
}
