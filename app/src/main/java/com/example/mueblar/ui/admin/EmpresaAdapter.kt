package com.example.mueblar.ui.admin

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.mueblar.R
import com.example.mueblar.data.model.Empresa

class EmpresaAdapter : ListAdapter<Empresa, EmpresaAdapter.EmpresaViewHolder>(EmpresaDiffCallback()) {

    class EmpresaViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val nombreEmpresaTextView: TextView = itemView.findViewById(R.id.textViewNombreEmpresa)
        private val correoTextView: TextView = itemView.findViewById(R.id.textViewCorreo)
        private val telefonoTextView: TextView = itemView.findViewById(R.id.textViewTelefono)
        private val imagenEmpresa: ImageView = itemView.findViewById(R.id.imageViewEmpresa)

        fun bind(empresa: Empresa) {
            nombreEmpresaTextView.text = empresa.nombre_empresa.ifEmpty { "Sin nombre" }
            correoTextView.text = empresa.correo.ifEmpty { "Sin correo" }
            telefonoTextView.text = empresa.telefono.ifEmpty { "Sin teléfono" }
            Log.d("EmpresaAdapter", "Cargando datos: nombre_empresa=${empresa.nombre_empresa}, correo=${empresa.correo}, telefono=${empresa.telefono}")
            Glide.with(itemView.context)
                .load(empresa.imagen)
                .placeholder(R.drawable.ic_empresas_administrador)
                .error(R.drawable.ic_empresas_administrador)
                .into(imagenEmpresa)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): EmpresaViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_empresa_admin, parent, false)
        return EmpresaViewHolder(view)
    }

    override fun onBindViewHolder(holder: EmpresaViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    class EmpresaDiffCallback : DiffUtil.ItemCallback<Empresa>() {
        override fun areItemsTheSame(oldItem: Empresa, newItem: Empresa): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: Empresa, newItem: Empresa): Boolean {
            return oldItem == newItem
        }
    }
}