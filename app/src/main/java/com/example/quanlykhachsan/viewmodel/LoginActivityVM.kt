package com.example.quanlykhachsan.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class LoginActivityVM : ViewModel() {

    /* LiveData mô tả trạng thái đăng nhập */
    private val _loginState = MutableLiveData<LoginState>()
    val loginState: LiveData<LoginState> = _loginState

    /* Hàm xử lý đăng nhập – không dùng “early-return” theo sở thích của bạn  */
    fun login(username: String, password: String) {
        var result: LoginState = LoginState.Success          // giả định thành công trước
        if (username.isBlank()) {
            result = LoginState.Error("Vui lòng nhập tên đăng nhập")
        } else if (password.isBlank()) {
            result = LoginState.Error("Vui lòng nhập mật khẩu")
        } else if (!(username == "admin" && password == "123456")) {
            result = LoginState.Error("Sai tài khoản hoặc mật khẩu")
        }
        _loginState.value = result
    }

    /* Định nghĩa các trạng thái UI */
    sealed class LoginState {
        object Success : LoginState()
        data class Error(val message: String) : LoginState()
    }
}
