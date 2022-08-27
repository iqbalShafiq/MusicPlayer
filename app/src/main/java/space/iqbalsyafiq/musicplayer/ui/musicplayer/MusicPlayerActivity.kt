package space.iqbalsyafiq.musicplayer.ui.musicplayer

import android.content.res.ColorStateList
import android.media.AudioAttributes
import android.media.MediaPlayer
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.*
import org.koin.androidx.viewmodel.ext.android.viewModel
import space.iqbalsyafiq.musicplayer.R
import space.iqbalsyafiq.musicplayer.adapter.MusicAdapter
import space.iqbalsyafiq.musicplayer.databinding.ActivityMusicPlayerBinding
import space.iqbalsyafiq.musicplayer.helper.textChanges
import space.iqbalsyafiq.musicplayer.model.ApiResponse
import space.iqbalsyafiq.musicplayer.model.music.response.MusicResult
import java.io.IOException

class MusicPlayerActivity : AppCompatActivity() {

    private val viewModel: MusicPlayerViewModel by viewModel()
    private lateinit var binding: ActivityMusicPlayerBinding
    private lateinit var adapter: MusicAdapter
    private lateinit var mediaPlayer: MediaPlayer

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMusicPlayerBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // init adapter
        adapter = MusicAdapter()

        // init search event listener
        setSearchEventListener()

