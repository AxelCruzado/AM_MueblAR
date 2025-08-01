package com.example.mueblar.ui.empresa

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.mueblar.R
import com.example.mueblar.data.model.Producto
import com.example.mueblar.databinding.ItemProductoEmpresaBinding

class EmpresaProductoAdapter(
    private val productos: List<Producto>,
    private val onEditarClick: (Producto) -> Unit,
    private val onEliminarClick: (Producto) -> Unit
) : RecyclerView.Adapter<EmpresaProductoAdapter.ProductoViewHolder>() {

    inner class ProductoViewHolder(val binding: ItemProductoEmpresaBinding) :
        RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ProductoViewHolder {
        val binding = ItemProductoEmpresaBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return ProductoViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ProductoViewHolder, position: Int) {
        val producto = productos[position]
        with(holder.binding) {
            tvNombreProducto.text = producto.nombre
            tvDescripcionProducto.text = producto.descripcion
            tvPrecioProducto.text = "S/ %.2f".format(producto.precio)
            tvEstadoProducto.text = if (producto.disponible) "Disponible" else "No disponible"
            tvEstadoProducto.setTextColor(
                root.context.getColor(
                    if (producto.disponible) android.R.color.holo_green_dark
                    else android.R.color.holo_red_dark
                )
            )

            Glide.with(root.context)
                .load(producto.imagenUrl)
                .placeholder(R.drawable.mueblarlogos)
                .into(ivImagenProducto)

            btnEditarProducto.setOnClickListener { onEditarClick(producto) }
            btnEliminarProducto.setOnClickListener { onEliminarClick(producto) }
        }
    }

    override fun getItemCount(): Int = productos.size
}
