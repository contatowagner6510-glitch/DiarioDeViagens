package com.example.diariodeviagens


import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize // <-- ADICIONE ISSO
data class Viagem(
    val id: String = "",
    val nome: String = "",
    val dataInicio: String = "",
    val dataFim: String = "",
    val latitude: Double = 0.0,
    val longitude: Double =0.0

) : Parcelable // <-- E ISSO
