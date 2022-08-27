package space.iqbalsyafiq.musicplayer.model.music.response

data class MusicResponse(
    val resultCount: Int?,
    val results: List<MusicResult>?
)