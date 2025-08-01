package com.example.mueblar.ui.client

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.setupWithNavController
import com.example.mueblar.R
import com.example.mueblar.databinding.FragmentClientMainBinding

class ClientMainFragment : Fragment() {

    private var _binding: FragmentClientMainBinding? = null
    private val binding get() = _binding!!

    private val viewModel: ClientMainViewModel by viewModels()
    private var backPressedTime: Long = 0
    private val backPressThreshold = 2000 // 2 segundos para el doble toque

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentClientMainBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val navHostFragment = childFragmentManager
            .findFragmentById(R.id.client_nav_host_fragment) as? NavHostFragment
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
                R.id.clientHomeFragment -> {
                    // Si se selecciona "Inicio", limpiar la pila y navegar a ClientHomeFragment
                    if (navController.currentDestination?.id != R.id.clientHomeFragment) {
                        navController.popBackStack(R.id.clientHomeFragment, false)
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

        // Manejo personalizado del botón de retroceso
        val callback = object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                val currentDestination = navController.currentDestination?.id
                if (currentDestination == R.id.clientHomeFragment) {
                    // Si está en ClientHomeFragment, manejar el doble toque para salir
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
                    // Si está en otro destino, regresar a ClientHomeFragment
                    navController.popBackStack(R.id.clientHomeFragment, false)
                }
            }
        }
        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner, callback)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}