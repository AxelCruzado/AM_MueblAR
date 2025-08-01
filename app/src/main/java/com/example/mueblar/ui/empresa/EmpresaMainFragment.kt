package com.example.mueblar.ui.empresa

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.NavController
import androidx.navigation.findNavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.setupWithNavController
import com.example.mueblar.R
import com.example.mueblar.databinding.FragmentEmpresaMainBinding

class EmpresaMainFragment : Fragment() {

    private var _binding: FragmentEmpresaMainBinding? = null
    private val binding get() = _binding!!

    private val viewModel: EmpresaMainViewModel by viewModels()
    private var backPressedTime: Long = 0
    private val backPressThreshold = 2000 // 2 segundos para el doble toque

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentEmpresaMainBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupBottomNavigation()
        observeViewModel()

        // Manejo personalizado del botón de retroceso
        val callback = object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                val navController = binding.empresaNavHostFragment.findNavController()
                val currentDestination = navController.currentDestination?.id
                if (currentDestination == R.id.empresaHomeFragment) {
                    // Si está en EmpresaHomeFragment, manejar el doble toque para salir
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
                    // Si está en otro destino, regresar a EmpresaHomeFragment
                    navController.popBackStack(R.id.empresaHomeFragment, false)
                }
            }
        }
        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner, callback)
    }

    private fun setupBottomNavigation() {
        val navHostFragment = childFragmentManager
            .findFragmentById(binding.empresaNavHostFragment.id) as? NavHostFragment
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
                R.id.empresaHomeFragment -> {
                    // Si se selecciona "Inicio", limpiar la pila y navegar a EmpresaHomeFragment
                    if (navController.currentDestination?.id != R.id.empresaHomeFragment) {
                        navController.popBackStack(R.id.empresaHomeFragment, false)
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
    }

    private fun observeViewModel() {
        // Ejemplo de observación si luego añades LiveData
        // viewModel.someLiveData.observe(viewLifecycleOwner) { data ->
        //     // actualizar UI
        // }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}