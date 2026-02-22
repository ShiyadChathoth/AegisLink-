package com.aegislink.ui.blocked

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.aegislink.R
import com.aegislink.databinding.ActivityBlockedUrlBinding

class BlockedUrlActivity : AppCompatActivity() {
    private lateinit var binding: ActivityBlockedUrlBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityBlockedUrlBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.tvBlockedUrl.text = getString(R.string.label_blocked_url, intent.getStringExtra("blocked_url").orEmpty())
        binding.tvReason.text = getString(R.string.label_block_reason, intent.getStringExtra("reason").orEmpty())
        binding.btnClose.setOnClickListener { finish() }
    }
}
