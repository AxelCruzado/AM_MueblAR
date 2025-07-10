package com.example.mueblar.ui.client

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.mueblar.data.model.Producto
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query

class ClientHomeViewModel : ViewModel() {

    private val db = FirebaseFirestore.getInstance()

    private val _productosDestacados = MutableLiveData<List<Producto>>()
    val productosDestacados: LiveData<List<Producto>> get() = _productosDestacados

    init {
        obtenerProductosDestacados()
    }

    private fun obtenerProductosDestacados() {
        db.collection("productos")
            .whereEqualTo("disponible", true)
            .orderBy("fechaCreacion", Query.Direction.DESCENDING)
            .limit(10)
            .get()
            .addOnSuccessListener { result ->
                val productos = result.toObjects(Producto::class.java)


                Log.d("Productos", productos.toString())

                _productosDestacados.value = productos
            }
            .addOnFailureListener { exception ->
                _productosDestacados.value = emptyList()
            }
    }

}
