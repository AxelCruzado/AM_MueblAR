package com.example.mueblar

import android.os.Bundle

import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.mueblar.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding  // ViewBinding opcional (recomendado)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Usando ViewBinding:
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Aplicar insets (barra superior, navegación) de forma automática:
        ViewCompat.setOnApplyWindowInsetsListener(binding.navHostFragment) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
    }
}
