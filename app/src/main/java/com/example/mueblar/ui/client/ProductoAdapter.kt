package com.example.mueblar.ui.client

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
import com.example.mueblar.data.model.Producto

class ProductoAdapter(
    private val onFavoriteClick: (Producto, Boolean) -> Unit,
    private val onItemClick: (Producto) -> Unit // Nuevo callback para clics en el elemento
) : ListAdapter<Producto, ProductoAdapter.ProductoViewHolder>(ProductoDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ProductoViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_producto, parent, false)
        return ProductoViewHolder(view)
    }

    override fun onBindViewHolder(holder: ProductoViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class ProductoViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val ivProductImage: ImageView = itemView.findViewById(R.id.ivProductImage)
        private val tvProductName: TextView = itemView.findViewById(R.id.tvProductName)
        private val tvProductDescription: TextView = itemView.findViewById(R.id.tvProductDescription)
        private val tvProductPrice: TextView = itemView.findViewById(R.id.tvProductPrice)
        private val ivFavorite: ImageView = itemView.findViewById(R.id.ivFavorite)

        fun bind(producto: Producto) {
            tvProductName.text = producto.nombre
            tvProductDescription.text = producto.descripcion
            tvProductPrice.text = "S/. ${producto.precio}"
            Glide.with(itemView.context)
                .load(producto.imagenUrl)
                .placeholder(R.drawable.ic_badge)
                .into(ivProductImage)

            // Actualizar ícono de favorito según estado
            ivFavorite.setImageResource(
                if (producto.isFavorite) R.drawable.ic_onfavorite
                else R.drawable.ic_offfavorite
            )

            // Listener para clic en favorito
            ivFavorite.setOnClickListener {
                onFavoriteClick(producto, !producto.isFavorite)
            }

            // Listener para clic en el elemento completo
            itemView.setOnClickListener {
                onItemClick(producto)
            }
        }
    }

    private class ProductoDiffCallback : DiffUtil.ItemCallback<Producto>() {
        override fun areItemsTheSame(oldItem: Producto, newItem: Producto): Boolean {
            return oldItem.productoId == newItem.productoId
        }

        override fun areContentsTheSame(oldItem: Producto, newItem: Producto): Boolean {
            return oldItem == newItem
        }
    }

    fun updateFavoriteStatus(productoId: String, isFavorite: Boolean) {
        val producto = currentList.find { it.productoId == productoId }
        producto?.isFavorite = isFavorite
        notifyItemChanged(currentList.indexOf(producto))
    }
}