        // init swipe refresh event listener
        setSwipeRefreshEventListener()
    }

    /**
     * Set the listener of search edit text so it will
     * call the Api when there's an update in the edit text
     * and have 1500ms debounce.
     * Also stop the current playing music when searching.
     */
    @OptIn(ExperimentalCoroutinesApi::class, FlowPreview::class)
    private fun setSearchEventListener() {
        binding.etSearch.textChanges()
            .filterNot {
                it.isNullOrBlank()
            }
            .debounce(1500)
            .distinctUntilChanged { old, new ->
                old != new
            }
            .onEach {
                // set adapter selected position into -1
                adapter.selectedItemPosition = MusicAdapter.NOT_SET_YET

                stopCurrentPlayingMusic()
                getMusicListByArtist(it.toString())
            }
            .launchIn(lifecycleScope)
    }

    /**
     * Call getMusicListByArtist when swiping the layout
     */
    private fun setSwipeRefreshEventListener() {
        with(binding) {
            swipeProgress.setOnRefreshListener {
                stopCurrentPlayingMusic()
                getMusicListByArtist(etSearch.text.toString())
            }
        }
    }

    /**
     * Observe live data of music response that returned from view model
     */
    private fun getMusicListByArtist(artist: String) {
        if (checkSearchValue(artist)) {
            viewModel.getMusicListByArtist(artist).observe(this) {
                it?.let { response ->
                    when (response) {
                        is ApiResponse.Loading -> {
                            setLoadingStateCondition(true)
                        }

                        is ApiResponse.Success -> {
                            response.data.results?.let { data ->
                                setSuccessStateCondition(data)
                            }
                        }

                        is ApiResponse.Error -> {
                            setErrorStateCondition()
                        }

                        ApiResponse.Empty -> {
                            setEmptyStateCondition()
                        }
                    }
                }
            }
        }
    }

    /**
     * Check the state of search value.
     * Will call empty state condition and return false when the value is empty
     */
    private fun checkSearchValue(value: String): Boolean {
        if (value.isEmpty()) {
            setEmptyStateCondition()
            return false
        }
        return true
    }

    /**
     * Show swipe refresh loading state based on isLoading parameter
     */
    private fun setLoadingStateCondition(isLoading: Boolean) {
        with(binding) {
            swipeProgress.isRefreshing = isLoading

            if (isLoading) {
                tvError.visibility = View.GONE
                rvMusicList.visibility = View.GONE
            }
        }
    }

    /**
     * Set adapter list data and assign it into the recyclerview
     * then show to the layout.
     */
    private fun setSuccessStateCondition(data: List<MusicResult>) {
        setLoadingStateCondition(false)

        with(binding) {
            adapter.setData(data)
            adapter.onItemClick = { music -> onAdapterItemClickedCallback(music) }
            rvMusicList.adapter = adapter

            // set the visibility
            tvError.visibility = View.GONE
            rvMusicList.visibility = View.VISIBLE
        }
    }

    /**
     * Hiding the recyclerview and show the text empty info
     */
    private fun setEmptyStateCondition() {
        setLoadingStateCondition(false)

        with(binding) {
            tvError.visibility = View.VISIBLE
            tvError.text = getString(R.string.warning_no_data)
            rvMusicList.visibility = View.GONE
        }
    }

    /**
     * Hiding the recyclerview and show the text error info
     */
    private fun setErrorStateCondition() {
        setLoadingStateCondition(false)

        with(binding) {
            tvError.visibility = View.VISIBLE
            tvError.text = getString(R.string.error_fetching_api)
            rvMusicList.visibility = View.GONE
        }
    }

    /**
     * Give listener when adapter's item clicked
     */
    private fun onAdapterItemClickedCallback(music: MusicResult) {
        stopCurrentPlayingMusic()
        playSelectedMusic(music)
    }

    /**
     * If true it will give loading state when music is waiting to play.
     */
    private fun setLoadingMusicState(isLoading: Boolean) {
        with(binding) {
            swipeProgress.isRefreshing = isLoading
            etSearch.isEnabled = !isLoading
            rvMusicList.isEnabled = !isLoading
            ivControl.isEnabled = !isLoading
            ivNext.isEnabled = !isLoading
            ivPrev.isEnabled = !isLoading
        }
    }

    /**
     * Playing selected music
     */
    private fun playSelectedMusic(music: MusicResult) {

        // init music player
        mediaPlayer = MediaPlayer()

        mediaPlayer.setAudioAttributes(
            AudioAttributes
                .Builder()
                .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                .build()
        )

        try {
            // get music from preview url
            mediaPlayer.setDataSource(music.previewUrl)

            // play music
            mediaPlayer.prepareAsync()

            // set loading
            setLoadingMusicState(true)

            // set on media player preparing is done
            mediaPlayer.setOnPreparedListener {
                mediaPlayer.start()

                // stop the loading
                setLoadingMusicState(false)

                // add sign to the selected item
                setPlayingMusicItemState(music)

                // set media control state
                setMediaControlState(music)
            }
        } catch (e: IOException) {
            setLoadingMusicState(false)
            e.printStackTrace()
        }

    }

    /**
     * Pause current playing music by checking
     * the initialization and current state of mediaPlayer.
     */
    private fun pauseCurrentPlayingMusic() {
        if (this::mediaPlayer.isInitialized) {
            if (mediaPlayer.isPlaying) {
                mediaPlayer.pause()
                setMediaControlState()
            }
        }
    }

    /**
     * Stop current playing music by checking
     * the initialization and current state of mediaPlayer.
     */
    private fun stopCurrentPlayingMusic() {
        if (this::mediaPlayer.isInitialized) {
            try {
                mediaPlayer.stop()
                mediaPlayer.reset()
                mediaPlayer.release()

                // set media control visibility
                binding.llMediaControl.visibility = View.GONE
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    /**
     * Set state of media control
     */
    private fun setMediaControlState(music: MusicResult? = null) {
        with(binding) {
            if (this@MusicPlayerActivity::mediaPlayer.isInitialized) {
                if (mediaPlayer.isPlaying) {
                    llMediaControl.visibility = View.VISIBLE
                    ivControl.setImageResource(R.drawable.ic_pause)
                    ivControl.setOnClickListener {
                        pauseCurrentPlayingMusic()
                    }
                } else {
                    ivControl.setImageResource(R.drawable.ic_play)
                    ivControl.setOnClickListener {
                        mediaPlayer.start()
                        setMediaControlState(music)
                    }
                }

                // set navigation media control
                music?.let {
                    setNavigationMediaControl(music)
                }
            }
        }
    }

    /**
     * Add sign to the current playing music
     */
    private fun setPlayingMusicItemState(music: MusicResult) {
        val currentPosition = adapter.listData.indexOf(music)
        val previousPosition = adapter.selectedItemPosition
        adapter.selectedItemPosition = currentPosition

        // update state of item in previous and current position
        adapter.notifyItemChanged(previousPosition)
        adapter.notifyItemChanged(currentPosition)
    }

    /**
     * Check the position of current music in the list.
     * Then add listener when prev and next icon clicked.
     */
    private fun setNavigationMediaControl(music: MusicResult) {
        val currentPosition = adapter.listData.indexOf(music)

        with(binding) {
            // check position of the current selected item
            when (currentPosition) {
                0 -> {
                    ivPrev.isEnabled = false
                    ivPrev.imageTintList = ColorStateList.valueOf(
                        ContextCompat.getColor(
                            this@MusicPlayerActivity, R.color.light_disabled
                        )
                    )
                    ivNext.isEnabled = true
                    ivNext.imageTintList = ColorStateList.valueOf(
                        ContextCompat.getColor(
                            this@MusicPlayerActivity, R.color.dark_accent
                        )
                    )
                    ivNext.setOnClickListener { playOtherMusic(currentPosition + 1) }
                }
                adapter.listData.size - 1 -> {
                    ivPrev.isEnabled = true
                    ivPrev.imageTintList = ColorStateList.valueOf(
                        ContextCompat.getColor(
                            this@MusicPlayerActivity, R.color.dark_accent
                        )
                    )
                    ivNext.isEnabled = false
                    ivNext.imageTintList = ColorStateList.valueOf(
                        ContextCompat.getColor(
                            this@MusicPlayerActivity, R.color.light_disabled
                        )
                    )
                    ivPrev.setOnClickListener { playOtherMusic(currentPosition - 1) }
                }
                else -> {
                    ivPrev.isEnabled = true
                    ivPrev.imageTintList = ColorStateList.valueOf(
                        ContextCompat.getColor(
                            this@MusicPlayerActivity, R.color.dark_accent
                        )
                    )
                    ivNext.isEnabled = true
                    ivNext.imageTintList = ColorStateList.valueOf(
                        ContextCompat.getColor(
                            this@MusicPlayerActivity, R.color.dark_accent
                        )
                    )

                    ivPrev.setOnClickListener { playOtherMusic(currentPosition - 1) }
                    ivNext.setOnClickListener { playOtherMusic(currentPosition + 1) }
                }
            }
        }
    }

    /**
     * Play previous selected music
     */
    private fun playOtherMusic(position: Int) {
        val music = adapter.listData[position]
        onAdapterItemClickedCallback(music)
    }
}