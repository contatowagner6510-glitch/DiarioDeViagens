package com.example.diariodeviagens


import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query

class MainActivity : AppCompatActivity() {

    // Propriedades para os componentes da UI e banco de dados
    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: ViagemAdapter
    private lateinit var fab: FloatingActionButton
    private lateinit var db: FirebaseFirestore

    // Lista que armazena os dados vindos do Firebase
    private var listaDeViagens = mutableListOf<Viagem>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Inicializa a instância do Firebase Firestore
        db = FirebaseFirestore.getInstance()

        // Encontra os componentes da UI no layout
        recyclerView = findViewById(R.id.recyclerViewViagens)
        fab = findViewById(R.id.fabAdicionarViagem)

        // Configura o Adapter, passando a lista e as funções de clique
        adapter = ViagemAdapter(
            listaDeViagens,
            onItemClick = { viagem -> // O que fazer no clique curto (Abrir Detalhes)
                abrirTelaDeDetalhes(viagem)
            },
            onItemLongClick = { viagem -> // O que fazer no clique longo (Excluir)
                mostrarDialogoDeExclusao(viagem)
            }
        )

        // Configura o RecyclerView com o LayoutManager e o Adapter
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = adapter

        // Configura a ação do botão flutuante para abrir a tela de adicionar
        fab.setOnClickListener {
            val intent = Intent(this, AdicionarViagemActivity::class.java)
            startActivity(intent)
        }

        // Inicia o "ouvinte" de atualizações do Firebase
        ouvirAtualizacoesDoFirebase()
    }

    /**
     * Abre a DetalhesViagemActivity, passando os dados da viagem selecionada.
     */
    private fun abrirTelaDeDetalhes(viagem: Viagem) {
        val intent = Intent(this, DetalhesViagemActivity::class.java).apply {
            // Adiciona os dados da viagem como "extras" para a próxima tela
            putExtra("VIAGEM_ID", viagem.id)
            putExtra("VIAGEM_NOME", viagem.nome)
            putExtra("VIAGEM_DATA_INICIO", viagem.dataInicio)
            putExtra("VIAGEM_DATA_FIM", viagem.dataFim)
            putExtra("VIAGEM_LATITUDE", viagem.latitude)
            putExtra("VIAGEM_LONGITUDE", viagem.longitude)
        }
        startActivity(intent)
    }

    /**
     * Mostra um diálogo de confirmação antes de excluir uma viagem.
     */
    private fun mostrarDialogoDeExclusao(viagem: Viagem) {
        AlertDialog.Builder(this)
            .setTitle("Excluir Viagem")
            .setMessage("Você tem certeza que deseja excluir a viagem '${viagem.nome}'? Esta ação não pode ser desfeita.")
            .setPositiveButton("Excluir") { _, _ ->
                excluirViagemDoFirebase(viagem)
            }
            .setNegativeButton("Cancelar", null)
            .setIcon(android.R.drawable.ic_dialog_alert)
            .show()
    }

    /**
     * Executa a operação de exclusão do documento no Firebase.
     */
    private fun excluirViagemDoFirebase(viagem: Viagem) {
        if (viagem.id.isEmpty()) {
            Toast.makeText(this, "Erro: ID da viagem inválido.", Toast.LENGTH_SHORT).show()
            return
        }

        db.collection("viagens").document(viagem.id)
            .delete()
            .addOnSuccessListener {
                Toast.makeText(this, "Viagem excluída com sucesso!", Toast.LENGTH_SHORT).show()
                // A lista se atualiza sozinha graças ao SnapshotListener
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Erro ao excluir viagem: ${e.message}", Toast.LENGTH_LONG).show()
            }
    }

    /**
     * Configura um listener em tempo real para a coleção 'viagens' no Firestore.
     * A lista na UI será atualizada automaticamente sempre que os dados mudarem.
     */
    private fun ouvirAtualizacoesDoFirebase() {
        db.collection("viagens").orderBy("nome", Query.Direction.ASCENDING)
            .addSnapshotListener { snapshots, e ->
                if (e != null) {
                    Log.w("MainActivity", "Ouvinte falhou.", e)
                    return@addSnapshotListener
                }

                if (snapshots != null) {
                    // Mapeia os documentos recebidos para objetos da classe Viagem,
                    // garantindo que o ID do documento seja incluído.
                    val viagensComId = snapshots.documents.mapNotNull { document ->
                        val viagem = document.toObject(Viagem::class.java)
                        // Cria uma cópia do objeto, inserindo o ID do documento do Firebase
                        viagem?.copy(id = document.id)
                    }
                    // Atualiza o adapter com a nova lista de dados
                    adapter.updateData(viagensComId)
                }
            }
    }
}

