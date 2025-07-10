package com.example.mueblar.ui.client

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.NavHostFragment
import com.example.mueblar.R
import com.example.mueblar.databinding.FragmentClientMainBinding

class ClientMainFragment : Fragment() {

    companion object {
        fun newInstance() = ClientMainFragment()
    }

    private val viewModel: ClientMainViewModel by viewModels()
    private var _binding: FragmentClientMainBinding? = null
    private val binding get() = _binding!!

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // TODO: Use the ViewModel
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentClientMainBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Configurar BottomNavigationView con NavController
        val navHostFragment = childFragmentManager.findFragmentById(R.id.admin_nav_host_fragment) as NavHostFragment
        val navController = navHostFragment.navController

        binding.bottomNavigationView.setOnItemSelectedListener { item ->
            // Aplicar animación de escala
            val animation = AnimationUtils.loadAnimation(requireContext(), R.anim.scale_up)
            binding.bottomNavigationView.startAnimation(animation)

            // Manejar navegación
            when (item.itemId) {
                R.id.clientHomeFragment -> {
                    navController.navigate(R.id.clientHomeFragment)
                    true
                }
                R.id.clientProductosFragment -> {
                    navController.navigate(R.id.clientProductsFragment)
                    true
                }
                R.id.clientBusquedaFragment -> {
                    navController.navigate(R.id.clientBusquedaFragment)
                    true
                }
                else -> false
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}