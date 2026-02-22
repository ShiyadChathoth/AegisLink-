package com.aegislink.ui.auth

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.aegislink.AegisApplication
import com.aegislink.databinding.ActivityLoginBinding

class LoginActivity : AppCompatActivity() {
    private lateinit var binding: ActivityLoginBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val container = (application as AegisApplication).container
        val repo = container.securePrefsRepository
        binding.etManualApiKey.setText(repo.getVirusTotalApiKey().orEmpty())

        binding.btnOpenVirusTotal.setOnClickListener {
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(VIRUS_TOTAL_API_KEY_URL))
            startActivity(intent)
        }

        binding.btnUseManualApiKey.setOnClickListener {
            val manualKey = binding.etManualApiKey.text.toString().trim()
            if (manualKey.isBlank()) {
                Toast.makeText(this, "Paste a valid API key", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            repo.setVirusTotalApiKey(manualKey)
            Toast.makeText(this, "Manual VirusTotal API key saved", Toast.LENGTH_SHORT).show()
            finish()
        }
    }

    companion object {
        private const val VIRUS_TOTAL_API_KEY_URL = "https://www.virustotal.com/gui/my-apikey"
    }
}
