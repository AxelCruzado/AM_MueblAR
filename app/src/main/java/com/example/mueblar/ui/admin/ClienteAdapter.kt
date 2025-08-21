package com.example.mueblar.ui.admin

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.mueblar.R
import com.example.mueblar.data.model.Cliente

class ClienteAdapter(private val listaClientes: List<Cliente>) :
    RecyclerView.Adapter<ClienteAdapter.ClienteViewHolder>() {

    class ClienteViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val imgCliente: ImageView = view.findViewById(R.id.imgCliente)
        val txtNombre: TextView = view.findViewById(R.id.txtNombre)
        val txtCorreo: TextView = view.findViewById(R.id.txtCorreo)
        val txtTelefono: TextView = view.findViewById(R.id.txtTelefono)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ClienteViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_cliente, parent, false)
        return ClienteViewHolder(view)
    }

    override fun onBindViewHolder(holder: ClienteViewHolder, position: Int) {
        val cliente = listaClientes[position]
        holder.txtNombre.text = "${cliente.nombre} ${cliente.apellido}"
        holder.txtCorreo.text = cliente.correo
        holder.txtTelefono.text = cliente.telefono

        Glide.with(holder.itemView.context)
            .load(cliente.imagen)
            .placeholder(R.drawable.mueblarlogos)
            .error(R.drawable.app_mueblar)
            .circleCrop()
            .into(holder.imgCliente)
    }

    override fun getItemCount(): Int = listaClientes.size
}
