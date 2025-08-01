package com.example.mueblar.ui.register

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.*
import android.widget.*
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.mueblar.R
import com.example.mueblar.data.model.Cliente
import com.example.mueblar.data.model.Empresa
import com.example.mueblar.ui.ubicacion.MapaUbicacionActivity
import com.google.android.material.button.MaterialButton
import com.google.android.material.button.MaterialButtonToggleGroup
import com.google.android.material.textfield.TextInputEditText
import com.google.firebase.firestore.FirebaseFirestore
import java.text.SimpleDateFormat
import java.util.*
import androidx.activity.result.contract.ActivityResultContracts
import com.google.android.material.textfield.TextInputLayout
import com.google.firebase.auth.FirebaseAuth

class RegisterFragment : Fragment() {

    private lateinit var toggleGroup: MaterialButtonToggleGroup
    private lateinit var layoutEmpresa: LinearLayout
    private lateinit var tvUbicacionConfirmada: TextView
    private lateinit var btnCliente: MaterialButton
    private lateinit var btnEmpresa: MaterialButton

    private lateinit var etNombre: TextInputEditText
    private lateinit var etApellido: TextInputEditText
    private lateinit var etCorreo: TextInputEditText
    private lateinit var etContrasena: TextInputEditText
    private lateinit var etTelefono: TextInputEditText
    private lateinit var etDni: TextInputEditText
    private lateinit var etNombreEmpresa: TextInputEditText
    private lateinit var etRuc: TextInputEditText
    private lateinit var etConfirmarContrasena: TextInputEditText
    private lateinit var tilConfirmarContrasena: TextInputLayout


    private var latitud: String = ""
    private var longitud: String = ""

