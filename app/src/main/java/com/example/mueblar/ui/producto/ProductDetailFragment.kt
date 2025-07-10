package com.example.mueblar.ui.client

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.bumptech.glide.Glide
import com.example.mueblar.R
import com.example.mueblar.data.model.Producto
import com.example.mueblar.databinding.FragmentProductDetailBinding
import com.google.firebase.firestore.FirebaseFirestore

class ProductDetailFragment : Fragment() {

    private var _binding: FragmentProductDetailBinding? = null
    private val binding get() = _binding!!

    private val db = FirebaseFirestore.getInstance()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentProductDetailBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Obtener el objeto Producto de los argumentos
        val producto = ProductDetailFragmentArgs.fromBundle(requireArguments()).producto

        // Configurar los elementos de la UI
        with(binding) {
            tvNombreProducto.text = producto.nombre
            tvDescripcion.text = producto.descripcion
            Glide.with(root.context)
                .load(producto.imagenUrl)
                .placeholder(R.drawable.ic_launcher_foreground)
                .into(imgProducto)

            // Botón de Realidad Aumentada
            btnRealidadAumentada.setOnClickListener {
                // Implementar lógica para realidad aumentada (ARCore)
                android.widget.Toast.makeText(
                    requireContext(),
                    "Iniciando Realidad Aumentada para ${producto.nombre}",
                    android.widget.Toast.LENGTH_SHORT
                ).show()
            }

            // Botón de Ubicación
            btnUbicacion.setOnClickListener {
                // Consultar la empresa para obtener latitud y longitud
                db.collection("empresas").document(producto.empresaId).get()
                    .addOnSuccessListener { document ->
                        if (document.exists()) {
                            val latitud = document.getDouble("latitud")
                            val longitud = document.getDouble("longitud")
                            val nombreEmpresa = document.getString("nombre_empresa") ?: "Empresa"
                            if (latitud != null && longitud != null) {
                                // Abrir Google Maps con las coordenadas
                                val uri = Uri.parse("geo:$latitud,$longitud?q=$latitud,$longitud($nombreEmpresa)")
                                val intent = Intent(Intent.ACTION_VIEW, uri)
                                intent.setPackage("com.google.android.apps.maps")
                                startActivity(intent)
                            } else {
                                android.widget.Toast.makeText(
                                    requireContext(),
                                    "No se encontró la ubicación de la empresa",
                                    android.widget.Toast.LENGTH_SHORT
                                ).show()
                            }
                        }
                    }
                    .addOnFailureListener {
                        android.widget.Toast.makeText(
                            requireContext(),
                            "Error al obtener ubicación",
                            android.widget.Toast.LENGTH_SHORT
                        ).show()
                    }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}