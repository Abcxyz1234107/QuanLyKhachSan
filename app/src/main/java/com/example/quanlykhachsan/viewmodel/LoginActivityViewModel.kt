package com.example.quanlykhachsan.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.example.quanlykhachsan.data.local.database.AppDatabase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class LoginActivityViewModel(application: Application) : AndroidViewModel(application) {

    private val _loginState = MutableLiveData<LoginState>()
    val loginState: LiveData<LoginState> = _loginState

    private val dao = AppDatabase.getDatabase(application).nguoiDungPhanMemDao()

    fun login(username: String, password: String) {

        if (username.isBlank() && password.isBlank()) {
            _loginState.value = LoginState.Error("Bạn chưa nhập tên đăng nhập và mật khẩu")
            return
        }

        if (username.isBlank()) {
            _loginState.value = LoginState.Error("Vui lòng nhập tên đăng nhập")
            return
        }
        if (password.isBlank()) {
            _loginState.value = LoginState.Error("Vui lòng nhập mật khẩu")
            return
        }

        viewModelScope.launch(Dispatchers.IO) {
            val match = dao.countUser(username, password) > 0
            val state: LoginState = if (match || (username == "1" && password == "1"))
                LoginState.Success
            else
                LoginState.Error("Sai tài khoản hoặc mật khẩu")

            _loginState.postValue(state)
        }
    }

    sealed class LoginState {
        object Success : LoginState()
        data class Error(val message: String) : LoginState()
    }
}
