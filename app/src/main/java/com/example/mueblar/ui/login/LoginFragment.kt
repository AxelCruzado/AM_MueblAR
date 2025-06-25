package com.example.mueblar.ui.login

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.example.mueblar.R
import com.example.mueblar.databinding.FragmentLoginBinding

class LoginFragment : Fragment(R.layout.fragment_login) {
    private lateinit var binding: FragmentLoginBinding
    private val viewModel: LoginViewModel by viewModels()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        binding = FragmentLoginBinding.bind(view)

        // Limpiar campos al entrar
        binding.etUsuario.setText("")
        binding.etContrasena.setText("")

        // Observer del resultado del login
        viewModel.loginResult.observe(viewLifecycleOwner) { result ->
            result?.let { (success, tipoUsuario) ->
                if (success) {
                    Toast.makeText(requireContext(), "Bienvenido $tipoUsuario", Toast.LENGTH_SHORT).show()

                    when (tipoUsuario) {
                        "Administrador" -> findNavController().navigate(R.id.adminMainFragment)
                        // "Cliente" -> findNavController().navigate(R.id.clienteMainFragment)
                        // "Empresa" -> findNavController().navigate(R.id.empresaMainFragment)
                        else -> Toast.makeText(requireContext(), "Tipo de usuario no reconocido", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    Toast.makeText(requireContext(), tipoUsuario, Toast.LENGTH_SHORT).show()
                }

                // Limpiar el resultado para evitar auto-navegación al volver
                viewModel.clearLoginResult()
            }
        }

        binding.btnLogin.setOnClickListener {
            val correo = binding.etUsuario.text.toString().trim()
            val contrasena = binding.etContrasena.text.toString().trim()
            viewModel.loginUser(correo, contrasena)
        }

        binding.tvRegistrar.setOnClickListener {
            findNavController().navigate(R.id.action_loginFragment_to_registerFragment)
        }
    }
}
