package com.example.mueblar.ui.client

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.mueblar.databinding.FragmentClientFavoriteBinding
import com.example.mueblar.data.model.Producto

class ClientFavoritoFragment : Fragment() {

    companion object {
        fun newInstance() = ClientFavoritoFragment()
    }

    private var _binding: FragmentClientFavoriteBinding? = null
    private val binding get() = _binding!!
    private val viewModel: ClientFavoritoViewModel by viewModels()
    private lateinit var adapter: ProductoAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentClientFavoriteBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupRecyclerView()
        observeViewModel()
    }

    private fun setupRecyclerView() {
        adapter = ProductoAdapter(
            onFavoriteClick = { producto, isFavorite ->
                if (!isFavorite) {
                    viewModel.removeFavorite(producto)
                }
            },
            onItemClick = { producto ->
                // Navegar a ProductDetailFragment
                val action = ClientFavoritoFragmentDirections.actionClientFavoritoFragmentToProductDetailFragment(producto)
                findNavController().navigate(action)
            }
        )
        binding.rvFavorites.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = this@ClientFavoritoFragment.adapter
        }
    }

    private fun observeViewModel() {
        viewModel.favoritos.observe(viewLifecycleOwner) { favoritos ->
            adapter.submitList(favoritos)
        }
        viewModel.error.observe(viewLifecycleOwner) { error ->
            if (error.isNotEmpty()) {
                Toast.makeText(context, error, Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}