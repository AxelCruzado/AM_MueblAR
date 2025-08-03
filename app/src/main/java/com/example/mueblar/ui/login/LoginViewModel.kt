package com.example.mueblar.ui.login

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.mueblar.data.validator.Validators
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class LoginViewModel : ViewModel() {

    private val auth = FirebaseAuth.getInstance()
    private val db = FirebaseFirestore.getInstance()

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> get() = _isLoading

    private val _loginResult = MutableLiveData<Quadruple<Boolean, String, String?, String?>?>()
    val loginResult: LiveData<Quadruple<Boolean, String, String?, String?>?> get() = _loginResult

    private val _resetPasswordResult = MutableLiveData<Pair<Boolean, String>?>()
    val resetPasswordResult: LiveData<Pair<Boolean, String>?> get() = _resetPasswordResult

    data class Quadruple<A, B, C, D>(val first: A, val second: B, val third: C, val fourth: D)

    fun loginUser(email: String, password: String) {
        if (!Validators.isValidEmail(email)) {
            _loginResult.value = Quadruple(false, "Correo inválido", null, null)
            return
        }

        if (password.length < 6) {
            _loginResult.value = Quadruple(false, "La contraseña debe tener al menos 6 caracteres", null, null)
            return
        }

        _isLoading.value = true
        auth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val uid = auth.currentUser?.uid
                    if (uid != null) {
                        db.collection("clientes").document(uid).get()
                            .addOnSuccessListener { document ->
                                if (document.exists()) {
                                    val tipoUsuario = document.getString("tipo_usuario") ?: ""
                                    _loginResult.value = Quadruple(true, "Login exitoso", tipoUsuario, null)
                                } else {
                                    db.collection("empresas").document(uid).get()
                                        .addOnSuccessListener { empresaDoc ->
                                            if (empresaDoc.exists()) {
                                                val tipoUsuario = empresaDoc.getString("tipo_usuario") ?: ""
                                                val estado = empresaDoc.getString("estado") ?: ""
                                                _loginResult.value = Quadruple(true, "Login exitoso", tipoUsuario, estado)
                                            } else {
                                                _loginResult.value = Quadruple(false, "No se encontró al usuario", null, null)
                                            }
                                            _isLoading.value = false
                                        }
                                        .addOnFailureListener { e ->
                                            _loginResult.value = Quadruple(false, "Error al obtener datos de empresa: ${e.message}", null, null)
                                            _isLoading.value = false
                                        }
                                }
                                _isLoading.value = false
                            }
                            .addOnFailureListener { e ->
                                _loginResult.value = Quadruple(false, "Error al obtener datos de cliente: ${e.message}", null, null)
                                _isLoading.value = false
                            }
                    } else {
                        _loginResult.value = Quadruple(false, "UID inválido", null, null)
                        _isLoading.value = false
                    }
                } else {
                    _loginResult.value = Quadruple(false, "Error: ${task.exception?.message}", null, null)
                    _isLoading.value = false
                }
            }
    }

    fun resetPassword(email: String) {
        if (!Validators.isValidEmail(email)) {
            _resetPasswordResult.value = Pair(false, "Correo inválido")
            return
        }

        _isLoading.value = true
        auth.sendPasswordResetEmail(email)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    _resetPasswordResult.value = Pair(true, "Se ha enviado un enlace de restablecimiento a tu correo")
                } else {
                    _resetPasswordResult.value = Pair(false, "Error: ${task.exception?.message}")
                }
                _isLoading.value = false
            }
    }

    fun clearLoginResult() {
        _loginResult.value = null
    }

    fun clearResetPasswordResult() {
        _resetPasswordResult.value = null
    }
}