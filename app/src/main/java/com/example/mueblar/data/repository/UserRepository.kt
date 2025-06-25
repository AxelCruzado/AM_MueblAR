package com.example.mueblar.data.repository
import com.example.mueblar.data.model.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class UserRepository {
    private val auth = FirebaseAuth.getInstance()
    private val db = FirebaseFirestore.getInstance()

    fun registerUser(user: User, password: String, onResult: (Boolean, String) -> Unit) {
        auth.createUserWithEmailAndPassword(user.correo, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val uid = auth.currentUser?.uid
                    uid?.let {
                        db.collection("usuarios").document(it)
                            .set(user)
                            .addOnSuccessListener {
                                onResult(true, "Usuario registrado correctamente")
                            }
                            .addOnFailureListener { e ->
                                onResult(false, "Error Firestore: ${e.message}")
                            }
                    } ?: onResult(false, "Error al obtener UID")
                } else {
                    onResult(false, "Error Auth: ${task.exception?.message}")
                }
            }
    }

    fun loginUser(email: String, password: String, onResult: (Boolean, String) -> Unit) {
        auth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    onResult(true, "Login exitoso")
                } else {
                    onResult(false, "Error Auth: ${task.exception?.message}")
                }
            }
    }
}
