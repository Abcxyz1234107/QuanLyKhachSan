package com.example.quanlykhachsan.view.payment

import android.app.DatePickerDialog
import android.content.Context
import android.content.res.Resources
import android.os.Bundle
import android.text.InputType
import android.view.MotionEvent
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.ArrayAdapter
import android.widget.LinearLayout
import android.widget.NumberPicker
import androidx.fragment.app.Fragment
import android.widget.Toast
import androidx.core.widget.doOnTextChanged
import androidx.fragment.app.viewModels
import androidx.lifecycle.LiveData
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.quanlykhachsan.R
import com.example.quanlykhachsan.databinding.DialogPaymentDetailBinding
import com.example.quanlykhachsan.databinding.FragmentPaymentBinding
import com.example.quanlykhachsan.viewmodel.PaymentViewModel
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.textfield.TextInputEditText
import dagger.hilt.android.AndroidEntryPoint
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

@AndroidEntryPoint
class PaymentFragment : Fragment(R.layout.fragment_payment) {

    private var _binding: FragmentPaymentBinding? = null
    private val binding get() = _binding!!
    private val viewModel by viewModels<PaymentViewModel>()
    private val adapter  = PaymentAdapter()
    private val sdf = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
    private var originList: List<PaymentViewModel.PaymentItem> = emptyList()


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentPaymentBinding.bind(view)

        /* ---------- Tự fill ngày trả phòng ---------- */
        viewModel.suggestedDate.observe(viewLifecycleOwner) { dateStr ->
            if (dateStr.isNotEmpty()) binding.edtDateIn.setText(dateStr)
        }
        binding.edtRoomId.setOnFocusChangeListener { _, hasFocus ->
            if (!hasFocus) {
                val idStr = binding.edtRoomId.text.toString()
                viewModel.loadCheckoutDate(idStr, bookingIdStr = null)
            }
        }

        /* ---------- DatePicker spinner ---------- */
        setUpDatePicker(binding.edtDateOut)
        setUpDatePicker(binding.edtDateIn)

        /* RecyclerView */
        binding.rvBook.apply {
            layoutManager = LinearLayoutManager(context)
            adapter       = this@PaymentFragment.adapter
        }

        adapter.onItemClick = { item ->
            if (item == null) clearInputs()
            else {
                binding.edtRoomId.setText(item.roomId.toString())
                binding.edtDateOut.setText(item.paymentDate)
                viewModel.loadCheckoutDate(item.roomId.toString(), item.maDatPhong.toString())
                binding.edtPaymentType.setText(item.paymentType, false)
            }
            viewModel.setSelected(item)
        }

        /* hình thức thanh toán */
        val payTypes = listOf("Thẻ tín dụng", "Chuyển khoản", "Tiền mặt")
        binding.edtPaymentType.setSimpleItems(payTypes.toTypedArray())

        /* Lọc */
        binding.edtFilterPaymentType.setSimpleItems(payTypes.toTypedArray())
        viewModel.payments.observe(viewLifecycleOwner) { list ->
            originList = list
            applyFilter()
        }
        binding.cbFilter.setOnCheckedChangeListener { _, _ -> applyFilter() }
        binding.edtFilterPaymentType.doOnTextChanged { _, _, _, _ -> applyFilter() }
        binding.edtFilterTotalPayment.doOnTextChanged  { _, _, _, _ -> applyFilter() }

        // Observe data và notifications
        viewModel.payments.observe(viewLifecycleOwner) { adapter.submitList(it) }
        viewModel.notification.observe(viewLifecycleOwner) { msg ->
            Toast.makeText(requireContext(), msg, Toast.LENGTH_SHORT).show()
        }

        /** ---------- Nút THÊM ---------- */
        binding.btnAdd.setOnClickListener {
            val roomId  = binding.edtRoomId.text.toString()
            val payType = binding.edtPaymentType.text.toString()
            val payDate = sdf.parse(binding.edtDateIn.text.toString())
            if (payDate != null) {
                viewModel.addPayment(roomId, payType, payDate)
            }
        }
        /** ---------- Nút SỬA ---------- */
        binding.btnEdit.setOnClickListener {
            val type = binding.edtPaymentType.text.toString()
            val date = sdf.parse(binding.edtDateOut.text.toString())
            val note = ""
            if (date != null) viewModel.editPayment(type, date, note)
        }
        /** ---------- Nút XOÁ, HỦY---------- */
        binding.btnDelete.setOnClickListener { confirmDelete(viewModel.selected) }
        binding.btnCancel.setOnClickListener { confirmCancel(viewModel.selected) }
        /** ---------- Nút TRẢ PHÒNG ---------- */
        binding.btnPay.setOnClickListener   { viewModel.payRoom() }
        /** ---------- Nút CHI TIẾT ---------- */
        binding.btnDetail.setOnClickListener { openPaymentDetail() }

