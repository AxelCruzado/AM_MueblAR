package com.example.mueblar.ui.client

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.mueblar.data.model.Producto
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class ClientFavoritoViewModel : ViewModel() {

    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    private val _favoritos = MutableLiveData<List<Producto>>()
    val favoritos: LiveData<List<Producto>> get() = _favoritos

    private val _error = MutableLiveData<String>()
    val error: LiveData<String> get() = _error

    private val _favoriteChanged = MutableLiveData<Pair<String, Boolean>>()
    val favoriteChanged: LiveData<Pair<String, Boolean>> get() = _favoriteChanged

    init {
        loadFavoritos()
    }

    fun loadFavoritos() {
        viewModelScope.launch {
            val userId = auth.currentUser?.uid ?: return@launch
            try {
                val snapshot = db.collection("favoritos")
                    .document(userId)
                    .collection("productos")
                    .get()
                    .await()
                val favoritosList = snapshot.documents.mapNotNull { doc ->
                    doc.toObject(Producto::class.java)?.copy(productoId = doc.id, isFavorite = true)
                }
                _favoritos.value = favoritosList
            } catch (e: Exception) {
                _error.value = "Error al cargar favoritos: ${e.message}"
            }
        }
    }

    fun removeFavorite(producto: Producto) {
        val userId = auth.currentUser?.uid ?: return
        viewModelScope.launch {
            try {
                db.collection("favoritos")
                    .document(userId)
                    .collection("productos")
                    .document(producto.productoId)
                    .delete()
                    .await()

                // Notificar cambio de favorito
                _favoriteChanged.value = Pair(producto.productoId, false)

                // Actualizar lista local
                _favoritos.value = _favoritos.value?.filter { it.productoId != producto.productoId }
            } catch (e: Exception) {
                _error.value = "Error al eliminar favorito: ${e.message}"
            }
        }
    }
}