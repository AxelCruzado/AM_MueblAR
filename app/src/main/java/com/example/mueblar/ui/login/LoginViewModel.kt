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

    private val _loginResult = MutableLiveData<Pair<Boolean, String>?>()
    val loginResult: LiveData<Pair<Boolean, String>?> get() = _loginResult

    fun loginUser(email: String, password: String) {
        if (!Validators.isValidEmail(email)) {
            _loginResult.value = Pair(false, "Correo inválido")
            return
        }

        if (password.length < 6) {
            _loginResult.value = Pair(false, "La contraseña debe tener al menos 6 caracteres")
            return
        }

        auth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val uid = auth.currentUser?.uid
                    if (uid != null) {
                        db.collection("usuarios").document(uid).get()
                            .addOnSuccessListener { document ->
                                if (document.exists()) {
                                    val tipo = document.getString("tipoUsuario") ?: ""
                                    _loginResult.value = Pair(true, tipo)
                                } else {
                                    _loginResult.value = Pair(false, "No se encontró al usuario")
                                }
                            }
                            .addOnFailureListener { e ->
                                _loginResult.value = Pair(false, "Error al obtener datos: ${e.message}")
                            }
                    } else {
                        _loginResult.value = Pair(false, "UID inválido")
                    }
                } else {
                    _loginResult.value = Pair(false, "Error: ${task.exception?.message}")
                }
            }
    }

    fun clearLoginResult() {
        _loginResult.value = null
    }
}
