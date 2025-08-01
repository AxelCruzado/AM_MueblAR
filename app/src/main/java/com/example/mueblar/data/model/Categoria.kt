package com.example.mueblar.data.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class Categoria(
    val id: String = "",           // ID generado por Firestore (document ID)
    val nombre: String = "",       // Ej: "Comedor", "Sala", "Oficina"
    val descripcion: String = "",  // Opcional
    val iconoUrl: String = ""      // Opcional: ícono o imagen
) : Parcelable