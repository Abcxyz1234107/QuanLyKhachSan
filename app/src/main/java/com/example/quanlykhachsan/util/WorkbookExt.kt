package com.example.quanlykhachsan.util

import org.apache.poi.xssf.usermodel.XSSFWorkbook

/** Thêm một Sheet & đổ data */
fun <T : Any> XSSFWorkbook.appendDataSheet(
    name: String,
    data: List<T>
) {
    if (data.isEmpty()) return
    val sheet = createSheet(name)

    // Header
    val fields = data.first().javaClass.declaredFields
    fields.forEachIndexed { c, f ->
        sheet.createRow(0).createCell(c).setCellValue(f.name)
    }

    // Body
    data.forEachIndexed { r, item ->
        val row = sheet.createRow(r + 1)
        fields.forEachIndexed { c, f ->
            f.isAccessible = true
            val raw = f.get(item)
            val text = raw?.toString() ?: ""
            row.createCell(c).setCellValue(text)
        }
    }
}