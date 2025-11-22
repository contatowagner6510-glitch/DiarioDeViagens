package com.example.diariodeviagens

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.widget.Toolbar
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import org.osmdroid.config.Configuration
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.BoundingBox
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Marker

class DetalhesViagemActivity : AppCompatActivity() {

    // Propriedades para os dados da viagem
    private var viagemId: String? = null
    private var viagemNome: String? = null
    private var viagemDataInicio: String? = null
    private var viagemDataFim: String? = null
    private var viagemLatitude: Double = 0.0
    private var viagemLongitude: Double = 0.0

    // Propriedades para as Views e o Banco de Dados
    private lateinit var mapView: MapView
    private lateinit var textViewNome: TextView
    private lateinit var textViewDatas: TextView
    private lateinit var db: FirebaseFirestore

    // Propriedades para a lista de Pontos Visitados
    private lateinit var recyclerViewPontos: RecyclerView
    private lateinit var pontosAdapter: PontoVisitadoAdapter
    private var listaDePontos = mutableListOf<PontoVisitado>()
    private lateinit var fabAdicionarPonto: FloatingActionButton
    private lateinit var textViewListaVazia: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Configuração do OSMDroid antes de inflar o layout
        Configuration.getInstance().load(applicationContext, getSharedPreferences("osmdroid", MODE_PRIVATE))
        setContentView(R.layout.activity_detalhes_viagem)

        // --- CONFIGURAÇÃO DA TOOLBAR ---
        val toolbar: Toolbar = findViewById(R.id.toolbar_detalhes)
        setSupportActionBar(toolbar)
        // -----------------------------

        // Inicialização das Views
        mapView = findViewById(R.id.mapView)
        textViewNome = findViewById(R.id.textViewNomeViagemDetalhes)
        textViewDatas = findViewById(R.id.textViewDatasViagemDetalhes)
        recyclerViewPontos = findViewById(R.id.recyclerViewPontosVisitados)
        fabAdicionarPonto = findViewById(R.id.fabAdicionarPonto)
        textViewListaVazia = findViewById(R.id.textViewListaVazia)
        db = FirebaseFirestore.getInstance()

        viagemId = intent.getStringExtra("VIAGEM_ID")

        // Configuração da RecyclerView de Pontos Visitados
        pontosAdapter = PontoVisitadoAdapter(
            listaDePontos,
            onItemClick = { ponto -> abrirTelaEdicaoPonto(ponto) },
            onItemLongClick = { ponto -> mostrarDialogoExclusaoPonto(ponto) }
        )
        recyclerViewPontos.layoutManager = LinearLayoutManager(this)
        recyclerViewPontos.adapter = pontosAdapter

        fabAdicionarPonto.setOnClickListener {
            val intent = Intent(this, AdicionarPontoActivity::class.java)
            intent.putExtra("VIAGEM_ID", viagemId)
            startActivity(intent)
        }

