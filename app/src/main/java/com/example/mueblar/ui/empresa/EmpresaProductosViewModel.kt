package com.example.mueblar.ui.empresa

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.mueblar.data.model.Producto
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration

class EmpresaProductosViewModel : ViewModel() {

    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    private val _productos = MutableLiveData<List<Producto>>()
    val productos: LiveData<List<Producto>> get() = _productos

    private val _mensaje = MutableLiveData<String?>()
    val mensaje: LiveData<String?> get() = _mensaje

    private var listenerRegistration: ListenerRegistration? = null

    fun escucharProductosEmpresa() {
        val empresaId = auth.currentUser?.uid ?: return

        listenerRegistration?.remove()

        listenerRegistration = db.collection("productos")
            .whereEqualTo("empresaId", empresaId)
            .orderBy("fechaCreacion")
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    _mensaje.value = "Error al escuchar productos: ${error.message}"
                    return@addSnapshotListener
                }

                val lista = snapshot?.documents?.mapNotNull { it.toObject(Producto::class.java) } ?: emptyList()
                _productos.value = lista

                if (lista.isEmpty()) {
                    _mensaje.value = "Aún no has registrado productos"
                } else {
                    _mensaje.value = null
                }
            }
    }

    override fun onCleared() {
        super.onCleared()
        listenerRegistration?.remove()
    }

    fun limpiarMensaje() {
        _mensaje.value = null
    }
}
