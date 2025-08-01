package com.example.mueblar.data.repository

import com.example.mueblar.data.model.Categoria
import com.google.firebase.firestore.FirebaseFirestore

class CategoriaRepository {
    private val db = FirebaseFirestore.getInstance()

    fun obtenerCategorias(callback: (List<Categoria>) -> Unit) {
        db.collection("categorias")
            .get()
            .addOnSuccessListener { result ->
                val categorias = result.map { doc ->
                    Categoria(
                        id = doc.id,
                        nombre = doc.getString("nombre") ?: "",
                        descripcion = doc.getString("descripcion") ?: "",
                        iconoUrl = doc.getString("iconoUrl") ?: ""
                    )
                }
                callback(categorias)
            }
            .addOnFailureListener {
                callback(emptyList())
            }
    }
}