        configurarMapa()
    }

    override fun onResume() {
        super.onResume()
        mapView.onResume()
        buscarViagemDoFirebase()
        ouvirAtualizacoesDosPontos()
    }

    override fun onPause() {
        super.onPause()
        mapView.onPause()
    }

    private fun buscarViagemDoFirebase() {
        if (viagemId == null) {
            Toast.makeText(this, "Erro: ID da viagem não encontrado.", Toast.LENGTH_SHORT).show()
            finish()
            return
        }
        db.collection("viagens").document(viagemId!!).get().addOnSuccessListener { document ->
            if (document != null && document.exists()) {
                val viagem = document.toObject(Viagem::class.java)
                if (viagem != null) {
                    this.viagemNome = viagem.nome
                    this.viagemDataInicio = viagem.dataInicio
                    this.viagemDataFim = viagem.dataFim
                    this.viagemLatitude = viagem.latitude
                    this.viagemLongitude = viagem.longitude
                    atualizarUI()
                }
            } else {
                Toast.makeText(this, "Viagem não encontrada.", Toast.LENGTH_SHORT).show()
                finish()
            }
        }.addOnFailureListener { e ->
            Toast.makeText(this, "Erro ao buscar dados: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

    private fun ouvirAtualizacoesDosPontos() {
        if (viagemId == null) return
        db.collection("viagens").document(viagemId!!).collection("pontosVisitados")
            .orderBy("nomeLocal", Query.Direction.ASCENDING)
            .addSnapshotListener { snapshots, e ->
                if (e != null) {
                    Log.w("DetalhesViagem", "Erro ao ouvir pontos visitados.", e)
                    return@addSnapshotListener
                }
                if (snapshots != null) {
                    val pontosComId = snapshots.documents.mapNotNull { document ->
                        val ponto = document.toObject(PontoVisitado::class.java)
                        ponto?.copy(id = document.id)
                    }
                    pontosAdapter.updateData(pontosComId)
                    listaDePontos.clear()
                    listaDePontos.addAll(pontosComId)
                    atualizarMarcadoresNoMapa()

                    if (pontosComId.isEmpty()) {
                        recyclerViewPontos.visibility = View.GONE
                        textViewListaVazia.visibility = View.VISIBLE
                    } else {
                        recyclerViewPontos.visibility = View.VISIBLE
                        textViewListaVazia.visibility = View.GONE
                    }
                }
            }
    }

    private fun atualizarUI() {
        supportActionBar?.title = viagemNome // Atualiza o título na Toolbar
        textViewNome.text = viagemNome
        textViewDatas.text = "$viagemDataInicio - $viagemDataFim"
        atualizarMarcadoresNoMapa()
    }

    private fun atualizarMarcadoresNoMapa() {
        mapView.overlays.clear()
        val geoPoints = mutableListOf<GeoPoint>()

        if (viagemLatitude != 0.0 || viagemLongitude != 0.0) {
            val pontoViagem = GeoPoint(viagemLatitude, viagemLongitude)
            geoPoints.add(pontoViagem)
            val marcadorViagem = Marker(mapView)
            marcadorViagem.position = pontoViagem
            marcadorViagem.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
            marcadorViagem.icon = resources.getDrawable(R.drawable.ic_custom_marker, theme)
            marcadorViagem.title = "Início da Viagem: $viagemNome"
            mapView.overlays.add(marcadorViagem)
        }

        for (ponto in listaDePontos) {
            if (ponto.latitude != 0.0 || ponto.longitude != 0.0) {
                val pontoVisitado = GeoPoint(ponto.latitude, ponto.longitude)
                geoPoints.add(pontoVisitado)
                val marcadorPonto = Marker(mapView)
                marcadorPonto.position = pontoVisitado
                marcadorPonto.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
                marcadorPonto.icon = resources.getDrawable(R.drawable.ic_ponto_visitado_marker, theme)
                marcadorPonto.title = ponto.nomeLocal
                mapView.overlays.add(marcadorPonto)
            }
        }

        if (geoPoints.size > 1) {
            mapView.post {
                val boundingBox = BoundingBox.fromGeoPoints(geoPoints)
                mapView.zoomToBoundingBox(boundingBox, true, 100)
            }
        } else if (geoPoints.size == 1) {
            mapView.controller.setZoom(15.0)
            mapView.controller.animateTo(geoPoints[0])
        }

        mapView.invalidate()
    }

    private fun configurarMapa() {
        mapView.setTileSource(TileSourceFactory.MAPNIK)
        mapView.setMultiTouchControls(true)
        mapView.controller.setZoom(12.0)
    }

    private fun abrirTelaEdicaoPonto(ponto: PontoVisitado) {
        val intent = Intent(this, AdicionarPontoActivity::class.java).apply {
            putExtra("VIAGEM_ID", viagemId)
            putExtra("PONTO_ID", ponto.id)
            putExtra("PONTO_NOME", ponto.nomeLocal)
            putExtra("PONTO_NOTAS", ponto.notas)
            putExtra("PONTO_URL_FOTO", ponto.urlFoto)
        }
        startActivity(intent)
    }

    private fun mostrarDialogoExclusaoPonto(ponto: PontoVisitado) {
        AlertDialog.Builder(this)
            .setTitle("Excluir Ponto")
            .setMessage("Tem certeza que deseja excluir o ponto '${ponto.nomeLocal}'?")
            .setPositiveButton("Excluir") { _, _ -> excluirPontoDoFirebase(ponto) }
            .setNegativeButton("Cancelar", null)
            .show()
    }

    private fun excluirPontoDoFirebase(ponto: PontoVisitado) {
        if (viagemId == null || ponto.id.isEmpty()) {
            Toast.makeText(this, "Erro: Não foi possível excluir o ponto.", Toast.LENGTH_SHORT).show()
            return
        }
        db.collection("viagens").document(viagemId!!).collection("pontosVisitados").document(ponto.id)
            .delete()
            .addOnSuccessListener { Toast.makeText(this, "Ponto excluído com sucesso.", Toast.LENGTH_SHORT).show() }
            .addOnFailureListener { e -> Toast.makeText(this, "Erro ao excluir: ${e.message}", Toast.LENGTH_LONG).show() }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_detalhes_viagem, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_share -> {
                compartilharViagem()
                true
            }
            R.id.action_edit -> {
                abrirTelaDeEdicao()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun compartilharViagem() {
        val textoCompartilhado = StringBuilder()
        textoCompartilhado.append("✈️ *Minha Viagem: ${viagemNome}* (${viagemDataInicio} - ${viagemDataFim})\n\n")
        if (listaDePontos.isNotEmpty()) {
            textoCompartilhado.append("📍 *Lugares que visitei:*\n")
            listaDePontos.forEach { ponto ->
                textoCompartilhado.append("- ${ponto.nomeLocal}\n")
                if (ponto.notas.isNotEmpty()) {
                    textoCompartilhado.append("  *Anotação:* _${ponto.notas}_\n")
                }
            }
        } else {
            textoCompartilhado.append("Ainda não adicionei nenhum ponto visitado a esta viagem.\n")
        }
        textoCompartilhado.append("\nEnviado pelo meu app Diário de Viagens!")

        val shareIntent = Intent(Intent.ACTION_SEND)
        shareIntent.type = "text/plain"
        shareIntent.putExtra(Intent.EXTRA_TEXT, textoCompartilhado.toString())
        startActivity(Intent.createChooser(shareIntent, "Compartilhar Viagem Via"))
    }

    private fun abrirTelaDeEdicao() {
        val intent = Intent(this, AdicionarViagemActivity::class.java).apply {
            putExtra("VIAGEM_ID", viagemId)
            putExtra("VIAGEM_NOME", viagemNome)
            putExtra("VIAGEM_DATA_INICIO", viagemDataInicio)
            putExtra("VIAGEM_DATA_FIM", viagemDataFim)
        }
        startActivity(intent)
    }
}
