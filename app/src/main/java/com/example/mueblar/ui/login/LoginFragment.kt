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
        super.onViewCreated(view, savedInstanceState)
        binding = FragmentLoginBinding.bind(view)

        // Limpiar campos al entrar
        binding.etUsuario.setText("")
        binding.etContrasena.setText("")

        // Observer del resultado del login
        viewModel.loginResult.observe(viewLifecycleOwner) { result ->
            result?.let { (success, message, tipoUsuario, estadoEmpresa) ->
                if (success) {
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
                } else {
                    Toast.makeText(requireContext(), message, Toast.LENGTH_LONG).show()
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