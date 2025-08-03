
package com.example.mueblar.ui.producto

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import com.example.mueblar.databinding.FragmentProductDetailBinding
import com.google.firebase.firestore.FirebaseFirestore

class ProductDetailFragment : Fragment() {

    private var _binding: FragmentProductDetailBinding? = null
    private val binding get() = _binding!!
    private val db = FirebaseFirestore.getInstance()
    private val TAG = "ProductDetailFragment"
    private lateinit var empresaId: String

    private val locationPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            Log.d(TAG, "Permiso de ubicación concedido, navegando al mapa")
            navigateToMap()
        } else {
            Toast.makeText(context, "Permiso de ubicación denegado", Toast.LENGTH_SHORT).show()
        }
    }

    private val cameraPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            // Aquí no tienes acceso a modeloUrl directamente
            // Solución: guardar modeloUrl temporalmente en una variable global si quieres usarlo luego
            Toast.makeText(context, "Permiso de cámara concedido", Toast.LENGTH_SHORT).show()
        } else {
            Toast.makeText(context, "Permiso de cámara denegado", Toast.LENGTH_SHORT).show()
        }
    }


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentProductDetailBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Obtener argumentos del producto
        val args = ProductDetailFragmentArgs.fromBundle(requireArguments())
        val nombreProducto = args.producto.nombre
        val descripcion = args.producto.descripcion
        val precio = args.producto.precio
        val imagenUrl = args.producto.imagenUrl
        empresaId = args.producto.empresaId

        // Mostrar datos del producto en la UI
        binding.tvNombreProducto.text = nombreProducto
        binding.tvDescripcion.text = descripcion
        binding.tvPrecio.text = "S/. ${precio.toString()}"
        Glide.with(this)
            .load(imagenUrl)
            .into(binding.imgProducto)

        Log.d(TAG, "Datos del producto: nombre=$nombreProducto, empresaId=$empresaId")

        // Configurar clics de botones
        binding.btnUbicacion.setOnClickListener {
            checkLocationPermission()
        }

        binding.btnRealidadAumentada.setOnClickListener {
            val args = ProductDetailFragmentArgs.fromBundle(requireArguments())
            val modeloUrl = args.producto.modeloArUrl

            if (modeloUrl.isNotBlank()) {
                checkCameraPermission(modeloUrl)
            } else {
                Toast.makeText(context, "Este producto no tiene modelo AR", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun checkLocationPermission() {
        when {
            ContextCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED -> {
                Log.d(TAG, "Permiso de ubicación ya concedido, navegando al mapa")
                navigateToMap()
            }
            else -> {
                Log.d(TAG, "Solicitando permiso de ubicación")
                locationPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
            }
        }
    }

    private fun checkCameraPermission(modeloUrl: String) {
        when {
            ContextCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.CAMERA
            ) == PackageManager.PERMISSION_GRANTED -> {
                val action = ProductDetailFragmentDirections.actionProductDetailFragmentToARViewerFragment(modeloUrl)
                findNavController().navigate(action)
            }
            else -> {
                cameraPermissionLauncher.launch(Manifest.permission.CAMERA)
            }
        }
    }

    private fun navigateToMap() {
        if (!::empresaId.isInitialized) {
            Log.e(TAG, "empresaId no está inicializado")
            Toast.makeText(context, "Error: ID de empresa no inicializado", Toast.LENGTH_SHORT).show()
            return
        }

        Log.d(TAG, "Consultando Firestore para empresaId=$empresaId")

        // Consultar Firestore para obtener los datos de la empresa
        db.collection("empresas").document(empresaId).get()
            .addOnSuccessListener { document ->
                if (document.exists()) {
                    val nombreEmpresa = document.getString("nombre_empresa") ?: "Empresa"
                    val latitudStr = document.getString("latitud")
                    val longitudStr = document.getString("longitud")

                    val latitud = latitudStr?.toFloatOrNull() ?: 0.0f
                    val longitud = longitudStr?.toFloatOrNull() ?: 0.0f

                    Log.d(TAG, "Datos de empresa obtenidos: nombre=$nombreEmpresa, latitud=$latitud, longitud=$longitud")

                    val action = ProductDetailFragmentDirections.actionProductDetailFragmentToMapaFragment(
                        nombreEmpresa = nombreEmpresa,
                        latitud = latitud,
                        longitud = longitud
                    )
                    findNavController().navigate(action)

                } else {
                    Log.e(TAG, "Documento de empresa no encontrado para empresaId=$empresaId")
                    Toast.makeText(context, "No se encontró la empresa", Toast.LENGTH_SHORT).show()
                }
            }
            .addOnFailureListener { e ->
                Log.e(TAG, "Error al consultar Firestore: ${e.message}")
                Toast.makeText(context, "Error al obtener datos de la empresa: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}