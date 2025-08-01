package com.example.mueblar.ui.client

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.contract.ActivityResultContracts
import androidx.lifecycle.lifecycleScope
import com.bumptech.glide.Glide
import com.example.mueblar.R
import com.example.mueblar.databinding.FragmentClientPerfilBinding
import com.example.mueblar.ui.common.BaseFragment
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class ClientPerfilFragment : BaseFragment() {

    private var _binding: FragmentClientPerfilBinding? = null
    private val binding get() = _binding!!
    private val auth = FirebaseAuth.getInstance()
    private val db = FirebaseFirestore.getInstance()
    private val storage = FirebaseStorage.getInstance().reference.child("fotos")
    private var imageUri: Uri? = null
    private var isEditing = false
    private var currentImageUrl: String? = null

    private val galleryLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            result.data?.data?.let { uri ->
                imageUri = uri
                // Solo previsualización, no se sube aún
                Glide.with(this)
                    .load(uri)
                    .circleCrop()
                    .into(binding.ivProfile)
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentClientPerfilBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        cargarDatosUsuario()
        setupClickListeners()
        setEditMode(false) // Modo visualización inicial
    }

    private fun setupClickListeners() {
        binding.btnEditProfile.setOnClickListener { setEditMode(true) }
        binding.btnSaveProfile.setOnClickListener { guardarCambios() }
        binding.fabEditImage.setOnClickListener { abrirGaleria() }
        binding.btnCerrarSesion.setOnClickListener {
            auth.signOut()
            cerrarSesion()
        }
    }

    private fun cargarDatosUsuario() {
        val userId = auth.currentUser?.uid ?: return

        lifecycleScope.launch {
            try {
                val document = db.collection("clientes").document(userId).get().await()
                if (document.exists()) {
                    with(document) {
                        binding.etNombre.setText(getString("nombre"))
                        binding.etApellido.setText(getString("apellido"))
                        binding.etDni.setText(getString("dni"))
                        binding.etCorreo.setText(getString("correo"))
                        binding.etTelefono.setText(getString("telefono"))
                        binding.tvFechaRegistro.text = getString("fecha_registro")

                        // Cargar imagen si existe
                        currentImageUrl = getString("imagen")
                        currentImageUrl?.takeIf { it.isNotBlank() }?.let { url ->
                            Glide.with(this@ClientPerfilFragment)
                                .load(url)
                                .circleCrop()
                                .into(binding.ivProfile)
                        }

                        // Mostrar nombre completo
                        binding.tvNombreCompleto.text =
                            "${getString("nombre")} ${getString("apellido")}"
                    }
                }
            } catch (e: Exception) {
                Snackbar.make(binding.root, "Error al cargar datos", Snackbar.LENGTH_SHORT).show()
            }
        }
    }

    private fun setEditMode(editing: Boolean) {
        isEditing = editing
        binding.apply {
            btnEditProfile.visibility = if (editing) View.GONE else View.VISIBLE
            btnSaveProfile.visibility = if (editing) View.VISIBLE else View.GONE
            fabEditImage.visibility = if (editing) View.VISIBLE else View.GONE
            etNombre.isEnabled = editing
            etApellido.isEnabled = editing
            etTelefono.isEnabled = editing
        }
    }

    private fun abrirGaleria() {
        val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        galleryLauncher.launch(intent)
    }

    private fun guardarCambios() {
        val userId = auth.currentUser?.uid ?: return

        lifecycleScope.launch {
            try {
                mostrarProgreso(true)

                // 1. Subir imagen si hay una nueva
                val nuevaImagenUrl = imageUri?.let { subirImagen(userId) }

                // 2. Actualizar datos en Firestore
                actualizarDatosUsuario(userId, nuevaImagenUrl)

                // 3. Feedback y estado
                mostrarExito()
                setEditMode(false)

            } catch (e: Exception) {
                mostrarError(e)
            } finally {
                mostrarProgreso(false)
            }
        }
    }

    private fun mostrarProgreso(mostrar: Boolean) {
        binding.apply {
            progressBar.visibility = if (mostrar) View.VISIBLE else View.GONE
            btnSaveProfile.isEnabled = !mostrar
            btnSaveProfile.text = if (mostrar) "Guardando..." else "Guardar"
        }
    }

    private suspend fun actualizarDatosUsuario(userId: String, nuevaImagenUrl: String?) {
        val updates = hashMapOf<String, Any>().apply {
            put("nombre", binding.etNombre.text.toString())
            put("apellido", binding.etApellido.text.toString())
            put("telefono", binding.etTelefono.text.toString())
            nuevaImagenUrl?.let { put("imagen", it) }
        }

        db.collection("clientes").document(userId)
            .update(updates)
            .await()

        currentImageUrl = nuevaImagenUrl ?: currentImageUrl
    }

    private fun mostrarExito() {
        Snackbar.make(binding.root, "Perfil actualizado", Snackbar.LENGTH_SHORT).show()
    }

    private fun mostrarError(e: Exception) {
        Snackbar.make(binding.root,
            "Error al guardar: ${e.message ?: "Intente nuevamente"}",
            Snackbar.LENGTH_LONG).show()
    }

    private suspend fun subirImagen(userId: String): String {
        return try {
            val fileRef = storage.child("$userId-${System.currentTimeMillis()}.jpg")
            fileRef.putFile(imageUri!!).await()
            fileRef.downloadUrl.await().toString()
        } catch (e: Exception) {
            throw e // Re-lanzar para manejar en guardarCambios()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}