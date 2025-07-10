package com.example.mueblar.data.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class Producto(
    val empresaId: String = "", // UID del usuario/empresa que lo sube
    val nombre: String = "",
    val descripcion: String = "",
    val precio: Double = 0.0,
    val imagenUrl: String = "", // Imagen del producto
    val modeloArUrl: String = "", // URL del modelo 3D para AR
    val categoria: String = "", // opcional
    val disponible: Boolean = true,
    val fechaCreacion: Long = System.currentTimeMillis(), // en milisegundos
    val stock: Int = 1 // opcional, si quieres manejar cantidad
) : Parcelable