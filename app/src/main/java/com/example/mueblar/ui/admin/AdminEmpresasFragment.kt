package com.example.mueblar.ui.admin

import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.mueblar.R
import com.example.mueblar.data.model.Empresa
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

class AdminEmpresasFragment : Fragment(R.layout.fragment_admin_empresas) {

    private val db = FirebaseFirestore.getInstance()
    private val empresaAdapter = EmpresaAdapter()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val recyclerView = view.findViewById<RecyclerView>(R.id.recyclerViewEmpresas)
        setupRecyclerView(recyclerView)
        fetchEmpresas()
    }

    private fun setupRecyclerView(recyclerView: RecyclerView) {
        recyclerView.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = empresaAdapter
        }
    }

    private fun fetchEmpresas() {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val snapshot = db.collection("empresas").get().await()
                snapshot.documents.forEach { doc ->
                    Log.d("AdminEmpresasFragment", "Documento crudo: ${doc.data}")
                    val nombreEmpresa = doc.getString("nombre_empresa") ?: "No encontrado"
                    Log.d("AdminEmpresasFragment", "Campo nombre_empresa: $nombreEmpresa")
                }
                val empresas = snapshot.toObjects(Empresa::class.java)
                empresas.forEach { empresa ->
                    Log.d("AdminEmpresasFragment", "Empresa mapeada: id=${empresa.id}, nombreEmpresa=${empresa.nombre_empresa}, correo=${empresa.correo}, telefono=${empresa.telefono}, imagen=${empresa.imagen}")
                }
                withContext(Dispatchers.Main) {
                    empresaAdapter.submitList(empresas)
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(context, "Error al cargar empresas: ${e.message}", Toast.LENGTH_LONG).show()
                }
            }
        }
    }
}