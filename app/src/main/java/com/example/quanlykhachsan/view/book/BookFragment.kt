package com.example.quanlykhachsan.view.book

import android.app.AlertDialog
import android.app.DatePickerDialog
import android.content.res.Resources
import android.os.Bundle
import androidx.recyclerview.widget.LinearLayoutManager
import android.text.InputType
import android.view.View
import android.widget.ArrayAdapter
import android.widget.LinearLayout
import android.widget.NumberPicker
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.LiveData
import androidx.lifecycle.asLiveData
import androidx.lifecycle.lifecycleScope
import com.example.quanlykhachsan.R
import com.example.quanlykhachsan.data.local.entity.DatPhong
import com.example.quanlykhachsan.databinding.FragmentBookBinding
import com.example.quanlykhachsan.viewmodel.BookViewModel
import com.google.android.material.textfield.TextInputEditText
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.*
import com.example.quanlykhachsan.databinding.DialogBookingDetailBinding
import java.util.Locale
import kotlinx.coroutines.flow.map

private var roomTypeMap: Map<Int, String> = emptyMap()

class BookFragment : Fragment(R.layout.fragment_book) {

    private var selectedBooking: DatPhong? = null
    private var _bd: FragmentBookBinding? = null
    private val binding get() = _bd!!
    private val bd get() = _bd!!
    private val vm by viewModels<BookViewModel>()
    private val sdf = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())

    val adapter = BookAdapter { vm.setSelectedBooking(it) }  // callback trả DatPhong? cho ViewModel

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _bd = FragmentBookBinding.bind(view)

        /* ---------- Kết đặt và phòng ---------- */
        vm.roomTypeByRoomId.observe(viewLifecycleOwner) { map ->
            roomTypeMap = map
        }

        /* ---------- RecyclerView ---------- */
        bd.rvBook.layoutManager = LinearLayoutManager(requireContext())
        bd.rvBook.adapter = adapter
        vm.bookings.observe(viewLifecycleOwner) { list -> adapter.submit(list) }

        /* ---------- TỰ ĐIỀN form khi chọn / bỏ chọn ---------- */
        vm.selectedBooking.observe(viewLifecycleOwner) { dp ->
            if (dp == null) {
                bd.edtCustomerPhone.setText("")
                bd.edtRoomType.setText("")
                bd.edtDateIn.setText("")
                bd.edtDateOut.setText("")
            } else {
                bd.edtCustomerPhone.setText(dp.soDienThoai)
                bd.edtRoomType.setText(roomTypeMap[dp.maPhong] ?: "")
                bd.edtDateIn.setText(sdf.format(dp.ngayNhanPhong))
                bd.edtDateOut.setText(dp.ngayTraPhong?.let { sdf.format(it) } ?: "")
            }
        }

        /* ---------- Nút ---------- */
        bd.btnDetail.setOnClickListener {
            val booking = vm.selectedBooking.value
            if (booking == null) toast("Hãy chọn đơn đặt phòng!") else showDetailDialog(booking)
        }
        /* ---------- Nút Nhận phòng ---------- */
        bd.btnReceive.setOnClickListener {
            vm.selectedBooking.value?.let(vm::receiveBooking)
                ?: toast("Hãy chọn một đơn đặt phòng trước!")
        }
        /* ---------- Nút SỬA ---------- */
        bd.btnEdit.setOnClickListener {
            val dp = vm.selectedBooking.value
            if (dp == null) {
                toast("Hãy chọn một đơn đặt phòng!")
                return@setOnClickListener
            }
            val dateIn  = parseDate(bd.edtDateIn.text.toString())
            val dateOut = parseDate(bd.edtDateOut.text.toString())
            if (dateIn == null || dateOut == null) {
                toast("Ngày nhận / trả chưa hợp lệ!")
                return@setOnClickListener
            }
            vm.updateBookingSimple(
                bd.edtCustomerPhone.text.toString(),
                bd.edtRoomType.text.toString(),
                dateIn, dateOut
            )
        }
        /* ---------- Nút XOÁ ---------- */
        bd.btnDelete.setOnClickListener {
            vm.selectedBooking.value?.let(::confirmDelete)
                ?: toast("Hãy chọn một đơn đặt phòng!")
        }

        /* ---------- DatePicker spinner ---------- */
        setUpDatePicker(bd.edtDateIn)
        setUpDatePicker(bd.edtDateOut)

        /* ---------- Load tên loại phòng vào dropdown ---------- */
        vm.roomTypeNames.observe(viewLifecycleOwner) { names ->
            val adapter = ArrayAdapter(requireContext(),
                android.R.layout.simple_list_item_1, names)
            bd.edtRoomType.setAdapter(adapter)
        }

        /* ---------- Xử lý nút Thêm ---------- */
        bd.btnAdd.setOnClickListener {
            val phone      = bd.edtCustomerPhone.text.toString()
            val roomType   = bd.edtRoomType.text.toString()
            val dateIn     = parseDate(bd.edtDateIn.text.toString())
            val dateOut    = parseDate(bd.edtDateOut.text.toString())

            if (dateIn == null || dateOut == null) {
                Toast.makeText(requireContext(),
                    "Vui lòng chọn ngày nhận / trả!", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            vm.addBooking(phone, roomType, dateIn, dateOut)
        }

        /* ---------- Hiển thị thông báo kết quả ---------- */
        vm.message.observe(viewLifecycleOwner) { msg ->
            Toast.makeText(requireContext(), msg, Toast.LENGTH_SHORT).show()
        }

        // Bỏ focus khi ấn ngoài EditText
        binding.root.setOnTouchListener { v, event ->
            v.clearFocus()
            requireActivity().currentFocus?.clearFocus()
            false
        }
    }

    private fun setUpDatePicker(edt: TextInputEditText) = attachSpinner(edt, null)
    /* —— dùng chung attachSpinner với dialog —— */
    private fun attachSpinner(edt: TextInputEditText, init: Date?) {
        edt.inputType = InputType.TYPE_NULL
        edt.setOnClickListener {
            val cal = Calendar.getInstance().apply { time = init ?: Date() }
            val dlg = DatePickerDialog(
                requireContext(), R.style.ThemeOverlay_App_DatePicker_Spinner,
                null, cal[Calendar.YEAR], cal[Calendar.MONTH], cal[Calendar.DAY_OF_MONTH]
            )
            dlg.datePicker.calendarViewShown = false
            dlg.setOnShowListener {
                val sys = Resources.getSystem()
                val day   = dlg.datePicker.findViewById<NumberPicker>(sys.getIdentifier("day","id","android"))
                val month = dlg.datePicker.findViewById<NumberPicker>(sys.getIdentifier("month","id","android"))
                val year  = dlg.datePicker.findViewById<NumberPicker>(sys.getIdentifier("year","id","android"))
                (day.parent as? LinearLayout)?.apply { removeAllViews(); addView(day); addView(month); addView(year) }
                val viMonths = arrayOf("Tháng 1","Tháng 2","Tháng 3","Tháng 4","Tháng 5","Tháng 6","Tháng 7","Tháng 8","Tháng 9","Tháng 10","Tháng 11","Tháng 12")
                month.displayedValues = viMonths
                fun update() { cal.set(year.value, month.value, day.value); edt.setText(sdf.format(cal.time)) }
                listOf(day, month, year).forEach { it.setOnValueChangedListener { _, _, _ -> update() } }
                update()
            }
            dlg.show()
        }
    }


    private fun toast(msg: String) = Toast.makeText(requireContext(), msg, Toast.LENGTH_SHORT).show()
    private fun parseDate(str: String): Date? =
        try { sdf.parse(str) } catch (e: ParseException) { null }

    override fun onDestroyView() {
        super.onDestroyView()
        _bd = null
    }

    private fun confirmDelete(dp: DatPhong) {
        androidx.appcompat.app.AlertDialog.Builder(requireContext())
            .setTitle("Xóa đơn phòng #${dp.maPhong}?")
            .setMessage("Bạn chắc chắn muốn xoá đơn đặt phòng này?")
            .setPositiveButton("Xóa") { _, _ -> vm.deleteBooking(dp) }
            .setNegativeButton("Hủy", null)
            .show()
    }

    private val viDateFormat = SimpleDateFormat("dd/MM/yyyy", Locale("vi"))

    private fun showDetailDialog(dp: DatPhong) {
        val b = DialogBookingDetailBinding.inflate(layoutInflater)
        var isSaved = false

        /* ------- fill mặc định ------- */
        val typeName = roomTypeMap[dp.maPhong] ?: "-"
        b.tvLoaiPhong.text  = "Loại phòng: $typeName"
        b.tvRoomHeader.setText     (dp.maPhong.toString())
        b.edtTenKhach.setText    (dp.tenKhach)
        b.edtSoCCCD.setText      (dp.soCCCD)
        b.edtSoDT.setText        (dp.soDienThoai)
        b.edtNgayDat.setText     (viDateFormat.format(dp.ngayDatPhong))
        b.edtNgayNhan.setText    (viDateFormat.format(dp.ngayNhanPhong))
        b.edtNgayTra.setText     (dp.ngayTraPhong?.let { viDateFormat.format(it) } ?: "")
        b.edtTinhTrang.setText   (dp.tinhTrangDatPhong)
        b.edtGhiChu.setText      (dp.ghiChu ?: "")

        /* ------- attach DatePicker spinner cho 3 ô ngày ------- */
        fun attachSpinner(edt: TextInputEditText, init: Date?) {
            edt.inputType = InputType.TYPE_NULL
            edt.setOnClickListener {
                val cal = Calendar.getInstance().apply { time = init ?: Date() }
                val dpd = DatePickerDialog(
                    requireContext(),
                    R.style.ThemeOverlay_App_DatePicker_Spinner,
                    null,
                    cal[Calendar.YEAR], cal[Calendar.MONTH], cal[Calendar.DAY_OF_MONTH]
                )
                dpd.datePicker.calendarViewShown = false
                /* --- đoạn tuỳ biến thứ tự & tên tháng giống StaffFragment --- */
                dpd.setOnShowListener {
                    val res = Resources.getSystem()
                    val day   = dpd.datePicker.findViewById<NumberPicker>(res.getIdentifier("day", "id", "android"))
                    val month = dpd.datePicker.findViewById<NumberPicker>(res.getIdentifier("month", "id", "android"))
                    val year  = dpd.datePicker.findViewById<NumberPicker>(res.getIdentifier("year", "id", "android"))
                    (day.parent as? LinearLayout)?.let { c ->
                        c.removeAllViews(); c.addView(day); c.addView(month); c.addView(year)
                    }
                    val viMonths = arrayOf(
                        "Tháng 1","Tháng 2","Tháng 3","Tháng 4","Tháng 5","Tháng 6",
                        "Tháng 7","Tháng 8","Tháng 9","Tháng 10","Tháng 11","Tháng 12")
                    month?.displayedValues = viMonths
                    fun update() {
                        val newCal = Calendar.getInstance().apply {
                            set(year?.value ?: cal[Calendar.YEAR],
                                month?.value ?: cal[Calendar.MONTH],
                                day?.value ?: cal[Calendar.DAY_OF_MONTH])
                        }
                        edt.setText(viDateFormat.format(newCal.time))
                    }
                    day?.setOnValueChangedListener { _, _, _ -> update() }
                    month?.setOnValueChangedListener { _, _, _ -> update() }
                    year?.setOnValueChangedListener { _, _, _ -> update() }
                    update()
                }
                dpd.show()
            }
        }
        attachSpinner(b.edtNgayDat , dp.ngayDatPhong)
        attachSpinner(b.edtNgayNhan, dp.ngayNhanPhong)
        attachSpinner(b.edtNgayTra , dp.ngayTraPhong)

        /* ------- nút Lưu ------- */
        b.btnSave.setOnClickListener {
            val updated = dp.copy(
                tenKhach        = b.edtTenKhach.text.toString(),
                soCCCD          = b.edtSoCCCD.text.toString(),
                soDienThoai     = b.edtSoDT.text.toString(),
                ngayDatPhong    = viDateFormat.parse(b.edtNgayDat.text.toString()) ?: dp.ngayDatPhong,
                ngayNhanPhong   = viDateFormat.parse(b.edtNgayNhan.text.toString()) ?: dp.ngayNhanPhong,
                ngayTraPhong    = b.edtNgayTra.text.toString()
                    .takeIf { it.isNotBlank() }?.let { viDateFormat.parse(it) },
                tinhTrangDatPhong = b.edtTinhTrang.text.toString(),
                ghiChu          = b.edtGhiChu.text.toString()
            )
            vm.updateBookingFull(updated)
            isSaved = true
        }

        val dialog = AlertDialog.Builder(requireContext())
            .setView(b.root)
            .create()

        b.btnExit.setOnClickListener {
            fun Date.parse(text: String) =
                try { viDateFormat.parse(text) ?: this } catch (_:Exception) { this }

            val curr = dp.copy(
                tenKhach         = b.edtTenKhach.text.toString(),
                soCCCD           = b.edtSoCCCD.text.toString(),
                soDienThoai      = b.edtSoDT.text.toString(),
                ngayDatPhong     = dp.ngayDatPhong.parse(b.edtNgayDat.text.toString()),
                ngayNhanPhong    = dp.ngayNhanPhong.parse(b.edtNgayNhan.text.toString()),
                ngayTraPhong     = if (b.edtNgayTra.text.isNullOrBlank())
                                    null else dp.ngayTraPhong?.parse(b.edtNgayTra.text.toString()),
                ghiChu           = b.edtGhiChu.text.toString()
            )

            val changed = curr != dp

            if (isSaved || !changed) {
                dialog.dismiss()
            } else {
                AlertDialog.Builder(requireContext())
                    .setMessage("Bạn có muốn lưu thay đổi trước khi thoát?")
                    .setPositiveButton("Có") { _, _ ->
                        vm.updateBookingFull(curr)
                        dialog.dismiss()
                    }
                    .setNegativeButton("Không") { _, _ -> dialog.dismiss() }
                    .show()
            }
        }
        dialog.show()
    }
}
