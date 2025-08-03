package com.example.mueblar.data.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class Producto(
    val productoId: String = "", // ID único del documento en Firestore
    val empresaId: String = "", // UID del usuario/empresa que lo sube
    val nombre: String = "",
    val descripcion: String = "",
    val precio: Double = 0.0,
    val imagenUrl: String = "", // Imagen del producto
    val modeloArUrl: String = "", // URL del modelo 3D para AR
    val categoriaId: String = "",
    val disponible: Boolean = true,
    val fechaCreacion: Long = System.currentTimeMillis(), // en milisegundos
    val stock: Int = 1, // opcional, si quieres manejar cantidad
    var isFavorite: Boolean = false // Estado de favorito
) : Parcelable