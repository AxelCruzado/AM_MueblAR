package com.example.mueblar.ui.client

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.mueblar.databinding.FragmentClientProductsBinding
import com.example.mueblar.data.model.Producto

class ClientProductsFragment : Fragment() {

    companion object {
        fun newInstance() = ClientProductsFragment()
    }

    private var _binding: FragmentClientProductsBinding? = null
    private val binding get() = _binding!!
    private val viewModel: ClientProductsViewModel by viewModels()
    private lateinit var adapter: ProductoAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentClientProductsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupRecyclerView()
        setupSearch()
        observeViewModel()
    }

    private fun setupRecyclerView() {
        adapter = ProductoAdapter(
            onFavoriteClick = { producto, isFavorite ->
                viewModel.toggleFavorite(producto, isFavorite)
            },
            onItemClick = { producto ->
                // Usar el nombre correcto de la acción
                val action = ClientProductsFragmentDirections.actionClientProductosFragmentToProductDetailFragment(producto)
                findNavController().navigate(action)
            }
        )
        binding.rvProducts.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = this@ClientProductsFragment.adapter
        }
    }

    private fun setupSearch() {
        binding.etSearch.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                viewModel.filterProductos(s.toString())
            }
        })
    }

    private fun observeViewModel() {
        viewModel.filteredProductos.observe(viewLifecycleOwner) { productos ->
            adapter.submitList(productos)
        }
        viewModel.error.observe(viewLifecycleOwner) { error ->
            if (error.isNotEmpty()) {
                Toast.makeText(context, error, Toast.LENGTH_SHORT).show()
            }
        }
        viewModel.favoriteChanged.observe(viewLifecycleOwner) { (productoId, isFavorite) ->
            adapter.updateFavoriteStatus(productoId, isFavorite)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}