        /* Tự động cập nhật ngày thanh toán */
        binding.edtDateOut.setText(java.text.SimpleDateFormat("dd/MM/yyyy")
            .format(java.util.Date()))

        /** ------------------------------- Hàm bỏ focus & ẩn bàn phím ------------------------------- */
        fun clearFocusAndHideKeyboard() {
            // Form Trả phòng
            binding.edtRoomId.clearFocus()
            binding.edtPaymentType.clearFocus()
            binding.edtDateOut.clearFocus()
            binding.edtDateIn.clearFocus()
            // Phần Lọc
            binding.edtFilterPaymentType.clearFocus()
            binding.edtFilterTotalPayment.clearFocus()

            // Ẩn bàn phím
            val imm = context
                ?.getSystemService(Context.INPUT_METHOD_SERVICE) as? InputMethodManager
            imm?.hideSoftInputFromWindow(binding.root.windowToken, 0)
        }

        // Chạm ngoài form sẽ clear focus
        binding.formContainer.setOnTouchListener { _, event ->
            if (event.action == MotionEvent.ACTION_DOWN) {
                clearFocusAndHideKeyboard()
            }
            false
        }
        binding.root.setOnTouchListener { _, event ->
            if (event.action == MotionEvent.ACTION_DOWN) {
                clearFocusAndHideKeyboard()
            }
            false
        }
    }

    private fun setUpDatePicker(
        edt: TextInputEditText,
        onDateSelected: ((Date) -> Unit)? = null
    ) {
        attachSpinner(edt, null, onDateSelected)
    }
    /* —— dùng chung attachSpinner với dialog —— */
    private fun attachSpinner(
        edt: TextInputEditText,
        init: Date?,
        onDateSelected: ((Date) -> Unit)? = null
    ) {
        edt.inputType = InputType.TYPE_NULL
        edt.setOnClickListener {
            val cal = Calendar.getInstance().apply { time = init ?: Date() }
            val dlg = DatePickerDialog(
                requireContext(),
                R.style.ThemeOverlay_App_DatePicker_Spinner,
                null,
                cal[Calendar.YEAR], cal[Calendar.MONTH], cal[Calendar.DAY_OF_MONTH]
            ).apply { datePicker.calendarViewShown = false }
            dlg.setOnShowListener {
                val sys   = Resources.getSystem()
                val day   = dlg.datePicker.findViewById<NumberPicker>(
                    sys.getIdentifier("day","id","android")
                )
                val month = dlg.datePicker.findViewById<NumberPicker>(
                    sys.getIdentifier("month","id","android")
                )
                val year  = dlg.datePicker.findViewById<NumberPicker>(
                    sys.getIdentifier("year","id","android")
                )
                (day.parent as? LinearLayout)?.apply {
                    removeAllViews(); addView(day); addView(month); addView(year)
                }
                val viMonths = arrayOf(
                    "Tháng 1","Tháng 2","Tháng 3","Tháng 4","Tháng 5","Tháng 6",
                    "Tháng 7","Tháng 8","Tháng 9","Tháng 10","Tháng 11","Tháng 12"
                )
                month.displayedValues = viMonths
                fun update() {
                    cal.set(year.value, month.value, day.value)
                    edt.setText(sdf.format(cal.time))
                    onDateSelected?.invoke(cal.time)
                }
                day.setOnValueChangedListener   { _,_,_ -> update() }
                month.setOnValueChangedListener { _,_,_ -> update() }
                year.setOnValueChangedListener  { _,_,_ -> update() }
                update()
            }
            dlg.show()
        }
    }

    override fun onDestroyView() {
        _binding = null
        super.onDestroyView()
    }
    private fun clearInputs() {
        binding.edtRoomId.setText("")
        binding.edtPaymentType.setText("", false)
        binding.edtDateOut.setText("")
        binding.edtDateIn.setText("")
    }

    private fun openPaymentDetail() {
        val item = viewModel.selected.value
        if (item == null) {
            Toast.makeText(requireContext(), "Chưa chọn đơn trả phòng!", Toast.LENGTH_SHORT).show()
            return
        }

        val b = DialogPaymentDetailBinding.inflate(layoutInflater)
        val payTypes = arrayOf("Thẻ tín dụng", "Chuyển khoản", "Tiền mặt")

        /* ---------- Đổ dữ liệu gốc ---------- */
        b.tvRoomHeader.text = "Phòng ${item.roomId}"
        b.tvCustomer.text   = "Khách hàng: ${item.customer}"
        b.edtTongTien.setText("%,d ₫".format(item.total.toLong()))
        b.edtPaymentType.setSimpleItems(payTypes)
        b.edtPaymentType.setText(item.paymentType, false)
        b.edtGhiChu.setText(item.note)

        b.edtNgayThanhToan.setText(item.paymentDate)
        attachSpinner(b.edtNgayThanhToan, sdf.parse(item.paymentDate))

        var isSaved = false

        // Lưu
        b.btnSave.setOnClickListener {
            val type = b.edtPaymentType.text.toString()
            val date = runCatching { sdf.parse(b.edtNgayThanhToan.text.toString()) }.getOrNull()
            val note = b.edtGhiChu.text.toString()
            if (date == null) {
                Toast.makeText(requireContext(), "Ngày thanh toán không hợp lệ!", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            viewModel.editPayment(type, date, note)
            isSaved = true
        }

        val dlg = MaterialAlertDialogBuilder(requireContext())
            .setView(b.root)
            .setCancelable(false)
            .create()

        // Thoát
        b.btnExit.setOnClickListener {
            val curType = b.edtPaymentType.text.toString()
            val curDate = runCatching { sdf.parse(b.edtNgayThanhToan.text.toString()) }.getOrNull()
            val curNote = b.edtGhiChu.text.toString()

            val changed =
                curType != item.paymentType ||
                        curNote.isNotBlank() ||
                        (curDate != null && sdf.format(curDate) != item.paymentDate)

            if (isSaved || !changed) {
                dlg.dismiss()
            } else {
                androidx.appcompat.app.AlertDialog.Builder(requireContext())
                    .setMessage("Bạn có muốn lưu thay đổi trước khi thoát?")
                    .setPositiveButton("Có") { _, _ ->
                        if (curDate != null) viewModel.editPayment(curType, curDate, curNote)
                        dlg.dismiss()
                    }
                    .setNegativeButton("Không") { _, _ -> dlg.dismiss() }
                    .show()
            }
        }
        dlg.show()
    }
    private fun confirmDelete(dp: LiveData<PaymentViewModel.PaymentItem?>) {
        androidx.appcompat.app.AlertDialog.Builder(requireContext())
            .setTitle("Xóa đơn trả phòng #${dp.value?.traPhongId}?")
            .setMessage("Bạn chắc chắn muốn xoá đơn trả phòng này?")
            .setPositiveButton("Xóa") { _, _ -> viewModel.deletePayment() }
            .setNegativeButton("Hủy", null)
            .show()
    }
    private fun confirmCancel(dp: LiveData<PaymentViewModel.PaymentItem?>) {
        androidx.appcompat.app.AlertDialog.Builder(requireContext())
            .setTitle("Hủy đơn trả phòng #${dp.value?.traPhongId}?")
            .setMessage("Bạn chắc chắn muốn hủy đơn trả phòng này?")
            .setPositiveButton("Xóa") { _, _ -> viewModel.cancelPayment() }
            .setNegativeButton("Hủy", null)
            .show()
    }
    private fun applyFilter() {
        var data = originList
        if (binding.cbFilter.isChecked) {
            val type = binding.edtFilterPaymentType.text.toString().trim()
            if (type.isNotEmpty())
                data = data.filter { it.paymentType.contains(type, ignoreCase = true) }

            val minTotal = binding.edtFilterTotalPayment.text.toString().toIntOrNull()
            if (minTotal != null) data = data.filter { it.roomId == minTotal }
        }
        adapter.submitList(data)
    }
}