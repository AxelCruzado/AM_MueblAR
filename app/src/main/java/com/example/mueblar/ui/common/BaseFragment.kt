package com.example.mueblar.ui.common

import androidx.fragment.app.Fragment
import androidx.navigation.NavController
import androidx.navigation.fragment.findNavController
import com.example.mueblar.R
import android.util.Log

open class BaseFragment : Fragment() {
    fun getRootNavController(): NavController {
        val navHostFragment = requireActivity().supportFragmentManager
            .findFragmentById(R.id.nav_host_fragment)
        return navHostFragment?.findNavController()
            ?: throw IllegalStateException("Root NavHostFragment not found")
    }

    protected fun cerrarSesion() {
        try {
            // Borrar el token guardado en SharedPreferences para forzar actualización después del login
            val sharedPref = requireContext().getSharedPreferences("prefs", android.content.Context.MODE_PRIVATE)
            sharedPref.edit().remove("fcm_token").apply()

            getRootNavController().navigate(R.id.action_global_loginFragment)
        } catch (e: Exception) {
            Log.e("NavigationError", "Error navigating to loginFragment", e)
        }
    }

}