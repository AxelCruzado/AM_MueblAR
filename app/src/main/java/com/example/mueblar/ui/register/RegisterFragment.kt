package com.example.mueblar.ui.register
import android.os.Bundle
import android.view.View
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.example.mueblar.R
import com.example.mueblar.data.model.User
import com.example.mueblar.databinding.FragmentRegisterBinding

class RegisterFragment : Fragment(R.layout.fragment_register) {
    private lateinit var binding: FragmentRegisterBinding
    private val viewModel: RegisterViewModel by viewModels()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        binding = FragmentRegisterBinding.bind(view)

        val tiposUsuario = resources.getStringArray(R.array.tipos_usuario_registro)
        val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, tiposUsuario)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.spinnerTipoUsuario.adapter = adapter

        binding.spinnerTipoUsuario.onItemSelectedListener = object : android.widget.AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: android.widget.AdapterView<*>?, view: View?, position: Int, id: Long) {
                val tipo = tiposUsuario[position]
                binding.layoutRuc.visibility = if (tipo == "Empresa") View.VISIBLE else View.GONE
            }

            override fun onNothingSelected(parent: android.widget.AdapterView<*>?) {
                // Nada que hacer aquí
            }
        }

        binding.btnRegistrar.setOnClickListener {
            val user = User(
                correo = binding.etCorreo.text.toString().trim(),
                nombre = binding.etNombre.text.toString().trim(),
                apellido = binding.etApellido.text.toString().trim(),
                telefono = binding.etTelefono.text.toString().trim(),
                ruc = binding.etRuc.text.toString().trim(),
                tipoUsuario = binding.spinnerTipoUsuario.selectedItem.toString()
            )
            val password = binding.etContrasena.text.toString()
            viewModel.register(user, password)
        }

        viewModel.registerResult.observe(viewLifecycleOwner) { (success, message) ->
            Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
            if (success) findNavController().navigate(R.id.action_registerFragment_to_registerSuccessFragment)
        }
    }
}
