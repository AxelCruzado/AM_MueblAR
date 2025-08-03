package com.example.mueblar.data.model

data class Empresa(
    val imagen: String = "",
    val id: String = "",
    val nombre: String = "",
    val apellido: String = "",
    val correo: String = "",
    val telefono: String = "",
    val ruc: String = "",
    val dni: String = "",
    val tipo_usuario: String = "",
    val nombre_empresa: String = "", // Cambiado de nombreEmpresa a nombre_empresa
    val latitud: String = "",
    val longitud: String = "",
    val fecha_registro: String = "",
    val fecha_aprobacion: String = "",
    val estado: String = "",
    val fcmToken: String = ""
)