    // Launcher moderno para recoger ubicación
    private val ubicacionLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            result.data?.let { data ->
                latitud = data.getDoubleExtra("latitud", 0.0).toString()
                longitud = data.getDoubleExtra("longitud", 0.0).toString()
                tvUbicacionConfirmada.text = "Ubicación confirmada ✔"
                tvUbicacionConfirmada.visibility = View.VISIBLE
                Toast.makeText(requireContext(), "Coordenadas guardadas correctamente", Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        val view = inflater.inflate(R.layout.fragment_register, container, false)

        // Referencias de UI
        toggleGroup = view.findViewById(R.id.toggleTipoUsuario)
        layoutEmpresa = view.findViewById(R.id.layoutEmpresa)
        tvUbicacionConfirmada = view.findViewById(R.id.tvUbicacionConfirmada)

        btnCliente = view.findViewById(R.id.btnCliente)
        btnEmpresa = view.findViewById(R.id.btnEmpresa)

        etNombre = view.findViewById(R.id.etNombre)
        etApellido = view.findViewById(R.id.etApellido)
        etCorreo = view.findViewById(R.id.etCorreo)
        etContrasena = view.findViewById(R.id.etContrasena)
        etTelefono = view.findViewById(R.id.etTelefono)
        etDni = view.findViewById(R.id.etDni)
        etConfirmarContrasena = view.findViewById(R.id.etConfirmarContrasena)
        tilConfirmarContrasena = view.findViewById(R.id.tilConfirmarContrasena)

        etConfirmarContrasena.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}

            override fun afterTextChanged(s: Editable?) {
                val password = etContrasena.text.toString()
                val confirm = s.toString()
                if (confirm != password) {
                    tilConfirmarContrasena.error = "Las contraseñas no coinciden"
                } else {
                    tilConfirmarContrasena.error = null
                }
            }
        })



        etNombreEmpresa = view.findViewById(R.id.etNombreEmpresa)
        etRuc = view.findViewById(R.id.etRuc)

        // Toggle grupo
        toggleGroup.addOnButtonCheckedListener { _, checkedId, isChecked ->
            if (isChecked) {
                layoutEmpresa.visibility = if (checkedId == R.id.btnEmpresa) View.VISIBLE else View.GONE
                resaltarBotones()
            }
        }

        view.findViewById<Button>(R.id.btnRegistrar).setOnClickListener {
            val esEmpresa = toggleGroup.checkedButtonId == R.id.btnEmpresa
            registrarUsuario(esEmpresa)
        }

        view.findViewById<Button>(R.id.btnUbicacion).setOnClickListener {
            ubicacionLauncher.launch(Intent(requireContext(), MapaUbicacionActivity::class.java))
        }

        view.findViewById<ImageView>(R.id.ivBack).setOnClickListener {
            findNavController().navigateUp()
        }
        view.findViewById<TextView>(R.id.tvIniciarSesion).setOnClickListener {
            findNavController().navigateUp()
        }

        return view
    }

    private fun resaltarBotones() {
        val activeScale = 1.1f
        val inactiveScale = 1.0f

        if (toggleGroup.checkedButtonId == R.id.btnCliente) {
            btnCliente.setTextColor(resources.getColor(R.color.white))
            btnCliente.setBackgroundColor(resources.getColor(R.color.ar_primary))
            btnCliente.scaleX = activeScale
            btnCliente.scaleY = activeScale

            btnEmpresa.setTextColor(resources.getColor(R.color.ar_secondary))
            btnEmpresa.setBackgroundColor(resources.getColor(android.R.color.transparent))
            btnEmpresa.scaleX = inactiveScale
            btnEmpresa.scaleY = inactiveScale
        } else {
            btnEmpresa.setTextColor(resources.getColor(R.color.white))
            btnEmpresa.setBackgroundColor(resources.getColor(R.color.ar_secondary))
            btnEmpresa.scaleX = activeScale
            btnEmpresa.scaleY = activeScale

            btnCliente.setTextColor(resources.getColor(R.color.ar_primary))
            btnCliente.setBackgroundColor(resources.getColor(android.R.color.transparent))
            btnCliente.scaleX = inactiveScale
            btnCliente.scaleY = inactiveScale
        }
    }

    private fun registrarUsuario(esEmpresa: Boolean) {
        val correo = etCorreo.text.toString().trim()
        val contrasena = etContrasena.text.toString().trim()

        // Validación básica antes de continuar
        if (correo.isEmpty() || contrasena.isEmpty()) {
            Toast.makeText(requireContext(), "Correo y contraseña son obligatorios", Toast.LENGTH_SHORT).show()
            return
        }

        val auth = FirebaseAuth.getInstance()
        val db = FirebaseFirestore.getInstance()

        auth.createUserWithEmailAndPassword(correo, contrasena)
            .addOnSuccessListener { authResult ->
                val uid = authResult.user?.uid ?: return@addOnSuccessListener

                val fechaRegistro = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(Date())

                val user = hashMapOf(
                    "nombre" to etNombre.text.toString(),
                    "apellido" to etApellido.text.toString(),
                    "correo" to correo,
                    "telefono" to etTelefono.text.toString(),
                    "dni" to etDni.text.toString(),
                    "tipo_usuario" to if (esEmpresa) "empresa" else "cliente",
                    "fecha_registro" to fechaRegistro
                )

                if (esEmpresa) {
                    user["nombre_empresa"] = etNombreEmpresa.text.toString()
                    user["ruc"] = etRuc.text.toString()
                    user["latitud"] = latitud ?: ""
                    user["longitud"] = longitud ?: ""
                    user["estado"] = "pendiente"
                    user["fecha_aprobacion"] = ""

                    db.collection("empresas").document(uid).set(user)
                        .addOnSuccessListener {
                            Toast.makeText(context, "Empresa registrada correctamente", Toast.LENGTH_SHORT).show()
                            findNavController().navigateUp()
                        }
                        .addOnFailureListener {
                            Toast.makeText(context, "Error al guardar en Firestore", Toast.LENGTH_SHORT).show()
                        }
                } else {
                    db.collection("clientes").document(uid).set(user)
                        .addOnSuccessListener {
                            Toast.makeText(context, "Cliente registrado correctamente", Toast.LENGTH_SHORT).show()
                            findNavController().navigateUp()
                        }
                        .addOnFailureListener {
                            Toast.makeText(context, "Error al guardar en Firestore", Toast.LENGTH_SHORT).show()
                        }
                }
            }
            .addOnFailureListener { e ->
                Toast.makeText(context, "Error al crear cuenta: ${e.message}", Toast.LENGTH_LONG).show()
            }
    }


}
