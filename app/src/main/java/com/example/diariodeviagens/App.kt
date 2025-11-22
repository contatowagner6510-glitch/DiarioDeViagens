package com.example.diariodeviagens


import android.app.Application
import com.cloudinary.android.MediaManager // Import do Cloudinary
import org.osmdroid.config.Configuration

class App : Application() {
    override fun onCreate() {
        super.onCreate()

        // --- CONFIGURAÇÃO DO CLOUDINARY ---
        val config = HashMap<String, String>()
        config["cloud_name"] = "dibj6xxzm"
        config["api_key"] = "121658757481584"
        config["api_secret"] = "bcpLQAfm53nVJIrdSjQ4zfZ77-I"
        // Para segurança adicional, você pode adicionar:
        // config["secure"] = "true"
        MediaManager.init(this, config)
        // ------------------------------------

        // Configuração do OSMDroid (já tínhamos)
        Configuration.getInstance().load(
            this,
            getSharedPreferences("osmdroid", MODE_PRIVATE)
        )
    }
}
