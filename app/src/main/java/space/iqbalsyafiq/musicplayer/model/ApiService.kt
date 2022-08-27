package space.iqbalsyafiq.musicplayer.model

import retrofit2.http.GET
import retrofit2.http.Query
import space.iqbalsyafiq.musicplayer.model.music.response.MusicResponse

interface ApiService {
    @GET("search")
    suspend fun getListMusicByArtist(
        @Query("term") artist: String
    ): MusicResponse
}