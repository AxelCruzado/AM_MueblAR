package com.example.mueblar.ui.empresa

import android.app.Dialog
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.DialogFragment
import com.example.mueblar.data.model.Categoria
import com.example.mueblar.data.model.Producto
import com.example.mueblar.data.repository.CategoriaRepository
import com.example.mueblar.databinding.DialogAgregarProductoBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import okhttp3.*
import org.json.JSONObject
import java.io.IOException
import java.util.*
import okhttp3.MediaType.Companion.toMediaType

class AgregarProductoDialogFragment : DialogFragment() {

    private lateinit var binding: DialogAgregarProductoBinding
    private var categorias: List<Categoria> = emptyList()
    private var categoriaSeleccionada: Categoria? = null

    private var uriImagenSeleccionada: Uri? = null
    private var uriModeloSeleccionado: Uri? = null

    private val PICK_IMAGEN = 1001
    private val PICK_MODELO = 1002

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        binding = DialogAgregarProductoBinding.inflate(LayoutInflater.from(requireContext()))

        CategoriaRepository().obtenerCategorias { lista ->
            categorias = lista
            val nombres = lista.map { it.nombre }

            val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, nombres)
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            binding.spinnerCategoria.adapter = adapter

            binding.spinnerCategoria.setSelection(0)
            categoriaSeleccionada = categorias.firstOrNull()
        }

        binding.btnSeleccionarImagen.setOnClickListener {
            val intent = Intent(Intent.ACTION_GET_CONTENT)
            intent.type = "image/*"
            startActivityForResult(Intent.createChooser(intent, "Selecciona una imagen"), PICK_IMAGEN)
        }

        binding.btnSeleccionarModelo.setOnClickListener {
            val intent = Intent(Intent.ACTION_GET_CONTENT)
            intent.type = "*/*"
            val mimeTypes = arrayOf("model/gltf-binary", "application/octet-stream")
            intent.putExtra(Intent.EXTRA_MIME_TYPES, mimeTypes)
            startActivityForResult(Intent.createChooser(intent, "Selecciona modelo .glb"), PICK_MODELO)
        }

        return AlertDialog.Builder(requireContext())
            .setTitle("Agregar producto")
            .setView(binding.root)
            .setNegativeButton("Cancelar") { _, _ -> dismiss() }
            .setPositiveButton("Guardar", null)
            .create().apply {
                setOnShowListener {
                    val boton = getButton(AlertDialog.BUTTON_POSITIVE)
                    boton.setOnClickListener {
                        val nombre = binding.etNombre.text.toString().trim()
                        val descripcion = binding.etDescripcion.text.toString().trim()
                        val precio = binding.etPrecio.text.toString().toDoubleOrNull()
                        val stock = binding.etStock.text.toString().toIntOrNull()
                        val categoria = categorias.getOrNull(binding.spinnerCategoria.selectedItemPosition)

                        if (nombre.isEmpty() || precio == null || stock == null || categoria == null) {
                            binding.etNombre.error = if (nombre.isEmpty()) "Requerido" else null
                            Toast.makeText(requireContext(), "Completa todos los campos", Toast.LENGTH_SHORT).show()
                            return@setOnClickListener
                        }

                        if (uriImagenSeleccionada == null || uriModeloSeleccionado == null) {
                            Toast.makeText(requireContext(), "Selecciona imagen y modelo 3D", Toast.LENGTH_SHORT).show()
                            return@setOnClickListener
                        }
                        binding.progressBar.visibility = View.VISIBLE
                        boton.isEnabled = false
                        getButton(AlertDialog.BUTTON_NEGATIVE).isEnabled = false
                        val uid = FirebaseAuth.getInstance().currentUser?.uid ?: return@setOnClickListener
                        val storage = FirebaseStorage.getInstance().reference
                        val firestore = FirebaseFirestore.getInstance()

                        val nombreArchivoImg = "imagen/${UUID.randomUUID()}.jpg"
                        val nombreArchivoGlb = "modelo/${UUID.randomUUID()}.glb"

                        val imagenRef = storage.child(nombreArchivoImg)
                        val modeloRef = storage.child(nombreArchivoGlb)

                        imagenRef.putFile(uriImagenSeleccionada!!)
                            .addOnSuccessListener {
                                imagenRef.downloadUrl.addOnSuccessListener { uriImagen ->

                                    modeloRef.putFile(uriModeloSeleccionado!!)
                                        .addOnSuccessListener {
                                            modeloRef.downloadUrl.addOnSuccessListener { uriModelo ->

                                                val producto = Producto(
                                                    empresaId = uid,
                                                    nombre = nombre,
                                                    descripcion = descripcion,
                                                    precio = precio,
                                                    imagenUrl = uriImagen.toString(),
                                                    modeloArUrl = uriModelo.toString(),
                                                    categoriaId = categoria.id,
                                                    disponible = true,
                                                    stock = stock,
                                                    fechaCreacion = System.currentTimeMillis()
                                                )

                                                firestore.collection("productos")
                                                    .add(producto)
                                                    .addOnSuccessListener { doc ->
                                                        Toast.makeText(requireContext(), "Producto guardado", Toast.LENGTH_SHORT).show()
                                                        enviarNotificacionNuevoProducto(doc.id, nombre)
                                                        dismiss()
                                                    }
                                                    .addOnFailureListener {
                                                        Toast.makeText(requireContext(), "Error al guardar producto", Toast.LENGTH_SHORT).show()
                                                    }

                                            }
                                        }
                                        .addOnFailureListener {
                                            Toast.makeText(requireContext(), "Error al subir modelo", Toast.LENGTH_SHORT).show()
                                        }

                                }
                            }
                            .addOnFailureListener {
                                Toast.makeText(requireContext(), "Error al subir imagen", Toast.LENGTH_SHORT).show()
                            }
                    }
                }
            }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == AppCompatActivity.RESULT_OK) {
            when (requestCode) {
                PICK_IMAGEN -> {
                    uriImagenSeleccionada = data?.data
                    Toast.makeText(requireContext(), "Imagen seleccionada", Toast.LENGTH_SHORT).show()
                }
                PICK_MODELO -> {
                    uriModeloSeleccionado = data?.data
                    Toast.makeText(requireContext(), "Modelo seleccionado", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun enviarNotificacionNuevoProducto(productoId: String, nombre: String) {
        val client = OkHttpClient()

        val json = JSONObject().apply {
            put("productoId", productoId)
            put("nombreProducto", nombre)
        }

        val body = RequestBody.create(
            "application/json; charset=utf-8".toMediaType(),
            json.toString()
        )

        val request = Request.Builder()
            .url("https://enviarnotificacionnuevoproducto-oumxv53uzq-uc.a.run.app")
            .post(body)
            .addHeader("Content-Type", "application/json")
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                requireActivity().runOnUiThread {
                    android.util.Log.e("FCM", "Error al enviar notificación: ${e.message}", e)
                    Toast.makeText(requireContext(), "Error al enviar notificación", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onResponse(call: Call, response: Response) {
                requireActivity().runOnUiThread {
                    if (response.isSuccessful) {
                        android.util.Log.d("FCM", "Notificación enviada exitosamente")
                        Toast.makeText(requireContext(), "Notificación enviada", Toast.LENGTH_SHORT).show()
                    } else {
                        android.util.Log.e("FCM", "Error en la respuesta: ${response.code} ${response.message}")
                        Toast.makeText(requireContext(), "Error al enviar notificación: ${response.message}", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        })
    }
}