package com.example.mueblar.ui.admin

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.setupWithNavController
import com.example.mueblar.R
import com.example.mueblar.databinding.FragmentAdminMainBinding

class AdminMainFragment : Fragment(R.layout.fragment_admin_main) {

    private lateinit var binding: FragmentAdminMainBinding

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        binding = FragmentAdminMainBinding.bind(view)

        val navHostFragment = childFragmentManager
            .findFragmentById(R.id.admin_nav_host_fragment) as NavHostFragment

        val navController = navHostFragment.navController

        binding.bottomNavigationView.setupWithNavController(navController)

        // Cambiar el título del toolbar según el destino
        navController.addOnDestinationChangedListener { _, destination, _ ->
            binding.tvTitulo.text = destination.label
        }
    }
}
