package com.example.mueblar.ui.admin

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.fragment.app.Fragment
import androidx.navigation.findNavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.setupWithNavController
import com.example.mueblar.R
import com.example.mueblar.databinding.FragmentAdminMainBinding
import com.google.firebase.auth.FirebaseAuth

class AdminMainFragment : Fragment() {
    private var _binding: FragmentAdminMainBinding? = null
    private val binding get() = _binding!!

    private val auth: FirebaseAuth by lazy { FirebaseAuth.getInstance() }
    private var backPressedTime: Long = 0
    private val backPressThreshold = 2000 // 2 segundos para el doble toque

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAdminMainBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val navHostFragment = childFragmentManager
            .findFragmentById(R.id.admin_nav_host_fragment) as? NavHostFragment
        val navController = navHostFragment?.navController

        if (navController == null) {
            Toast.makeText(context, "Error: Controlador de navegación no encontrado", Toast.LENGTH_SHORT).show()
            return
        }

        // Configurar BottomNavigationView
        binding.bottomNavigationView.setupWithNavController(navController)

        // Interceptar clics en BottomNavigationView
        binding.bottomNavigationView.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.adminHomeFragment -> {
                    // Si se selecciona "Inicio", limpiar la pila y navegar a AdminHomeFragment
                    if (navController.currentDestination?.id != R.id.adminHomeFragment) {
                        navController.popBackStack(R.id.adminHomeFragment, false)
                    }
                    true
                }
                else -> {
                    // Permitir que setupWithNavController maneje otros ítems
                    navController.navigate(item.itemId)
                    true
                }
            }
        }

        // Actualizar título según el destino
        navController.addOnDestinationChangedListener { _, destination, _ ->
            binding.tvTitulo.text = destination.label
        }

        // Manejo del cierre de sesión
        binding.ivLogout.setOnClickListener {
            auth.signOut()
            navigateToLogin()
        }

        // Manejo personalizado del botón de retroceso
        val callback = object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                val currentDestination = navController.currentDestination?.id
                if (currentDestination == R.id.adminHomeFragment) {
                    // Si está en AdminHomeFragment, manejar el doble toque para salir
                    if (System.currentTimeMillis() - backPressedTime < backPressThreshold) {
                        requireActivity().finish() // Cerrar la aplicación
                    } else {
                        backPressedTime = System.currentTimeMillis()
                        Toast.makeText(
                            context,
                            "Presione nuevamente para salir",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                } else {
                    // Si está en otro destino, regresar a AdminHomeFragment
                    navController.popBackStack(R.id.adminHomeFragment, false)
                }
            }
        }
        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner, callback)
    }

    private fun navigateToLogin() {
        try {
            if (isAdded) {
                requireActivity().findNavController(R.id.nav_host_fragment)
                    .navigate(R.id.action_global_loginFragment)
            } else {
                Log.e("NavigationError", "Fragment no adjunto")
                Toast.makeText(context, "Error al cerrar sesión", Toast.LENGTH_SHORT).show()
            }
        } catch (e: Exception) {
            Log.e("NavigationError", "Error navigating to loginFragment", e)
            Toast.makeText(context, "Error al cerrar sesión", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}