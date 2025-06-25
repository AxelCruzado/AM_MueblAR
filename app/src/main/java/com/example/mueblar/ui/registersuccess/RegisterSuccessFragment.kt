package com.example.mueblar.ui.registersuccess

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.mueblar.R
import com.example.mueblar.databinding.FragmentRegisterSuccessBinding

class RegisterSuccessFragment : Fragment(R.layout.fragment_register_success) {
    private lateinit var binding: FragmentRegisterSuccessBinding

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        binding = FragmentRegisterSuccessBinding.bind(view)
        binding.btnAceptar.setOnClickListener {
            findNavController().navigate(R.id.action_registerSuccessFragment_to_loginFragment)
        }
    }
}
