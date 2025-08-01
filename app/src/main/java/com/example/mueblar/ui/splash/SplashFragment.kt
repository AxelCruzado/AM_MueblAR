package com.example.mueblar.ui.splash

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import android.view.animation.AnimationUtils
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.mueblar.R
import com.example.mueblar.databinding.FragmentSplashBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class SplashFragment : Fragment(R.layout.fragment_splash) {

    private lateinit var binding: FragmentSplashBinding
    private val auth = FirebaseAuth.getInstance()
    private val db = FirebaseFirestore.getInstance()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = FragmentSplashBinding.bind(view)

        startAnimations()

        Handler(Looper.getMainLooper()).postDelayed({
            checkUserAndNavigate()
        }, 2500)
    }

    private fun startAnimations() {
        val context = requireContext()

        // Animaciones cargadas desde res/anim
        val fadeIn = AnimationUtils.loadAnimation(context, R.anim.splash_fade_in)
        val bounce = AnimationUtils.loadAnimation(context, R.anim.logo_bounce)
        val slideUp = AnimationUtils.loadAnimation(context, R.anim.text_slide_up)
        val pulse = AnimationUtils.loadAnimation(context, R.anim.loading_pulse)

        // Aplica animaciones a los elementos visuales
        binding.mainContainer.startAnimation(fadeIn)

        Handler(Looper.getMainLooper()).postDelayed({
            binding.logoImageView.startAnimation(bounce)
        }, 400)

        Handler(Looper.getMainLooper()).postDelayed({
            binding.appName.startAnimation(slideUp)
        }, 800)

        Handler(Looper.getMainLooper()).postDelayed({
            binding.appSubtitle.startAnimation(slideUp)
        }, 1200)

        Handler(Looper.getMainLooper()).postDelayed({
            binding.progressContainer.startAnimation(fadeIn)
        }, 1600)

        Handler(Looper.getMainLooper()).postDelayed({
            binding.loadingText.startAnimation(pulse)
        }, 2000)
    }

    private fun checkUserAndNavigate() {
        auth.currentUser?.let { user ->
            db.collection("clientes").document(user.uid).get()
                .addOnSuccessListener { document ->
                    if (document.exists()) {
                        navigateBasedOnUserType(
                            document.getString("tipo_usuario"),
                            document.getString("estado")
                        )
                    } else {
                        checkEmpresaUser(user.uid)
                    }
                }
                .addOnFailureListener {
                    navigateToLogin()
                }
        } ?: run {
            navigateToLogin()
        }
    }

    private fun checkEmpresaUser(userId: String) {
        db.collection("empresas").document(userId).get()
            .addOnSuccessListener { document ->
                if (document.exists()) {
                    navigateBasedOnUserType(
                        "empresa",
                        document.getString("estado")
                    )
                } else {
                    auth.signOut()
                    navigateToLogin()
                }
            }
            .addOnFailureListener {
                navigateToLogin()
            }
    }

    private fun navigateBasedOnUserType(tipoUsuario: String?, estadoEmpresa: String?) {
        val destination = when (tipoUsuario) {
            "cliente" -> R.id.clientMainFragment
            "admin" -> R.id.adminMainFragment
            "empresa" -> if (estadoEmpresa == "aprobado") {
                R.id.empresaMainFragment
            } else {
                R.id.loginFragment
            }
            else -> R.id.loginFragment
        }

        findNavController().navigate(destination)
    }

    private fun navigateToLogin() {
        findNavController().navigate(R.id.loginFragment)
    }
}
