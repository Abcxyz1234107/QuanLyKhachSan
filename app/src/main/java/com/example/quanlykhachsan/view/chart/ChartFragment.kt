package com.example.quanlykhachsan.view.chart

import android.graphics.Color
import androidx.core.content.ContextCompat
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

        configPieCharts()
        observeChartConfig()
        observeQuarterData()
    }

    // -------------------- cấu hình 4 PieChart --------------------
    private fun configPieCharts() {
        listOf(
            binding.pieChartQ1, binding.pieChartQ2,
            binding.pieChartQ3, binding.pieChartQ4
        ).forEach { chart ->
            chart.apply {
                setUsePercentValues(false)
                description.isEnabled = false
                isDrawHoleEnabled = false    // bỏ vòng trong ở tâm
                holeRadius = 0f
                transparentCircleRadius = 0f
                setEntryLabelTextSize(viewModel.valueTextSize.value ?: 12f)
                legend.isEnabled = false
            }
        }
    }
    // -------------------- LiveData  --------------------
    private fun observeChartConfig() {
        viewModel.valueTextSize.observe(viewLifecycleOwner) { size ->
            listOf(
                binding.pieChartQ1, binding.pieChartQ2,
                binding.pieChartQ3, binding.pieChartQ4
            ).forEach { it.setEntryLabelTextSize(size) }
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
    private fun observeQuarterData() {
        viewModel.quarterlyEntries.observe(viewLifecycleOwner) { map ->
            val defaultColors = listOf(
                Color.parseColor("#2196F3"), Color.parseColor("#4CAF50"),
                Color.parseColor("#FFC107"), Color.parseColor("#F44336"),
                Color.parseColor("#9C27B0"), Color.parseColor("#03A9F4")
            )
            val sliceColors = viewModel.sliceColors.value ?: defaultColors

            // Helper để tạo PieData
            fun toPieData(entries: List<PieEntry>) = PieData(
                PieDataSet(entries, "").apply {
                    colors = sliceColors
                    valueFormatter = object : ValueFormatter() {
                        override fun getPieLabel(value: Float, entry: PieEntry?): String =
                            vnCurrency.format(value.toLong())
                    }
                    valueTextSize = viewModel.valueTextSize.value ?: 12f
                })

            // Gán dữ liệu cho từng chart; ẩn chart nếu quý không có
            listOf(
                binding.pieChartQ1 to 1,
                binding.pieChartQ2 to 2,
                binding.pieChartQ3 to 3,
                binding.pieChartQ4 to 4
            ).forEach { (chart, qIndex) ->
                map[qIndex]?.let { entries ->
                    chart.visibility = View.VISIBLE
                    chart.data = toPieData(entries)
                    chart.invalidate()
                } ?: run { chart.visibility = View.GONE }
            }
        }
    }
}
