package com.example.notessharingapp

import android.content.Intent
import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class ShareActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_share)

        val tvShareInstructions = findViewById<TextView>(R.id.tvShareInstructions)

        tvShareInstructions.setOnClickListener {
            val intent = Intent(Intent.ACTION_SEND)
            intent.type = "text/plain"
            intent.putExtra(Intent.EXTRA_SUBJECT, "Sharing Notes")
            intent.putExtra(Intent.EXTRA_TEXT, "Here are my notes!")
            startActivity(Intent.createChooser(intent, "Share via"))
        }
    }
}
