package com.example.mueblar.ui.admin

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.mueblar.R
import com.example.mueblar.data.model.Cliente
import com.google.firebase.firestore.FirebaseFirestore

class AdminClientesFragment : Fragment() {

    private lateinit var recyclerClientes: RecyclerView
    private lateinit var clienteAdapter: ClienteAdapter
    private val listaClientes = mutableListOf<Cliente>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_admin_clientes, container, false)
        recyclerClientes = view.findViewById(R.id.recyclerClientes)
        recyclerClientes.layoutManager = LinearLayoutManager(context)

        clienteAdapter = ClienteAdapter(listaClientes)
        recyclerClientes.adapter = clienteAdapter

        obtenerClientes()

        return view
    }

    private fun obtenerClientes() {
        val db = FirebaseFirestore.getInstance()
        db.collection("clientes")
            .whereEqualTo("tipo_usuario", "cliente")
            .get()
            .addOnSuccessListener { documentos ->
                listaClientes.clear()
                for (documento in documentos) {
                    val cliente = documento.toObject(Cliente::class.java)
                    listaClientes.add(cliente)
                }
                clienteAdapter.notifyDataSetChanged()
            }
            .addOnFailureListener { e ->
                // Aquí puedes mostrar un Toast o Log
                e.printStackTrace()
            }
    }
}
