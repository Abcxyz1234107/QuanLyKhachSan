package com.example.quanlykhachsan.view.book

import android.app.DatePickerDialog
import android.os.Bundle
import androidx.recyclerview.widget.LinearLayoutManager
import android.text.InputType
import android.view.View
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import com.example.quanlykhachsan.R
import com.example.quanlykhachsan.data.local.entity.DatPhong
import com.example.quanlykhachsan.databinding.FragmentBookBinding
import com.example.quanlykhachsan.viewmodel.BookViewModel
import com.google.android.material.textfield.TextInputEditText
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.*

class BookFragment : Fragment(R.layout.fragment_book) {

    private var selectedBooking: DatPhong? = null
    private var _bd: FragmentBookBinding? = null
    private val binding get() = _bd!!
    private val bd get() = _bd!!
    private val vm by viewModels<BookViewModel>()

    private val sdf = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _bd = FragmentBookBinding.bind(view)

        /* ---------- RecyclerView ---------- */
        val adapter = BookAdapter { booking ->selectedBooking = booking}
        bd.rvBook.layoutManager = LinearLayoutManager(requireContext())
        bd.rvBook.adapter = adapter
        vm.bookings.observe(viewLifecycleOwner) { list -> adapter.submit(list) }

        /* ---------- Nút Nhận phòng ---------- */
        bd.btnReceive.setOnClickListener {
            val booking = selectedBooking
            if (booking == null) {
                Toast.makeText(requireContext(), "Hãy chọn một đơn đặt phòng trước!", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            vm.receiveBooking(booking)
        }

        /* ---------- DatePicker spinner ---------- */
        setUpDatePicker(bd.edtDateIn)
        setUpDatePicker(bd.edtDateOut)

        /* ---------- Load tên loại phòng vào dropdown ---------- */
        vm.roomTypeNames.observe(viewLifecycleOwner) { names ->
            val adapter = ArrayAdapter(requireContext(),
                android.R.layout.simple_list_item_1, names)
            bd.actvRoomType.setAdapter(adapter)
        }

        /* ---------- Xử lý nút Thêm ---------- */
        bd.btnAdd.setOnClickListener {
            val phone      = bd.edtCustomerPhone.text.toString()
            val roomType   = bd.actvRoomType.text.toString()
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

    private fun setUpDatePicker(edt: TextInputEditText) {
        edt.inputType = InputType.TYPE_NULL          // chặn bàn phím
        edt.setOnClickListener {
            val cal = Calendar.getInstance()
            DatePickerDialog(
                requireContext(),
                R.style.ThemeOverlay_App_DatePicker_Spinner,
                { _, y, m, d ->
                    cal.set(y, m, d)
                    edt.setText(sdf.format(cal.time))
                },
                cal[Calendar.YEAR], cal[Calendar.MONTH], cal[Calendar.DAY_OF_MONTH]
            ).apply { datePicker.calendarViewShown = false }   // ép dùng spinner
                .show()
        }
    }

    private fun parseDate(str: String): Date? =
        try { sdf.parse(str) } catch (e: ParseException) { null }

    override fun onDestroyView() {
        super.onDestroyView()
        _bd = null
    }
}
