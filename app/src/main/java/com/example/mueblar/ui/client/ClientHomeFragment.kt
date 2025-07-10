package com.example.mueblar.ui.client

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.example.mueblar.R
import com.example.mueblar.data.model.Producto
import com.example.mueblar.databinding.FragmentClientHomeBinding
import com.example.mueblar.ui.client.adapter.ProductoDestacadoAdapter
import com.google.android.material.tabs.TabLayoutMediator

class ClientHomeFragment : Fragment() {

    private var _binding: FragmentClientHomeBinding? = null
    private val binding get() = _binding!!

    private val viewModel: ClientHomeViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentClientHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel.productosDestacados.observe(viewLifecycleOwner) { productos ->
            setupProductosDestacados(productos)
        }
    }

    private fun setupProductosDestacados(productos: List<Producto>) {
        val adapter = ProductoDestacadoAdapter(productos) { producto ->
            // Navegar al fragmento de detalles del producto
            val action = ClientHomeFragmentDirections.actionClientHomeFragmentToProductDetailFragment(producto)
            findNavController().navigate(action)
        }
        binding.vpProductosDestacados.adapter = adapter

        // Configurar indicadores
        TabLayoutMediator(binding.tabLayoutIndicators, binding.vpProductosDestacados) { _, _ ->
            // No se necesita configuración adicional, el drawable maneja el estilo
        }.attach()

        // Ocultar indicadores si solo hay un producto
        binding.tabLayoutIndicators.visibility = if (productos.size <= 1) View.GONE else View.VISIBLE
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}