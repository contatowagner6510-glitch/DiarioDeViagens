package com.example.diariodeviagens

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Geocoder
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import com.bumptech.glide.Glide
import com.cloudinary.android.MediaManager
import com.cloudinary.android.callback.ErrorInfo
import com.cloudinary.android.callback.UploadCallback
import com.google.firebase.firestore.FirebaseFirestore
import java.io.IOException

class AdicionarPontoActivity : AppCompatActivity() {

    // Propriedades para as Views, DB, etc.
    private lateinit var imageViewPreview: ImageView
    private lateinit var buttonSelecionarFoto: Button
    private lateinit var editTextNomeLocal: EditText
    private lateinit var editTextNotas: EditText
    private lateinit var buttonSalvarPonto: Button
    private lateinit var db: FirebaseFirestore

    // Variáveis de estado
    private var viagemId: String? = null
    private var pontoId: String? = null // Guarda o ID do ponto se estivermos em modo de edição
    private var uriDaImagemSelecionada: Uri? = null
    private var urlDaImagemExistente: String? = null // Guarda a URL da foto se já existir uma

    // Lançador para pedir permissão de acesso à galeria
    private val requestPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted: Boolean ->
            if (isGranted) {
                abrirGaleria()
            } else {
                Toast.makeText(this, "Permissão para acessar a galeria é necessária.", Toast.LENGTH_LONG).show()
            }
        }

    // Lançador para pegar a imagem da galeria
    private val pickImageLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                uriDaImagemSelecionada = result.data?.data
                imageViewPreview.setImageURI(uriDaImagemSelecionada)
                urlDaImagemExistente = null // Se uma nova imagem é selecionada, a antiga é descartada
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_adicionar_ponto)

        // Inicializa DB e Views
        db = FirebaseFirestore.getInstance()
        imageViewPreview = findViewById(R.id.imageViewPontoPreview)
        buttonSelecionarFoto = findViewById(R.id.buttonSelecionarFoto)
        editTextNomeLocal = findViewById(R.id.editTextNomeLocal)
        editTextNotas = findViewById(R.id.editTextNotas)
        buttonSalvarPonto = findViewById(R.id.buttonSalvarPonto)

        viagemId = intent.getStringExtra("VIAGEM_ID")
        if (viagemId == null) {
            Toast.makeText(this, "Erro: ID da viagem não encontrado.", Toast.LENGTH_LONG).show()
            finish()
            return
        }

        // Verifica se estamos em modo de EDIÇÃO ou ADIÇÃO
        if (intent.hasExtra("PONTO_ID")) {
            // MODO DE EDIÇÃO
            title = "Editar Ponto Visitado"
            pontoId = intent.getStringExtra("PONTO_ID")
            urlDaImagemExistente = intent.getStringExtra("PONTO_URL_FOTO")

            editTextNomeLocal.setText(intent.getStringExtra("PONTO_NOME"))
            editTextNotas.setText(intent.getStringExtra("PONTO_NOTAS"))

            // Carrega a imagem existente se houver uma URL
            if (!urlDaImagemExistente.isNullOrEmpty()) {
                Glide.with(this)
                    .load(urlDaImagemExistente)
                    .placeholder(R.drawable.ic_custom_marker)
                    .into(imageViewPreview)
            }
        } else {
            // MODO DE ADIÇÃO
            title = "Adicionar Ponto Visitado"
        }

        // Configura os listeners dos botões
        buttonSelecionarFoto.setOnClickListener { verificarPermissaoEAbrirGaleria() }
        buttonSalvarPonto.setOnClickListener { salvarPontoVisitado() }
    }

    private fun verificarPermissaoEAbrirGaleria() {
        when {
            ContextCompat.checkSelfPermission(this, Manifest.permission.READ_MEDIA_IMAGES) == PackageManager.PERMISSION_GRANTED -> abrirGaleria()
            else -> requestPermissionLauncher.launch(Manifest.permission.READ_MEDIA_IMAGES)
        }
    }

    private fun abrirGaleria() {
        val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        pickImageLauncher.launch(intent)
    }

    private fun salvarPontoVisitado() {
        val nomeLocal = editTextNomeLocal.text.toString().trim()
        if (nomeLocal.isEmpty()) {
            Toast.makeText(this, "O nome do local é obrigatório.", Toast.LENGTH_SHORT).show()
            return
        }

        // Geocodificação para obter coordenadas
        val geocoder = Geocoder(this)
        var latitude = 0.0
        var longitude = 0.0
        try {
            val enderecos = geocoder.getFromLocationName(nomeLocal, 1)
            if (enderecos != null && enderecos.isNotEmpty()) {
                val endereco = enderecos[0]
                latitude = endereco.latitude
                longitude = endereco.longitude
            } else {
                Toast.makeText(this, "Aviso: Localização do ponto não encontrada.", Toast.LENGTH_LONG).show()
            }
        } catch (e: IOException) {
            Toast.makeText(this, "Aviso: Problema de rede ao buscar localização.", Toast.LENGTH_LONG).show()
        }

        // Decide se faz upload de uma nova imagem ou usa a existente
        if (uriDaImagemSelecionada != null) {
            fazerUploadDaImagemComCloudinary(nomeLocal, latitude, longitude)
        } else {
            salvarDadosNoFirestore(nomeLocal, urlDaImagemExistente ?: "", latitude, longitude)
        }
    }

    private fun fazerUploadDaImagemComCloudinary(nomeLocal: String, latitude: Double, longitude: Double) {
        buttonSalvarPonto.isEnabled = false
        Toast.makeText(this, "Salvando foto, por favor aguarde...", Toast.LENGTH_SHORT).show()

        MediaManager.get().upload(uriDaImagemSelecionada)
            .callback(object : UploadCallback {
                override fun onStart(requestId: String) {}
                override fun onProgress(requestId: String, bytes: Long, totalBytes: Long) {}
                override fun onSuccess(requestId: String, resultData: Map<*, *>) {
                    val urlDaImagem = resultData["secure_url"] as String
                    salvarDadosNoFirestore(nomeLocal, urlDaImagem, latitude, longitude)
                }
                override fun onError(requestId: String, error: ErrorInfo) {
                    Log.e("Cloudinary", "Erro no upload: ${error.description}")
                    Toast.makeText(baseContext, "Erro no upload da foto: ${error.description}", Toast.LENGTH_LONG).show()
                    buttonSalvarPonto.isEnabled = true
                }
                override fun onReschedule(requestId: String, error: ErrorInfo) {}
            }).dispatch()
    }

    private fun salvarDadosNoFirestore(nomeLocal: String, urlDaImagem: String, latitude: Double, longitude: Double) {
        val notas = editTextNotas.text.toString().trim()
        val ponto = hashMapOf(
            "nomeLocal" to nomeLocal,
            "notas" to notas,
            "urlFoto" to urlDaImagem,
            "latitude" to latitude,
            "longitude" to longitude
        )

        // Se pontoId for nulo, cria um novo documento. Se não, atualiza o existente.
        val task = if (pontoId == null) {
            db.collection("viagens").document(viagemId!!).collection("pontosVisitados").add(ponto)
        } else {
            db.collection("viagens").document(viagemId!!).collection("pontosVisitados").document(pontoId!!).set(ponto)
        }

        task.addOnSuccessListener {
            Toast.makeText(this, "Ponto salvo com sucesso!", Toast.LENGTH_SHORT).show()
            finish()
        }.addOnFailureListener { e ->
            Toast.makeText(this, "Erro ao salvar: ${e.message}", Toast.LENGTH_LONG).show()
            buttonSalvarPonto.isEnabled = true
        }
    }
}
