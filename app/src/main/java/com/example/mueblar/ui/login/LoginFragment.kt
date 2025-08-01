package com.example.mueblar.ui.login

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.example.mueblar.R
import com.example.mueblar.databinding.FragmentLoginBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import com.google.firebase.messaging.FirebaseMessaging

class LoginFragment : Fragment(R.layout.fragment_login) {

    private lateinit var binding: FragmentLoginBinding
    private val viewModel: LoginViewModel by viewModels()
    private val auth = FirebaseAuth.getInstance()
    private val db = FirebaseFirestore.getInstance()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = FragmentLoginBinding.bind(view)

        // Limpiar campos al entrar
        binding.etUsuario.setText("")
        binding.etContrasena.setText("")

        setupObservers()
        setupClickListeners()
    }

    private fun setupObservers() {
        viewModel.loginResult.observe(viewLifecycleOwner) { result ->
            result?.let { (success, message, tipoUsuario, estadoEmpresa) ->
                if (success) {
                    updateFCMTokenAndNavigate(tipoUsuario, estadoEmpresa)
                } else {
                    Toast.makeText(requireContext(), message, Toast.LENGTH_LONG).show()
                }
                viewModel.clearLoginResult()
            }
        }

        viewModel.isLoading.observe(viewLifecycleOwner) { isLoading ->
            if (isLoading) {
                binding.progressBar.visibility = View.VISIBLE
                binding.btnLogin.isEnabled = false
                binding.btnLogin.text = "Iniciando..."
            } else {
                binding.progressBar.visibility = View.GONE
                binding.btnLogin.isEnabled = true
                binding.btnLogin.text = "INICIAR SESIÓN"
            }
        }
    }

    private fun setupClickListeners() {
        binding.btnLogin.setOnClickListener {
            val correo = binding.etUsuario.text.toString().trim()
            val contrasena = binding.etContrasena.text.toString().trim()
            viewModel.loginUser(correo, contrasena)
        }

        binding.tvRegistrar.setOnClickListener {
            findNavController().navigate(R.id.action_loginFragment_to_registerFragment)
        }
    }

    private fun updateFCMTokenAndNavigate(tipoUsuario: String?, estadoEmpresa: String?) {
        val userId = auth.currentUser?.uid ?: return
        val collection = when (tipoUsuario) {
            "empresa" -> "empresas"
            else -> "clientes" // Para admin y cliente normal
        }

        FirebaseMessaging.getInstance().token.addOnSuccessListener { newToken ->
            val sharedPref = requireContext().getSharedPreferences("prefs", android.content.Context.MODE_PRIVATE)
            val savedToken = sharedPref.getString("fcm_token", null)

            if (newToken != null && newToken != savedToken) {
                // Solo actualiza si el token cambió
                val tokenData = mapOf("fcmToken" to newToken)
                db.collection(collection).document(userId)
                    .set(tokenData, SetOptions.merge())
                    .addOnSuccessListener {
                        sharedPref.edit().putString("fcm_token", newToken).apply()
                        navigateToMain(tipoUsuario, estadoEmpresa)
                    }
                    .addOnFailureListener {
                        Toast.makeText(requireContext(), "Error al actualizar token", Toast.LENGTH_SHORT).show()
                        navigateToMain(tipoUsuario, estadoEmpresa)
                    }
            } else {
                // Token ya estaba guardado, continúa sin actualizar Firestore
                navigateToMain(tipoUsuario, estadoEmpresa)
            }

        }.addOnFailureListener {
            Toast.makeText(requireContext(), "Error al obtener token", Toast.LENGTH_SHORT).show()
            navigateToMain(tipoUsuario, estadoEmpresa)
        }
    }


    private fun navigateToMain(tipoUsuario: String?, estadoEmpresa: String?) {
        when (tipoUsuario) {
            "cliente" -> {
                Toast.makeText(requireContext(), "Bienvenido, cliente", Toast.LENGTH_SHORT).show()
                findNavController().navigate(R.id.clientMainFragment)
            }
            "admin" -> {
                Toast.makeText(requireContext(), "Bienvenido, administrador", Toast.LENGTH_SHORT).show()
                findNavController().navigate(R.id.adminMainFragment)
            }
            "empresa" -> {
                when (estadoEmpresa) {
                    "aprobado" -> {
                        Toast.makeText(requireContext(), "Bienvenido, empresa", Toast.LENGTH_SHORT).show()
                        findNavController().navigate(R.id.empresaMainFragment)
                    }
                    "pendiente" -> {
                        Toast.makeText(requireContext(), "Tu solicitud está en verificación", Toast.LENGTH_LONG).show()
                    }
                    "rechazado" -> {
                        Toast.makeText(requireContext(), "Tu cuenta no ha sido aprobada", Toast.LENGTH_LONG).show()
                    }
                    else -> {
                        Toast.makeText(requireContext(), "Estado de empresa no reconocido", Toast.LENGTH_SHORT).show()
                    }
                }
            }
            else -> {
                Toast.makeText(requireContext(), "Tipo de usuario no reconocido", Toast.LENGTH_SHORT).show()
            }
        }
    }
}
