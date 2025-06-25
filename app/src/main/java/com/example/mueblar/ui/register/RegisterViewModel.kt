package com.example.mueblar.ui.register
import com.example.mueblar.data.validator.Validators
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.mueblar.data.model.User
import com.example.mueblar.data.repository.UserRepository

class RegisterViewModel : ViewModel() {
    private val userRepository = UserRepository()

    private val _registerResult = MutableLiveData<Pair<Boolean, String>>()
    val registerResult: LiveData<Pair<Boolean, String>> get() = _registerResult

    fun register(user: User, password: String) {
        if (user.tipoUsuario == "Administrador") {
            _registerResult.value = Pair(false, "No puedes registrarte como Administrador")
            return
        }

        if (!Validators.isValidEmail(user.correo)) {
            _registerResult.value = Pair(false, "Correo inválido")
            return
        }

        if (!Validators.isValidPhone(user.telefono)) {
            _registerResult.value = Pair(false, "Teléfono inválido")
            return
        }

        if (user.tipoUsuario == "Empresa" && !Validators.isValidRuc(user.ruc)) {
            _registerResult.value = Pair(false, "RUC inválido")
            return
        }

        userRepository.registerUser(user, password) { success, message ->
            _registerResult.value = Pair(success, message)
        }
    }
}