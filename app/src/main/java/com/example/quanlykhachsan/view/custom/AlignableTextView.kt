package com.example.quanlykhachsan.view.custom

import android.content.Context
import android.graphics.Canvas
import android.util.AttributeSet
import androidx.appcompat.widget.AppCompatTextView
import com.example.quanlykhachsan.R

/**
 * Custom TextView cho phép căn chỉnh text theo 1 hệ số tuỳ ý (0f = trái, 1f = phải).
 */
class AlignableTextView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = android.R.attr.textViewStyle
) : AppCompatTextView(context, attrs, defStyleAttr) {

    private var alignFactor = 0f

    init {
        attrs?.let {
            // Đọc custom attribute nếu khai báo trong XML
            val a = context.obtainStyledAttributes(it, R.styleable.AlignableTextView)
            alignFactor = a.getFloat(R.styleable.AlignableTextView_alignFactor, 0f)
            a.recycle()
        }
    }

    /**
     * Thiết lập hệ số căn chỉnh:
     * - 0f: căn lề trái
     * - 0.5f: căn giữa
     * - 1f: căn lề phải
     */
    fun setAlignFactor(factor: Float) {
        alignFactor = factor.coerceIn(0f, 1f)
        invalidate()
    }

    override fun onDraw(canvas: Canvas) {
        // Tính chiều rộng text và khoảng trống khả dụng
        val textWidth = paint.measureText(text.toString())
        val available = (width - paddingLeft - paddingRight).toFloat()
        // Tính offset dịch để căn đúng hệ số
        val dx = (available - textWidth) * alignFactor
        canvas.save()
        canvas.translate(dx, 0f)
        super.onDraw(canvas)
        canvas.restore()
    }
}
