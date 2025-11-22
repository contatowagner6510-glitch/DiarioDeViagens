package com.example.diariodeviagens


import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class PontoVisitado(
    val id: String = "",
    val nomeLocal: String = "",
    val notas: String = "",
    val urlFoto: String = "",
    val latitude: Double = 0.0,
    val longitude: Double = 0.0
) : Parcelable
