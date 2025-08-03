package com.example.mueblar.ui.admin

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.mueblar.databinding.FragmentAdminRegistrosBinding
import com.example.mueblar.ui.empresa.EmpresaAdapter

class AdminRegistrosFragment : Fragment() {

    private var _binding: FragmentAdminRegistrosBinding? = null
    private val binding get() = _binding!!
    private val viewModel: AdminRegistrosViewModel by viewModels()
    private lateinit var adapter: EmpresaAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAdminRegistrosBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupRecyclerView()
        setupSwipeRefresh()
        observeEmpresas()
    }

    private fun setupRecyclerView() {
        adapter = EmpresaAdapter(
            emptyList(),
            onAceptarClick = { empresa ->
                viewModel.actualizarEstadoEmpresa(empresa, "aprobado")
                Toast.makeText(context, "Empresa ${empresa.nombre_empresa} aceptada exitosamente", Toast.LENGTH_SHORT).show()
            },
            onRechazarClick = { empresa ->
                viewModel.actualizarEstadoEmpresa(empresa, "rechazado")
                Toast.makeText(context, "Empresa ${empresa.nombre_empresa} rechazada exitosamente", Toast.LENGTH_SHORT).show()
            }
        )
        binding.rvEmpresas.apply {
            layoutManager = LinearLayoutManager(context)
            this.adapter = this@AdminRegistrosFragment.adapter
        }
    }

    private fun setupSwipeRefresh() {
        binding.swipeRefreshLayout.setOnRefreshListener {
            viewModel.loadEmpresasPendientes()
        }
    }

    private fun observeEmpresas() {
        viewModel.empresas.observe(viewLifecycleOwner) { empresas ->
            adapter = EmpresaAdapter(
                empresas,
                onAceptarClick = { empresa ->
                    viewModel.actualizarEstadoEmpresa(empresa, "aprobado")
                    Toast.makeText(context, "Empresa ${empresa.nombre_empresa} aceptada exitosamente", Toast.LENGTH_SHORT).show()
                },
                onRechazarClick = { empresa ->
                    viewModel.actualizarEstadoEmpresa(empresa, "rechazado")
                    Toast.makeText(context, "Empresa ${empresa.nombre_empresa} rechazada exitosamente", Toast.LENGTH_SHORT).show()
                }
            )
            binding.rvEmpresas.adapter = adapter

            // Controlar visibilidad del RecyclerView y el TextView
            if (empresas.isEmpty()) {
                binding.rvEmpresas.visibility = View.GONE
                binding.tvNoSolicitudes.visibility = View.VISIBLE
            } else {
                binding.rvEmpresas.visibility = View.VISIBLE
                binding.tvNoSolicitudes.visibility = View.GONE
            }

            // Detener la animación de refresco
            binding.swipeRefreshLayout.isRefreshing = false
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}