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

class ClientProductsViewModel : ViewModel() {

    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    private val _productos = MutableLiveData<List<Producto>>()
    val productos: LiveData<List<Producto>> get() = _productos

    private val _filteredProductos = MutableLiveData<List<Producto>>()
    val filteredProductos: LiveData<List<Producto>> get() = _filteredProductos

    private val _error = MutableLiveData<String>()
    val error: LiveData<String> get() = _error

    private val _favoriteChanged = MutableLiveData<Pair<String, Boolean>>()
    val favoriteChanged: LiveData<Pair<String, Boolean>> get() = _favoriteChanged

    init {
        loadProductos()
        loadFavoritos()
    }

    private fun loadProductos() {
        viewModelScope.launch {
            try {
                val snapshot = db.collection("productos").get().await()
                val productosList = snapshot.documents.mapNotNull { doc ->
                    doc.toObject(Producto::class.java)?.copy(productoId = doc.id, isFavorite = false)
                }
                _productos.value = productosList
                _filteredProductos.value = productosList
            } catch (e: Exception) {
                _error.value = "Error al cargar productos: ${e.message}"
            }
        }
    }

    private fun loadFavoritos() {
        viewModelScope.launch {
            val userId = auth.currentUser?.uid ?: return@launch
            try {
                val snapshot = db.collection("favoritos")
                    .document(userId)
                    .collection("productos")
                    .get()
                    .await()
                val favoritosIds = snapshot.documents.map { it.id }
                _productos.value?.let { productos ->
                    val updatedProductos = productos.map { producto ->
                        producto.copy(isFavorite = favoritosIds.contains(producto.productoId))
                    }
                    _productos.value = updatedProductos
                    _filteredProductos.value = updatedProductos
                }
            } catch (e: Exception) {
                _error.value = "Error al cargar favoritos: ${e.message}"
            }
        }
    }

    fun filterProductos(query: String) {
        val productosList = _productos.value ?: return
        _filteredProductos.value = if (query.isEmpty()) {
            productosList
        } else {
            productosList.filter {
                it.nombre.contains(query, ignoreCase = true) ||
                        it.categoriaId.contains(query, ignoreCase = true)
            }
        }
    }

    fun toggleFavorite(producto: Producto, isFavorite: Boolean) {
        val userId = auth.currentUser?.uid ?: return
        viewModelScope.launch {
            try {
                val favoritosRef = db.collection("favoritos")
                    .document(userId)
                    .collection("productos")
                    .document(producto.productoId)

                if (isFavorite) {
                    favoritosRef.set(producto.copy(isFavorite = true)).await()
                } else {
                    favoritosRef.delete().await()
                }

                // Notificar cambio de favorito
                _favoriteChanged.value = Pair(producto.productoId, isFavorite)

                // Actualizar estado local
                _productos.value?.let { productos ->
                    val updatedProductos = productos.map {
                        if (it.productoId == producto.productoId) it.copy(isFavorite = isFavorite)
                        else it
                    }
                    _productos.value = updatedProductos
                    _filteredProductos.value = updatedProductos.filter {
                        if (_filteredProductos.value == productos) true
                        else it.nombre.contains(_filteredProductos.value?.joinToString { it.nombre } ?: "", ignoreCase = true) ||
                                it.categoriaId.contains(_filteredProductos.value?.joinToString { it.categoriaId } ?: "", ignoreCase = true)
                    }
                }
            } catch (e: Exception) {
                _error.value = "Error al actualizar favorito: ${e.message}"
            }
        }
    }

    fun updateFavoriteStatus(productoId: String, isFavorite: Boolean) {
        _productos.value?.let { productos ->
            val updatedProductos = productos.map {
                if (it.productoId == productoId) it.copy(isFavorite = isFavorite)
                else it
            }
            _productos.value = updatedProductos
            _filteredProductos.value = updatedProductos.filter {
                if (_filteredProductos.value == productos) true
                else it.nombre.contains(_filteredProductos.value?.joinToString { it.nombre } ?: "", ignoreCase = true) ||
                        it.categoriaId.contains(_filteredProductos.value?.joinToString { it.categoriaId } ?: "", ignoreCase = true)
            }
        }
    }
}