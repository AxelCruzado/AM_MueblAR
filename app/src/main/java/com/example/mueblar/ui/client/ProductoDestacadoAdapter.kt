package com.example.mueblar.ui.client.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.mueblar.R
import com.example.mueblar.data.model.Producto
import com.example.mueblar.databinding.ItemProductoDestacadoBinding

class ProductoDestacadoAdapter(
    private val productos: List<Producto>,
    private val onClick: (Producto) -> Unit // Añadido para manejar clics
) : RecyclerView.Adapter<ProductoDestacadoAdapter.ProductoViewHolder>() {

    class ProductoViewHolder(val binding: ItemProductoDestacadoBinding)
        : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ProductoViewHolder {
        val binding = ItemProductoDestacadoBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return ProductoViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ProductoViewHolder, position: Int) {
        val producto = productos[position]
        with(holder.binding) {
            tvNombreProducto.text = producto.nombre
            Glide.with(root.context)
                .load(producto.imagenUrl)
                .placeholder(R.drawable.ic_launcher_foreground)
                .into(imgProducto)
            // Manejar clics
            root.setOnClickListener { onClick(producto) }
        }
    }

    override fun getItemCount(): Int = productos.size
}