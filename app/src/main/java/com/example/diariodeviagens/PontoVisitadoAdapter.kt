package com.example.diariodeviagens


import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import coil.load

class PontoVisitadoAdapter(
    private var pontos: MutableList<PontoVisitado>,
    // --- NOVOS PARÂMETROS PARA OS CLIQUES ---
    private val onItemClick: (PontoVisitado) -> Unit,
    private val onItemLongClick: (PontoVisitado) -> Unit
) : RecyclerView.Adapter<PontoVisitadoAdapter.PontoViewHolder>() {

    class PontoViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val imagemPonto: ImageView = itemView.findViewById(R.id.imageViewPonto)
        val nomeLocal: TextView = itemView.findViewById(R.id.textViewNomeLocal)
        val notas: TextView = itemView.findViewById(R.id.textViewNotas)

        // --- NOVA FUNÇÃO DENTRO DO VIEWHOLDER ---
        fun bind(ponto: PontoVisitado, onItemClick: (PontoVisitado) -> Unit, onItemLongClick: (PontoVisitado) -> Unit) {
            // Configura os listeners de clique para o item inteiro
            itemView.setOnClickListener { onItemClick(ponto) }
            itemView.setOnLongClickListener {
                onItemLongClick(ponto)
                true // Retorna true para indicar que o clique longo foi consumido
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PontoViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_ponto_visitado, parent, false)
        return PontoViewHolder(view)
    }

    override fun onBindViewHolder(holder: PontoViewHolder, position: Int) {
        val ponto = pontos[position]
        // Chama a função bind para configurar os cliques
        holder.bind(ponto, onItemClick, onItemLongClick)

        // O resto da lógica de carregar dados e imagem não muda
        holder.nomeLocal.text = ponto.nomeLocal
        holder.notas.text = ponto.notas

        if (ponto.urlFoto.isNotEmpty()) {
            holder.imagemPonto.load(ponto.urlFoto) {
                crossfade(true)
                placeholder(R.drawable.ic_ponto_visitado_marker) // Mostra um ícone enquanto carrega
                error(R.drawable.ic_edit) // Mostra um ícone de erro se falhar
            }
        } else {
            // Se não houver foto, define uma imagem padrão para não mostrar a cor de fundo
            holder.imagemPonto.setImageResource(R.drawable.ic_ponto_visitado_marker)
        }

    }

    override fun getItemCount(): Int {
        return pontos.size
    }

    fun updateData(novosPontos: List<PontoVisitado>) {
        pontos.clear()
        pontos.addAll(novosPontos)
        notifyDataSetChanged()
    }
}
