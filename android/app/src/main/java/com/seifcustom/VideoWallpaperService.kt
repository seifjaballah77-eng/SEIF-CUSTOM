package com.seifcustom

import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Movie
import android.graphics.Paint
import android.media.MediaPlayer
import android.net.Uri
import android.os.Handler
import android.os.Looper
import android.service.wallpaper.WallpaperService
import android.view.SurfaceHolder
import java.io.InputStream

class VideoWallpaperService : WallpaperService() {
    override fun onCreateEngine(): Engine = MediaEngine()
    inner class MediaEngine : Engine() {
        private var player: MediaPlayer? = null
        private var gif: Movie? = null
        private var gifStream: InputStream? = null
        private val paint = Paint(Paint.ANTI_ALIAS_FLAG or Paint.FILTER_BITMAP_FLAG)
        private val handler = Handler(Looper.getMainLooper())
        private var visibleNow = false
        private var startedAt = 0L
        private val frame = object : Runnable {
            override fun run() {
                if (!visibleNow) return
                drawGifFrame()
                handler.postDelayed(this, 33L)
            }
        }
        override fun onSurfaceCreated(holder: SurfaceHolder) { super.onSurfaceCreated(holder); startSelectedMedia(holder) }
        override fun onSurfaceChanged(holder: SurfaceHolder, format: Int, width: Int, height: Int) { super.onSurfaceChanged(holder, format, width, height); if (gif != null) drawGifFrame() }
        override fun onVisibilityChanged(visible: Boolean) {
            visibleNow = visible
            if (visible) {
                if (gif != null) { startedAt = android.os.SystemClock.uptimeMillis(); handler.removeCallbacks(frame); handler.post(frame) }
                else if (player != null && !player!!.isPlaying) try { player?.start() } catch (_: Exception) {}
            } else { handler.removeCallbacks(frame); try { player?.pause() } catch (_: Exception) {} }
        }
        private fun startSelectedMedia(holder: SurfaceHolder) {
            releaseMedia()
            val raw = getSharedPreferences("seif-custom", MODE_PRIVATE).getString("wallpaper_uri", null) ?: return
            val uri = Uri.parse(raw)
            val mime = try { contentResolver.getType(uri) ?: "" } catch (_: Exception) { "" }
            if (mime.equals("image/gif", true) || uri.toString().substringBefore("?").endsWith(".gif", true)) {
                try {
                    gifStream = contentResolver.openInputStream(uri)
                    gif = gifStream?.let { Movie.decodeStream(it) }
                    startedAt = android.os.SystemClock.uptimeMillis()
                    if (visibleNow) handler.post(frame)
                } catch (_: Exception) { gif = null }
            } else if (mime.startsWith("video/") || mime.isBlank()) {
                try {
                    player = MediaPlayer().apply {
                        setDataSource(this@VideoWallpaperService, uri)
                        setSurface(holder.surface)
                        isLooping = true
                        setVolume(0f, 0f)
                        setOnPreparedListener { if (visibleNow) it.start() }
                        setOnErrorListener { _, _, _ -> releasePlayer(); true }
                        prepareAsync()
                    }
                } catch (_: Exception) { releasePlayer() }
            }
        }
        private fun drawGifFrame() {
            val movie = gif ?: return
            var canvas: Canvas? = null
            try {
                canvas = surfaceHolder.lockCanvas()
                if (canvas != null) {
                    canvas.drawColor(Color.BLACK)
                    val duration = movie.duration().takeIf { it > 0 } ?: 1000
                    movie.setTime(((android.os.SystemClock.uptimeMillis() - startedAt) % duration).toInt())
                    val scale = minOf(canvas.width.toFloat() / movie.width(), canvas.height.toFloat() / movie.height())
                    val left = (canvas.width - movie.width() * scale) / 2f
                    val top = (canvas.height - movie.height() * scale) / 2f
                    val opacity = getSharedPreferences("seif-custom", MODE_PRIVATE).getInt("wallpaper_opacity", 85).coerceIn(15, 85)
                    paint.alpha = (opacity / 85f * 255).toInt()
                    canvas.save(); canvas.translate(left, top); canvas.scale(scale, scale); movie.draw(canvas, 0f, 0f, paint); canvas.restore()
                }
            } catch (_: Exception) {
            } finally { if (canvas != null) surfaceHolder.unlockCanvasAndPost(canvas) }
        }
        private fun releasePlayer() {
            try { player?.setOnPreparedListener(null); player?.stop() } catch (_: Exception) {}
            try { player?.release() } catch (_: Exception) {}
            player = null
        }
        private fun releaseMedia() {
            handler.removeCallbacks(frame); releasePlayer(); gif = null
            try { gifStream?.close() } catch (_: Exception) {}
            gifStream = null
        }
        override fun onDestroy() { visibleNow = false; releaseMedia(); super.onDestroy() }
    }
}
