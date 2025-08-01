package com.example.mueblar.ui.empresa

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
import com.example.mueblar.databinding.FragmentEmpresaPerfilBinding
import com.example.mueblar.ui.common.BaseFragment
import com.example.mueblar.ui.ubicacion.MapaUbicacionActivity
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class EmpresaPerfilFragment : BaseFragment() {

    companion object {
        private const val REQUEST_CODE_UBICACION = 1001
    }

    private var _binding: FragmentEmpresaPerfilBinding? = null
    private val binding get() = _binding!!
    private val auth = FirebaseAuth.getInstance()
    private val db = FirebaseFirestore.getInstance()
    private val storage = FirebaseStorage.getInstance().reference.child("logos_empresas")
    private var imageUri: Uri? = null
    private var isEditing = false
    private var currentImageUrl: String? = null

    private val galleryLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            result.data?.data?.let { uri ->
                imageUri = uri
                Glide.with(this)
                    .load(uri)
                    .circleCrop()
                    .into(binding.ivEmpresaLogo)
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentEmpresaPerfilBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        cargarDatosEmpresa()
        setupClickListeners()
        setEditMode(false)
    }

    private fun setupClickListeners() {
        binding.btnEditProfile.setOnClickListener { setEditMode(true) }
        binding.btnSaveProfile.setOnClickListener { guardarCambios() }
        binding.fabEditLogo.setOnClickListener { abrirGaleria() }
        binding.btnCerrarSesion.setOnClickListener {
            auth.signOut()
            cerrarSesion()
        }
        binding.btnGetLocation.setOnClickListener { obtenerUbicacion() }
    }

    private fun obtenerUbicacion() {
        val intent = Intent(requireContext(), MapaUbicacionActivity::class.java)
        startActivityForResult(intent, REQUEST_CODE_UBICACION)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == REQUEST_CODE_UBICACION && resultCode == Activity.RESULT_OK) {
            val latitud = data?.getDoubleExtra("latitud", 0.0) ?: 0.0
            val longitud = data?.getDoubleExtra("longitud", 0.0) ?: 0.0

            binding.etLatitud.setText(latitud.toString())
            binding.etLongitud.setText(longitud.toString())
        }
    }

    private fun cargarDatosEmpresa() {
        val userId = auth.currentUser?.uid ?: return

        lifecycleScope.launch {
            try {
                mostrarProgreso(true)

                val document = db.collection("empresas").document(userId).get().await()
                if (document.exists()) {
                    with(document) {
                        binding.etNombreEmpresa.setText(getString("nombre_empresa"))
                        binding.etRuc.setText(getString("ruc"))
                        binding.etNombre.setText(getString("nombre"))
                        binding.etApellido.setText(getString("apellido"))
                        binding.etDni.setText(getString("dni"))
                        binding.etCorreo.setText(getString("correo"))
                        binding.etTelefono.setText(getString("telefono"))
                        binding.tvFechaRegistro.text = getString("fecha_registro")

                        // Ubicación como String
                        getString("latitud")?.let { binding.etLatitud.setText(it) }
                        getString("longitud")?.let { binding.etLongitud.setText(it) }

                        val estado = getString("estado") ?: "pendiente"
                        binding.tvEstadoEmpresa.text = when(estado) {
                            "aprobado" -> "APROBADO"
                            "rechazado" -> "RECHAZADO"
                            else -> "EN REVISIÓN"
                        }

                        if (estado == "aprobado") {
                            binding.layoutFechaAprobacion.visibility = View.VISIBLE
                            binding.tvFechaAprobacion.text = getString("fecha_aprobacion")
                        }

                        currentImageUrl = getString("logo_url")
                        currentImageUrl?.takeIf { it.isNotBlank() }?.let { url ->
                            Glide.with(this@EmpresaPerfilFragment)
                                .load(url)
                                .circleCrop()
                                .into(binding.ivEmpresaLogo)
                        }

                        binding.tvNombreEmpresa.text = getString("nombre_empresa")
                    }
                }
            } catch (e: Exception) {
                Snackbar.make(binding.root, "Error al cargar datos", Snackbar.LENGTH_LONG).show()
            } finally {
                mostrarProgreso(false)
            }
        }
    }

    private fun setEditMode(editing: Boolean) {
        isEditing = editing
        binding.apply {
            btnEditProfile.visibility = if (editing) View.GONE else View.VISIBLE
            btnSaveProfile.visibility = if (editing) View.VISIBLE else View.GONE
            fabEditLogo.visibility = if (editing) View.VISIBLE else View.GONE
            btnGetLocation.visibility = if (editing) View.VISIBLE else View.GONE

            arrayOf(
                etNombreEmpresa,
                etTelefono,
                etLatitud,
                etLongitud
            ).forEach { it.isEnabled = editing }
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
                mostrarProgresoGuardado(true)

                val nuevoLogoUrl = imageUri?.let { subirLogoEmpresa(userId) }

                val updates = hashMapOf<String, Any>().apply {
                    put("nombre_empresa", binding.etNombreEmpresa.text.toString())
                    put("telefono", binding.etTelefono.text.toString())

                    binding.etLatitud.text.toString().takeIf { it.isNotBlank() }?.let {
                        put("latitud", it)
                    }
                    binding.etLongitud.text.toString().takeIf { it.isNotBlank() }?.let {
                        put("longitud", it)
                    }

                    nuevoLogoUrl?.let { put("logo_url", it) }
                }

                db.collection("empresas").document(userId)
                    .update(updates)
                    .await()

                currentImageUrl = nuevoLogoUrl ?: currentImageUrl
                setEditMode(false)
                Snackbar.make(binding.root, "Perfil actualizado", Snackbar.LENGTH_SHORT).show()

            } catch (e: Exception) {
                Snackbar.make(binding.root, "Error al guardar: ${e.message}", Snackbar.LENGTH_LONG).show()
            } finally {
                mostrarProgresoGuardado(false)
            }
        }
    }

    private suspend fun subirLogoEmpresa(userId: String): String {
        return try {
            val fileRef = storage.child("$userId-${System.currentTimeMillis()}.jpg")
            fileRef.putFile(imageUri!!).await()
            fileRef.downloadUrl.await().toString()
        } catch (e: Exception) {
            throw e
        }
    }

    private fun mostrarProgreso(mostrar: Boolean) {
        binding.progressBar.visibility = if (mostrar) View.VISIBLE else View.GONE
    }

    private fun mostrarProgresoGuardado(mostrar: Boolean) {
        binding.apply {
            progressBar.visibility = if (mostrar) View.VISIBLE else View.GONE
            btnSaveProfile.isEnabled = !mostrar
            btnSaveProfile.text = if (mostrar) "Guardando..." else "Guardar"
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}