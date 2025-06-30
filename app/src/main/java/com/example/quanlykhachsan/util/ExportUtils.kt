package com.example.quanlykhachsan.util

import org.apache.poi.ss.usermodel.Workbook
import java.io.OutputStream

object ExportUtils {
    fun writeWorkbook(wb: Workbook, os: OutputStream) {
        wb.write(os); os.flush(); os.close(); wb.close()
    }
}