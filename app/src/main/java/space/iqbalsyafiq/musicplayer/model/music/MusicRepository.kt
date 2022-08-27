package space.iqbalsyafiq.musicplayer.model.music

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import space.iqbalsyafiq.musicplayer.model.ApiResponse
import space.iqbalsyafiq.musicplayer.model.ApiService
import space.iqbalsyafiq.musicplayer.model.music.response.MusicResponse

class MusicRepository(private val apiService: ApiService) {

    /**
     * Get the music list from Api by filtering the artist
     */
    fun getMusicListByArtist(artist: String): Flow<ApiResponse<MusicResponse>> {
        return flow {
            try {
                emit(ApiResponse.Loading())

                val response = apiService.getListMusicByArtist(artist)

                when (response.resultCount) {
                    EMPTY_LIST -> {
                        emit(ApiResponse.Empty)
                    }

                    else -> {
                        emit(ApiResponse.Success(response))
                    }
                }
            } catch (e: Exception) {
                e.message?.let { message ->
                    emit(ApiResponse.Error(message))
                }
            }
        }
    }

    companion object {
        private const val EMPTY_LIST = 0
    }
}