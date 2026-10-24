package com.thiya.player.demo

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.os.nativeVideoHardware.ThiyaPlayer
import com.os.nativeVideoHardware.ThiyaPlayerConstants

class MainActivity : AppCompatActivity() {

    private val selectVideoLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK && result.data != null) {
            val uri: Uri? = result.data?.data ?: result.data?.clipData?.getItemAt(0)?.uri
            if (uri != null) {
                playVideo(uri)
            } else {
                Toast.makeText(this, "No video selected", Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val button = Button(this).apply {
            text = "Pick Video to Play with Vulkan"
            setOnClickListener {
                val intent = Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
                    addCategory(Intent.CATEGORY_OPENABLE)
                    type = "video/*"
                }
                selectVideoLauncher.launch(intent)
            }
        }

        setContentView(button)
    }

    private fun playVideo(uri: Uri) {
        try {
            val pfd = contentResolver.openFileDescriptor(uri, "r")
            if (pfd == null) {
                Log.e("ThiyaDemo", "Failed to open ParcelFileDescriptor")
                Toast.makeText(this, "Failed to open video file", Toast.LENGTH_SHORT).show()
                return
            }

            // Detach the file descriptor so the native Vulkan engine can take ownership
            val fd = pfd.detachFd()

            val intent = Intent(this, ThiyaPlayer::class.java).apply {
                putExtra(ThiyaPlayerConstants.fdString, fd)
            }
            startActivity(intent)
        } catch (e: Exception) {
            Log.e("ThiyaDemo", "Error launching ThiyaPlayer: ${e.message}", e)
            Toast.makeText(this, "Error: ${e.message}", Toast.LENGTH_LONG).show()
        }
    }
}
