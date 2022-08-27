package space.iqbalsyafiq.musicplayer.ui

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import space.iqbalsyafiq.musicplayer.databinding.ActivityMainBinding
import space.iqbalsyafiq.musicplayer.ui.musicplayer.MusicPlayerActivity

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // delay splash screen
        runBlocking {
            lifecycleScope.launch {
                // set delay time
                delay(2000)

                // intent to music player
                Intent(this@MainActivity, MusicPlayerActivity::class.java).apply {
                    startActivity(this)
                    finish()
                }
            }
        }
    }
}