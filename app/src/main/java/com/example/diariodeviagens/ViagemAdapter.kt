package com.example.diariodeviagens


import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class ViagemAdapter(
    private var viagens: MutableList<Viagem>,
    private val onItemClick: (Viagem) -> Unit,
    private val onItemLongClick: (Viagem) -> Unit
) : RecyclerView.Adapter<ViagemAdapter.ViagemViewHolder>() {

    /**
     * ViewHolder: Representa a view de um único item na lista.
     */
    class ViagemViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        // Referências para os componentes visuais do item_viagem.xml
        private val nomeViagem: TextView = itemView.findViewById(R.id.textViewNomeViagem)
        private val datasViagem: TextView = itemView.findViewById(R.id.textViewDatasViagem)

        /**
         * 'bind' é a função que conecta os dados de uma Viagem específica
         * com as views deste ViewHolder e configura os cliques.
         */
        fun bind(viagem: Viagem, onItemClick: (Viagem) -> Unit, onItemLongClick: (Viagem) -> Unit) {
            // Preenche os dados nas views
            nomeViagem.text = viagem.nome
            val datasFormatadas = if (viagem.dataInicio.isNotEmpty() && viagem.dataFim.isNotEmpty()) {
                "${viagem.dataInicio} - ${viagem.dataFim}"
            } else {
                "Datas não definidas"
            }
            datasViagem.text = datasFormatadas

            // Configura os listeners de clique para o item inteiro
            itemView.setOnClickListener {
                onItemClick(viagem)
            }
            itemView.setOnLongClickListener {
                onItemLongClick(viagem)
                true // Retornar true indica que o evento foi consumido
            }
        }
    }

    /**
     * Chamado pelo RecyclerView quando ele precisa criar um novo ViewHolder.
     */
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViagemViewHolder {
        // Cria (infla) a view do item a partir do nosso XML
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_viagem, parent, false)
        return ViagemViewHolder(view)
    }

    /**
     * Chamado pelo RecyclerView para exibir os dados em uma posição específica.
     */
    override fun onBindViewHolder(holder: ViagemViewHolder, position: Int) {
        // Pega a viagem na posição atual
        val viagem = viagens[position]
        // Chama a função 'bind' do ViewHolder para fazer o trabalho
        holder.bind(viagem, onItemClick, onItemLongClick)
    }

    /**
     * Retorna o número total de itens na lista.
     */
    override fun getItemCount(): Int {
        return viagens.size
    }

    /**
     * Função pública para a MainActivity atualizar a lista de viagens no adapter.
     */
    fun updateData(novaListaDeViagens: List<Viagem>) {
        viagens.clear()
        viagens.addAll(novaListaDeViagens)
        // Notifica o RecyclerView que o conjunto de dados mudou completamente.
        // Isso força a lista a ser redesenhada.
        notifyDataSetChanged()
    }
}
