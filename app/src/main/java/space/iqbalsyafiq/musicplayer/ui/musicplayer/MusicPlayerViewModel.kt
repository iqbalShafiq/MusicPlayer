package space.iqbalsyafiq.musicplayer.ui.musicplayer

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import space.iqbalsyafiq.musicplayer.model.ApiResponse
import space.iqbalsyafiq.musicplayer.model.music.MusicRepository
import space.iqbalsyafiq.musicplayer.model.music.response.MusicResponse

class MusicPlayerViewModel(private val repository: MusicRepository) : ViewModel() {

    /**
     * Get data of music list by artist from repository.
     * Then return live data of music response.
     */
    fun getMusicListByArtist(artist: String): LiveData<ApiResponse<MusicResponse>> =
        repository.getMusicListByArtist(artist).asLiveData()
}