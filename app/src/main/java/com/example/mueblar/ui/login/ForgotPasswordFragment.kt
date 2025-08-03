package com.example.mueblar.ui.login

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.example.mueblar.R
import com.example.mueblar.databinding.FragmentForgotPasswordBinding

class ForgotPasswordFragment : Fragment(R.layout.fragment_forgot_password) {

    private lateinit var binding: FragmentForgotPasswordBinding
    private val viewModel: LoginViewModel by viewModels()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = FragmentForgotPasswordBinding.bind(view)

        // Limpiar campo al entrar
        binding.etEmail.setText("")

        setupObservers()
        setupClickListeners()
    }

    private fun setupObservers() {
        viewModel.resetPasswordResult.observe(viewLifecycleOwner) { result ->
            result?.let { (success, message) ->
                Toast.makeText(requireContext(), message, Toast.LENGTH_LONG).show()
                if (success) {
                    findNavController().navigate(R.id.action_forgotPasswordFragment_to_loginFragment)
                }
                viewModel.clearResetPasswordResult()
            }
        }

        viewModel.isLoading.observe(viewLifecycleOwner) { isLoading ->
            if (isLoading) {
                binding.progressBar.visibility = View.VISIBLE
                binding.btnSendReset.isEnabled = false
                binding.btnSendReset.text = "Enviando..."
            } else {
                binding.progressBar.visibility = View.GONE
                binding.btnSendReset.isEnabled = true
                binding.btnSendReset.text = "ENVIAR ENLACE"
            }
        }
    }

    private fun setupClickListeners() {
        binding.btnSendReset.setOnClickListener {
            val email = binding.etEmail.text.toString().trim()
            viewModel.resetPassword(email)
        }

        binding.btnBack.setOnClickListener {
            findNavController().navigate(R.id.action_forgotPasswordFragment_to_loginFragment)
        }
    }
}