package com.nexus.tune.core.extractor

import com.nexus.tune.core.common.ExtractorError
import com.nexus.tune.core.common.MusicResult
import com.nexus.tune.core.model.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import org.schabi.newpipe.extractor.NewPipe
import org.schabi.newpipe.extractor.ServiceList
import org.schabi.newpipe.extractor.localization.Localization
import org.schabi.newpipe.extractor.services.youtube.extractors.YoutubeSuggestionExtractor
import org.schabi.newpipe.extractor.stream.AudioStream as ExtractorAudioStream
import org.schabi.newpipe.extractor.stream.StreamInfo
import org.schabi.newpipe.extractor.stream.StreamInfoItem
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.TimeUnit

class NewPipeMusicSource(
    private val okHttpClient: OkHttpClient = OkHttpClient.Builder()
        .connectTimeout(15, TimeUnit.SECONDS)
        .readTimeout(15, TimeUnit.SECONDS)
        .build()
) : MusicSource {

    private val streamCache = ConcurrentHashMap<String, AudioStream>()

    init {
        val downloader = OkHttpDownloader(okHttpClient)
        NewPipe.init(downloader, Localization("en", "US"))
    }

    override suspend fun search(query: String, filter: String?): MusicResult<SearchResult> =
        withContext(Dispatchers.IO) {
            try {
                val service = ServiceList.YouTube
                val searchExtractor = service.getSearchExtractor(query)
                searchExtractor.fetchPage()

                val tracks = mutableListOf<Track>()
                searchExtractor.initialPage.items.forEach { item ->
                    val url = item.url
                    val id = url.substringAfter("v=").substringBefore("&")
                    val uploader = (item as? StreamInfoItem)?.uploaderName ?: "Unknown Artist"
                    val thumb = item.thumbnails.firstOrNull()?.url
                    tracks.add(
                        Track(
                            id = id,
                            title = item.name,
                            artist = uploader,
                            artworkUrl = thumb
                        )
                    )
                }
                MusicResult.Success(SearchResult(tracks = tracks))
            } catch (e: Exception) {
                MusicResult.Failure(mapException(e))
            }
        }

    override suspend fun getSearchSuggestions(query: String): List<String> =
        withContext(Dispatchers.IO) {
            try {
                val extractor = YoutubeSuggestionExtractor(ServiceList.YouTube)
                extractor.suggestionList(query)
            } catch (e: Exception) {
                emptyList()
            }
        }

    override suspend fun getHomeFeed(): MusicResult<List<Playlist>> =
        withContext(Dispatchers.IO) {
            try {
                val service = ServiceList.YouTube
                val kiosk = service.kioskList.defaultKioskExtractor
                kiosk.fetchPage()

                val trendingTracks = kiosk.initialPage.items.mapNotNull { item ->
                    val url = item.url
                    val id = url.substringAfter("v=").substringBefore("&")
                    if (id.isNotBlank()) {
                        val uploader = (item as? StreamInfoItem)?.uploaderName ?: "Trending"
                        val thumb = item.thumbnails.firstOrNull()?.url
                        Track(
                            id = id,
                            title = item.name,
                            artist = uploader,
                            artworkUrl = thumb
                        )
                    } else null
                }

                val playlist = Playlist(
                    id = "trending_charts",
                    title = "Trending Charts",
                    author = "YouTube",
                    artworkUrl = trendingTracks.firstOrNull()?.artworkUrl,
                    trackCount = trendingTracks.size,
                    tracks = trendingTracks
                )
                MusicResult.Success(listOf(playlist))
            } catch (e: Exception) {
                MusicResult.Failure(mapException(e))
            }
        }

    override suspend fun getTrackDetails(trackId: String): MusicResult<Track> =
        withContext(Dispatchers.IO) {
            try {
                val service = ServiceList.YouTube
                val streamInfo = StreamInfo.getInfo(service, "https://www.youtube.com/watch?v=$trackId")
                val track = Track(
                    id = trackId,
                    title = streamInfo.name,
                    artist = streamInfo.uploaderName ?: "Unknown Artist",
                    durationSeconds = streamInfo.duration,
                    artworkUrl = streamInfo.thumbnails.firstOrNull()?.url
                )
                MusicResult.Success(track)
            } catch (e: Exception) {
                MusicResult.Failure(mapException(e))
            }
        }

    override suspend fun resolveStreamUrl(trackId: String): MusicResult<AudioStream> =
        withContext(Dispatchers.IO) {
            val cached = streamCache[trackId]
            val now = System.currentTimeMillis()
            if (cached != null && cached.expiresAtTimestampMs > now + 60_000) {
                return@withContext MusicResult.Success(cached)
            }

            try {
                val service = ServiceList.YouTube
                val streamInfo = StreamInfo.getInfo(service, "https://www.youtube.com/watch?v=$trackId")
                val audioStreams: List<ExtractorAudioStream> = streamInfo.audioStreams

                val selectedStream = audioStreams
                    .sortedByDescending { it.averageBitrate }
                    .firstOrNull()
                    ?: return@withContext MusicResult.Failure(
                        ExtractorError.UnknownError("No audio streams available for $trackId")
                    )

                val audioStream = AudioStream(
                    url = selectedStream.content,
                    format = selectedStream.format?.name ?: "Opus",
                    bitrateKbps = selectedStream.averageBitrate / 1000,
                    expiresAtTimestampMs = now + 10_800_000 // 3 hours expiry
                )

                streamCache[trackId] = audioStream
                MusicResult.Success(audioStream)
            } catch (e: Exception) {
                streamCache.remove(trackId)
                MusicResult.Failure(mapException(e))
            }
        }

    override suspend fun getLyrics(
        trackId: String,
        title: String,
        artist: String,
        durationSeconds: Long
    ): MusicResult<TrackLyrics> = withContext(Dispatchers.IO) {
        try {
            val url = "https://lrclib.net/api/get?track_name=${title}&artist_name=${artist}&duration=${durationSeconds}"
            val request = okhttp3.Request.Builder().url(url).build()
            val response = okHttpClient.newCall(request).execute()
            if (response.isSuccessful) {
                val body = response.body?.string() ?: ""
                val lines = parseLrc(body)
                MusicResult.Success(TrackLyrics(trackId, lines, lines.isNotEmpty()))
            } else {
                MusicResult.Success(TrackLyrics(trackId, emptyList(), false))
            }
        } catch (e: Exception) {
            MusicResult.Success(TrackLyrics(trackId, emptyList(), false))
        }
    }

    private fun parseLrc(json: String): List<LyricsLine> {
        if (!json.contains("syncedLyrics")) return emptyList()
        val syncedText = json.substringAfter("\"syncedLyrics\":\"").substringBefore("\"")
            .replace("\\n", "\n")
        val lines = mutableListOf<LyricsLine>()
        syncedText.lines().forEach { line ->
            if (line.startsWith("[") && line.contains("]")) {
                val timeStr = line.substringAfter("[").substringBefore("]")
                val text = line.substringAfter("]").trim()
                val parts = timeStr.split(":")
                if (parts.size == 2) {
                    val min = parts[0].toLongOrNull() ?: 0L
                    val secParts = parts[1].split(".")
                    val sec = secParts[0].toLongOrNull() ?: 0L
                    val ms = if (secParts.size > 1) (secParts[1].toLongOrNull() ?: 0L) * 10 else 0L
                    val totalMs = (min * 60 + sec) * 1000 + ms
                    lines.add(LyricsLine(totalMs, text))
                }
            }
        }
        return lines
    }

    private fun mapException(e: Exception): ExtractorError {
        val msg = e.message?.lowercase() ?: ""
        return when {
            msg.contains("timeout") || msg.contains("connect") || msg.contains("host") ->
                ExtractorError.NetworkError(e)
            msg.contains("429") || msg.contains("rate limit") ->
                ExtractorError.RateLimitedError()
            msg.contains("geo") || msg.contains("country") ->
                ExtractorError.GeoBlockedError()
            msg.contains("age") || msg.contains("sign in") ->
                ExtractorError.AgeRestrictedError("")
            msg.contains("deleted") || msg.contains("private") ->
                ExtractorError.TrackRemovedError("")
            else -> ExtractorError.ExtractorBrokenError(e)
        }
    }
}
