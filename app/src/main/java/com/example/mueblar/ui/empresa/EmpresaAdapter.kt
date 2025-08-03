package com.example.mueblar.ui.empresa

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.mueblar.data.model.Empresa
import com.example.mueblar.databinding.ItemEmpresaBinding

class EmpresaAdapter(
    private val empresas: List<Empresa>,
    private val onAceptarClick: (Empresa) -> Unit,
    private val onRechazarClick: (Empresa) -> Unit
) : RecyclerView.Adapter<EmpresaAdapter.EmpresaViewHolder>() {

    inner class EmpresaViewHolder(private val binding: ItemEmpresaBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(empresa: Empresa) {
            binding.tvNombreEmpresa.text = empresa.nombre_empresa
            binding.tvRuc.text = "RUC: ${empresa.ruc}"
            binding.tvCorreo.text = "Correo: ${empresa.correo}"
            binding.btnAceptar.setOnClickListener { onAceptarClick(empresa) }
            binding.btnRechazar.setOnClickListener { onRechazarClick(empresa) }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): EmpresaViewHolder {
        val binding = ItemEmpresaBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return EmpresaViewHolder(binding)
    }

    override fun onBindViewHolder(holder: EmpresaViewHolder, position: Int) {
        holder.bind(empresas[position])
    }

    override fun getItemCount(): Int = empresas.size
}