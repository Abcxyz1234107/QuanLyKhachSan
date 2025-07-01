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
import com.github.mikephil.charting.highlight.Highlight
import com.github.mikephil.charting.listener.OnChartValueSelectedListener
import dagger.hilt.android.AndroidEntryPoint
import java.text.NumberFormat
import java.time.LocalDate
import java.util.Locale
import com.github.mikephil.charting.data.Entry

@AndroidEntryPoint
class ChartFragment : Fragment(R.layout.fragment_chart) {

    private val viewModel: ChartViewModel by viewModels()
    private lateinit var binding: FragmentChartBinding
    private val vnCurrency: NumberFormat = NumberFormat.getCurrencyInstance(Locale("vi", "VN"))
    private lateinit var adapter: ChartAdapter
    private var selectedEntry: PieEntry? = null
    private var lastSelectedChart: com.github.mikephil.charting.charts.PieChart? = null


    // ───────── onViewCreated ─────────
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = FragmentChartBinding.bind(view)

        initYearSpinner()
        configPieCharts()
        observeChartConfig()
        observeQuarterData()

        setupRecycler()
        observeRoomTypeRevenue()

        viewModel.yearDetail.observe(viewLifecycleOwner) { d ->
            val cur = vnCurrency
            binding.tvTotalYear.text =
                "Tổng doanh thu năm: ${cur.format(d.total)}"

            val diffText = if (d.diff >= 0)
                "Tăng ${cur.format(d.diff)} so với năm trước"
            else
                "Giảm ${cur.format(-d.diff)} so với năm trước"
            binding.tvCompare.text = diffText

            binding.tvMaxType.text = if (d.maxType != null)
                "LP doanh thu cao nhất: ${d.maxType.tenLoaiPhong} – ${cur.format(d.maxType.total)}"
            else "LP doanh thu cao nhất: …"

            binding.tvMinType.text = if (d.minType != null)
                "LP doanh thu thấp nhất: ${d.minType.tenLoaiPhong} – ${cur.format(d.minType.total)}"
            else "LP doanh thu thấp nhất: …"
        }
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
                chart.marker = RoomRevenueMarkerView(requireContext())
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

    // Spinner chọn năm (10 năm gần nhất)
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
        val quarterLabels = mapOf(
            1 to binding.tvQ1,
            2 to binding.tvQ2,
            3 to binding.tvQ3,
            4 to binding.tvQ4
        )

        viewModel.quarterlyEntries.observe(viewLifecycleOwner) { map ->
            val defaultColors = listOf(
                Color.parseColor("#2196F3"), Color.parseColor("#4CAF50"),
                Color.parseColor("#FFC107"), Color.parseColor("#F44336"),
                Color.parseColor("#9C27B0"), Color.parseColor("#03A9F4")
            )
            val sliceColors = viewModel.sliceColors.value ?: defaultColors

            fun toPieData(entries: List<PieEntry>) = PieData(
                PieDataSet(entries, "").apply {
                    colors = sliceColors
                    valueFormatter = object : ValueFormatter() {
                        override fun getPieLabel(v: Float, e: PieEntry?) =
                            vnCurrency.format(v.toLong())
                    }
                    valueTextSize = viewModel.valueTextSize.value ?: 12f
                })

            listOf(
                binding.pieChartQ1 to 1,
                binding.pieChartQ2 to 2,
                binding.pieChartQ3 to 3,
                binding.pieChartQ4 to 4
            ).forEach { (chart, q) ->
                map[q]?.let { entries ->
                    chart.visibility = View.VISIBLE
                    quarterLabels[q]?.visibility = View.VISIBLE

                    chart.data = toPieData(entries)
                    chart.setOnChartValueSelectedListener(object : OnChartValueSelectedListener {
                        override fun onValueSelected(e: Entry?, h: Highlight?) {
                            val entry = e as? PieEntry ?: return
                            if (selectedEntry == entry) return
                            selectedEntry = entry
                            lastSelectedChart = chart

                            val month = entry.label.substringAfter(' ').trim().toIntOrNull() ?: return
                            val year  = binding.spYear.selectedItem as Int
                            viewModel.onSliceSelected(year, month)
                        }
                        override fun onNothingSelected() {
                            selectedEntry = null
                            lastSelectedChart = null
                            viewModel.clearSlice()
                        }
                    })
                    chart.invalidate()
                } ?: run {
                    chart.visibility = View.GONE
                    quarterLabels[q]?.visibility = View.GONE
                }
            }
        }
    }

    private fun setupRecycler() {
        adapter = ChartAdapter()
    }

    private fun observeRoomTypeRevenue() {
        viewModel.roomTypeRevenue.observe(viewLifecycleOwner) { list ->
            lastSelectedChart?.let { chart ->
                (chart.marker as? RoomRevenueMarkerView)?.also { marker ->
                    marker.setData(list)
                    chart.highlightValues(chart.highlighted)   // refresh marker
                }
            }
            if (!list.isEmpty()) adapter.submit(list)
        }
    }
}
