package com.example.mueblar.ui.empresa

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.mueblar.R
import com.example.mueblar.data.model.Producto
import com.example.mueblar.databinding.FragmentEmpresaProductosBinding

class EmpresaProductosFragment : Fragment(R.layout.fragment_empresa_productos) {

    private var _binding: FragmentEmpresaProductosBinding? = null
    private val binding get() = _binding!!

    private lateinit var adapter: EmpresaProductoAdapter
    private val viewModel: EmpresaProductosViewModel by viewModels()

    private val productos = mutableListOf<Producto>()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentEmpresaProductosBinding.bind(view)

        configurarRecyclerView()

        viewModel.productos.observe(viewLifecycleOwner) { lista ->
            productos.clear()
            productos.addAll(lista)
            adapter.notifyDataSetChanged()
        }

        viewModel.mensaje.observe(viewLifecycleOwner) { mensaje ->
            mensaje?.let {
                Toast.makeText(requireContext(), it, Toast.LENGTH_SHORT).show()
                viewModel.limpiarMensaje()
            }
        }

        viewModel.escucharProductosEmpresa()

        binding.fabAgregarProducto.setOnClickListener {
            AgregarProductoDialogFragment().show(childFragmentManager, "AgregarProductoDialog")
        }
    }

    private fun configurarRecyclerView() {
        adapter = EmpresaProductoAdapter(
            productos,
            onEditarClick = { producto ->
                Toast.makeText(requireContext(), "Editar: ${producto.nombre}", Toast.LENGTH_SHORT).show()
                // Aquí iría la lógica para editar producto
            },
            onEliminarClick = { producto ->
                eliminarProducto(producto)
            }
        )

        binding.rvProductos.layoutManager = LinearLayoutManager(requireContext())
        binding.rvProductos.adapter = adapter
    }

    private fun eliminarProducto(producto: Producto) {
        val db = com.google.firebase.firestore.FirebaseFirestore.getInstance()
        db.collection("productos")
            .whereEqualTo("empresaId", producto.empresaId)
            .whereEqualTo("nombre", producto.nombre)
            .get()
            .addOnSuccessListener { result ->
                for (doc in result) {
                    db.collection("productos").document(doc.id).delete()
                }
                Toast.makeText(requireContext(), "Producto eliminado", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener {
                Toast.makeText(requireContext(), "Error al eliminar producto", Toast.LENGTH_SHORT).show()
            }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
