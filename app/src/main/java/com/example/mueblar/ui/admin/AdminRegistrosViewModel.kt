package com.example.mueblar.ui.admin

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.mueblar.data.model.Empresa
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.QuerySnapshot
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class AdminRegistrosViewModel : ViewModel() {

    private val db = FirebaseFirestore.getInstance()
    private val _empresas = MutableLiveData<List<Empresa>>()
    val empresas: LiveData<List<Empresa>> = _empresas

    init {
        loadEmpresasPendientes()
    }

    fun loadEmpresasPendientes() {
        viewModelScope.launch {
            try {
                val snapshot: QuerySnapshot = db.collection("empresas")
                    .whereEqualTo("estado", "pendiente")
                    .get()
                    .await()
                val empresasList = snapshot.documents.mapNotNull { doc ->
                    doc.toObject(Empresa::class.java)?.copy(id = doc.id)
                }
                _empresas.value = empresasList
            } catch (e: Exception) {
                // Manejar error
                _empresas.value = emptyList()
            }
        }
    }

    fun actualizarEstadoEmpresa(empresa: Empresa, nuevoEstado: String) {
        viewModelScope.launch {
            try {
                db.collection("empresas").document(empresa.id)
                    .update("estado", nuevoEstado)
                    .await()
                // Recargar la lista tras la actualización
                loadEmpresasPendientes()
            } catch (e: Exception) {
                // Manejar error
            }
        }
    }
}