
package com.example.mueblar.ui.ar

import android.os.Bundle
import android.util.Log
import android.view.*
import android.widget.FrameLayout
import android.widget.ImageButton
import android.widget.ProgressBar
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.google.ar.core.Anchor
import com.google.ar.core.Config
import com.google.ar.core.Plane
import io.github.sceneview.ar.ARSceneView
import io.github.sceneview.ar.node.AnchorNode
import io.github.sceneview.model.ModelInstance
import io.github.sceneview.node.ModelNode
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.File
import java.io.FileOutputStream
import java.security.MessageDigest
import java.util.concurrent.TimeUnit

class ARViewerFragment : Fragment() {

    private lateinit var arSceneView: ARSceneView
    private lateinit var backButton: ImageButton
    private lateinit var loadingSpinner: ProgressBar
    private lateinit var modeloUrl: String
    private var cachedModelInstance: ModelInstance? = null
    private val TAG = "ARViewerFragment"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        modeloUrl = requireArguments().getString("modeloArUrl") ?: ""
        Log.d(TAG, "modeloUrl: $modeloUrl")
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        val root = FrameLayout(requireContext()).apply {
            layoutParams = FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
            )
        }

        arSceneView = ARSceneView(requireContext()).apply {
            layoutParams = FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
            )
            // Configurar ARCore para usar estimación de luz ambiental
            sessionConfiguration = { session, config ->
                config.lightEstimationMode = Config.LightEstimationMode.ENVIRONMENTAL_HDR
                Log.d(TAG, "LightEstimationMode set to ENVIRONMENTAL_HDR")
            }
        }

        backButton = ImageButton(requireContext()).apply {
            setImageResource(android.R.drawable.ic_menu_close_clear_cancel)
            setBackgroundResource(android.R.color.transparent)
            layoutParams = FrameLayout.LayoutParams(150, 150).apply {
                marginStart = 32
                topMargin = 32
            }
            setOnClickListener { findNavController().navigateUp() }
        }

        loadingSpinner = ProgressBar(requireContext()).apply {
            layoutParams = FrameLayout.LayoutParams(150, 150, Gravity.CENTER)
            visibility = View.GONE
        }

        root.addView(arSceneView)
        root.addView(backButton)
        root.addView(loadingSpinner)
        return root
    }

    override fun onResume() {
        super.onResume()
        Toast.makeText(requireContext(), "Cargando entorno AR...", Toast.LENGTH_SHORT).show()
        arSceneView.onTouchEvent = { motionEvent, _ ->
            if (motionEvent.action == MotionEvent.ACTION_UP) {
                performHitTestAndPlace(motionEvent)
                true
            } else false
        }
        // Log para verificar el estado de la sesión AR
        arSceneView.onSessionCreated = { session ->
            Log.d(TAG, "AR Session created successfully")
        }
        arSceneView.onSessionFailed = { exception ->
            Log.e(TAG, "AR Session failed: ${exception.message}")
            Toast.makeText(requireContext(), "Error en la sesión AR: ${exception.message}", Toast.LENGTH_LONG).show()
        }
    }

    private fun performHitTestAndPlace(event: MotionEvent) {
        val frame = arSceneView.session?.update() ?: return
        val hit = frame.hitTest(event)
            .firstOrNull { it.trackable is Plane && it.hitPose != null } ?: return
        placeModelAtAnchor(hit.createAnchor())
    }

    private fun placeModelAtAnchor(anchor: Anchor) {
        lifecycleScope.launch {
            loadingSpinner.visibility = View.VISIBLE
            try {
                val modelFile = getCachedModelFile(modeloUrl)
                Log.d(TAG, "Attempting to load model from: ${modelFile.absolutePath}, size: ${modelFile.length()} bytes")

                // Validar el archivo local
                if (!modelFile.exists() || modelFile.length() == 0L) {
                    Log.e(TAG, "Invalid or empty cache file: ${modelFile.absolutePath}")
                    modelFile.delete() // Eliminar archivo inválido
                    throw Exception("Archivo local inválido o vacío: ${modelFile.absolutePath}")
                }

                val modelInstance = cachedModelInstance ?: try {
                    // Usar prefijo file:// para compatibilidad
                    arSceneView.modelLoader.loadModelInstance(fileLocation = "file://${modelFile.absolutePath}")
                        ?.also { cachedModelInstance = it }
                } catch (e: Exception) {
                    Log.e(TAG, "Failed to load model from cache: ${e.message}")
                    modelFile.delete() // Eliminar archivo posiblemente corrupto
                    Toast.makeText(requireContext(), "Error al cargar desde caché: ${e.message}. Intentando desde URL...", Toast.LENGTH_LONG).show()
                    arSceneView.modelLoader.loadModelInstance(fileLocation = modeloUrl)
                        ?.also { cachedModelInstance = it }
                }

                loadingSpinner.visibility = View.GONE

                if (modelInstance == null) {
                    Log.e(TAG, "Failed to load model instance")
                    Toast.makeText(requireContext(), "Error: No se pudo cargar el modelo 3D", Toast.LENGTH_LONG).show()
                    return@launch
                }

                Log.d(TAG, "Model loaded successfully")
                val modelNode = ModelNode(modelInstance = modelInstance)
                val anchorNode = AnchorNode(engine = arSceneView.engine, anchor = anchor).apply {
                    addChildNode(modelNode)
                }
                arSceneView.addChildNode(anchorNode)
                Log.d(TAG, "Model rendered with ${modelNode.materialInstances.size} materials")
            } catch (e: Exception) {
                loadingSpinner.visibility = View.GONE
                Log.e(TAG, "Error in placeModelAtAnchor: ${e.message}")
                Toast.makeText(requireContext(), "Error: ${e.message}", Toast.LENGTH_LONG).show()
            }
        }
    }

    private suspend fun getCachedModelFile(url: String): File = withContext(Dispatchers.IO) {
        val fileName = url.toMD5() + ".glb"
        val cacheFile = File(requireContext().cacheDir, fileName)
        Log.d(TAG, "Checking cache file: ${cacheFile.absolutePath}")

        if (cacheFile.exists() && cacheFile.length() > 0) {
            Log.d(TAG, "Cache file exists, size: ${cacheFile.length()} bytes")
            return@withContext cacheFile
        }

        Log.d(TAG, "Downloading model from: $url")
        val client = OkHttpClient.Builder()
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .build()
        val request = Request.Builder().url(url).build()
        val response = client.newCall(request).execute()

        if (!response.isSuccessful) {
            Log.e(TAG, "Download failed: ${response.message}")
            throw Exception("Error al descargar el modelo: ${response.message}")
        }

        Log.d(TAG, "Saving model to cache: ${cacheFile.absolutePath}")
        response.body?.byteStream()?.use { input ->
            FileOutputStream(cacheFile).use { output ->
                input.copyTo(output)
            }
        }
        Log.d(TAG, "Model saved to cache, size: ${cacheFile.length()} bytes")

        return@withContext cacheFile
    }

    private fun String.toMD5(): String {
        val bytes = MessageDigest.getInstance("MD5").digest(this.toByteArray())
        return bytes.joinToString("") { "%02x".format(it) }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        arSceneView.destroy()
        cachedModelInstance = null
    }
}
