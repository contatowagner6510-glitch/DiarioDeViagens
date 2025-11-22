package com.example.diariodeviagens


import android.app.DatePickerDialog
import android.location.Geocoder
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.firestore.FirebaseFirestore
import java.io.IOException
import java.util.Calendar

class AdicionarViagemActivity : AppCompatActivity() {

    private lateinit var editTextNome: EditText
    private lateinit var editTextDataInicio: EditText
    private lateinit var editTextDataFim: EditText
    private lateinit var buttonSalvar: Button
    private lateinit var db: FirebaseFirestore
    private var idViagemAtual: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_adicionar_viagem)

        db = FirebaseFirestore.getInstance()
        editTextNome = findViewById(R.id.editTextNomeViagem)
        editTextDataInicio = findViewById(R.id.editTextDataInicio)
        editTextDataFim = findViewById(R.id.editTextDataFim)
        buttonSalvar = findViewById(R.id.buttonSalvarViagem)

        if (intent.hasExtra("VIAGEM_ID")) {
            title = "Editar Viagem"
            idViagemAtual = intent.getStringExtra("VIAGEM_ID")
            editTextNome.setText(intent.getStringExtra("VIAGEM_NOME"))
            editTextDataInicio.setText(intent.getStringExtra("VIAGEM_DATA_INICIO"))
            editTextDataFim.setText(intent.getStringExtra("VIAGEM_DATA_FIM"))
        } else {
            title = "Adicionar Nova Viagem"
        }

        // --- CORREÇÃO DO BUG 1 (Calendário) ---
        // Listeners de clique para os campos de data, agora no lugar correto.
        editTextDataInicio.setOnClickListener {
            mostrarSeletorDeData(editTextDataInicio)
        }

        editTextDataFim.setOnClickListener {
            mostrarSeletorDeData(editTextDataFim)
        }
        // ------------------------------------

        buttonSalvar.setOnClickListener {
            salvarViagem()
        }
    }

    private fun mostrarSeletorDeData(campoData: EditText) {
        val calendario = Calendar.getInstance()
        val ano = calendario.get(Calendar.YEAR)
        val mes = calendario.get(Calendar.MONTH)
        val dia = calendario.get(Calendar.DAY_OF_MONTH)

        DatePickerDialog(this, { _, anoSelecionado, mesSelecionado, diaSelecionado ->
            val mesCorrigido = mesSelecionado + 1
            val dataFormatada = String.format("%02d/%02d/%d", diaSelecionado, mesCorrigido, anoSelecionado)
            campoData.setText(dataFormatada)
        }, ano, mes, dia).show()
    }

    private fun salvarViagem() {
        val nome = editTextNome.text.toString().trim()
        val dataInicio = editTextDataInicio.text.toString().trim()
        val dataFim = editTextDataFim.text.toString().trim()

        if (nome.isEmpty() || dataInicio.isEmpty() || dataFim.isEmpty()) {
            Toast.makeText(this, "Por favor, preencha todos os campos.", Toast.LENGTH_SHORT).show()
            return
        }

        val geocoder = Geocoder(this)
        var latitude = 0.0
        var longitude = 0.0

        try {
            val enderecos = geocoder.getFromLocationName(nome, 1)
            if (enderecos != null && enderecos.isNotEmpty()) {
                val endereco = enderecos[0]
                latitude = endereco.latitude
                longitude = endereco.longitude
            } else {
                Toast.makeText(this, "Aviso: Localização não encontrada para '$nome'.", Toast.LENGTH_LONG).show()
            }
        } catch (e: IOException) {
            Toast.makeText(this, "Aviso: Problema de rede ao buscar localização.", Toast.LENGTH_LONG).show()
        }

        val viagem = hashMapOf(
            "nome" to nome,
            "dataInicio" to dataInicio,
            "dataFim" to dataFim,
            "latitude" to latitude,
            "longitude" to longitude
        )

        val task = if (idViagemAtual == null) {
            db.collection("viagens").add(viagem)
        } else {
            db.collection("viagens").document(idViagemAtual!!).set(viagem)
        }

        task.addOnSuccessListener {
            val mensagem = if (idViagemAtual == null) "Viagem salva!" else "Viagem atualizada!"
            Toast.makeText(this, mensagem, Toast.LENGTH_SHORT).show()
            finish() // Apenas fecha a tela. A MainActivity cuidará do resto.
        }.addOnFailureListener { e ->
            Toast.makeText(this, "Erro ao salvar: ${e.message}", Toast.LENGTH_LONG).show()
        }
    }
}
