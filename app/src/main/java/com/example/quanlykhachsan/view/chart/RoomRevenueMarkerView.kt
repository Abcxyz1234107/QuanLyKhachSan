package com.example.quanlykhachsan.view.chart

import android.content.Context
import android.widget.TextView
import com.example.quanlykhachsan.R
import com.example.quanlykhachsan.data.local.model.RoomTypeRevenue
import com.github.mikephil.charting.components.MarkerView
import com.github.mikephil.charting.utils.MPPointF
import java.text.NumberFormat
import java.util.Locale

class RoomRevenueMarkerView(
    ctx: Context
) : MarkerView(ctx, R.layout.marker_room_revenue) {

    private val tvContent: TextView = findViewById(R.id.tvContent)
    private val vnCur = NumberFormat.getCurrencyInstance(Locale("vi", "VN"))
    private var data: List<RoomTypeRevenue> = emptyList()

    /** nhận list doanh thu đã sắp giảm dần */
    fun setData(list: List<RoomTypeRevenue>) {
        data = list
    }

    override fun refreshContent(e: com.github.mikephil.charting.data.Entry?, h: com.github.mikephil.charting.highlight.Highlight?) {
        if (data.isEmpty()) {
            tvContent.text = context.getString(R.string.no_data)
        } else {
            // Ghép 3 dòng đầu: Loại – tiền
            tvContent.text = data.take(3).joinToString("\n") {
                "${it.tenLoaiPhong}: ${vnCur.format(it.total)}"
            }
        }
        super.refreshContent(e, h)
    }

    // Đẩy marker lên trên lát & canh giữa
    override fun getOffset(): MPPointF =
        MPPointF(-(width / 2f), -height.toFloat() - 10f)
}
