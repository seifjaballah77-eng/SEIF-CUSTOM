package com.seifcustom

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Movie
import android.graphics.Paint
import android.graphics.RectF
import android.graphics.ImageDecoder
import android.graphics.drawable.Animatable
import android.graphics.drawable.Drawable
import android.media.MediaPlayer
import android.net.Uri
import android.os.Build
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
        private var bitmap: Bitmap? = null
        private var imageDrawable: Drawable? = null
        private var gifStream: InputStream? = null
        private val paint = Paint(Paint.ANTI_ALIAS_FLAG or Paint.FILTER_BITMAP_FLAG)
        private val handler = Handler(Looper.getMainLooper())
        private var visibleNow = false
        private var startedAt = 0L
        private val frame = object : Runnable {
            override fun run() {
                if (!visibleNow) return
                drawImageFrame()
                if (gif != null || imageDrawable is Animatable) handler.postDelayed(this, 33L)
            }
        }
        override fun onSurfaceCreated(holder: SurfaceHolder) { super.onSurfaceCreated(holder); startSelectedMedia(holder) }
        override fun onSurfaceChanged(holder: SurfaceHolder, format: Int, width: Int, height: Int) {
            super.onSurfaceChanged(holder, format, width, height)
            if (gif != null || bitmap != null || imageDrawable != null) drawImageFrame()
        }
        override fun onVisibilityChanged(visible: Boolean) {
            visibleNow = visible
            if (visible) {
                if (gif != null || imageDrawable is Animatable) { startedAt = android.os.SystemClock.uptimeMillis(); handler.removeCallbacks(frame); handler.post(frame) }
                else if (bitmap != null || imageDrawable != null) drawImageFrame()
                else if (player != null && !player!!.isPlaying) try { player?.start() } catch (_: Exception) {}
            } else {
                handler.removeCallbacks(frame)
                try { player?.pause() } catch (_: Exception) {}
            }
        }
        private fun startSelectedMedia(holder: SurfaceHolder) {
            releaseMedia()
            val raw = getSharedPreferences("seif-custom", MODE_PRIVATE).getString("wallpaper_uri", null) ?: return
            val uri = Uri.parse(raw)
            val mime = try { contentResolver.getType(uri) ?: "" } catch (_: Exception) { "" }
            try {
                when {
                    mime.equals("image/gif", true) || uri.toString().substringBefore("?").endsWith(".gif", true) -> {
                        gifStream = contentResolver.openInputStream(uri)
                        gif = gifStream?.let { Movie.decodeStream(it) }
                        startedAt = android.os.SystemClock.uptimeMillis()
                        if (visibleNow) handler.post(frame)
                    }
                    mime.startsWith("image/") -> {
                        if (Build.VERSION.SDK_INT >= 28) {
                            imageDrawable = ImageDecoder.decodeDrawable(ImageDecoder.createSource(contentResolver, uri))
                            (imageDrawable as? Animatable)?.start()
                        } else {
                            bitmap = contentResolver.openInputStream(uri)?.use { BitmapFactory.decodeStream(it) }
                        }
                        if (imageDrawable is Animatable && visibleNow) handler.post(frame) else if (visibleNow) drawImageFrame()
                    }
                    mime.startsWith("video/") || mime.isBlank() -> {
                        player = MediaPlayer().apply {
                            setDataSource(this@VideoWallpaperService, uri)
                            setSurface(holder.surface)
                            isLooping = true
                            setVolume(0f, 0f)
                            setOnPreparedListener { if (visibleNow) it.start() }
                            setOnErrorListener { _, _, _ -> releasePlayer(); true }
                            prepareAsync()
                        }
                    }
                }
            } catch (_: Exception) {
                releaseMedia()
            }
        }
        private fun drawImageFrame() {
            var canvas: Canvas? = null
            try {
                canvas = surfaceHolder.lockCanvas()
                if (canvas != null) {
                    canvas.drawColor(Color.BLACK)
                    val opacity = getSharedPreferences("seif-custom", MODE_PRIVATE).getInt("wallpaper_opacity", 100).coerceIn(15, 100)
                    paint.alpha = (opacity * 255 / 100)
                    val movie = gif
                    if (movie != null) {
                        val duration = movie.duration().takeIf { it > 0 } ?: 1000
                        movie.setTime(((android.os.SystemClock.uptimeMillis() - startedAt) % duration).toInt())
                        val scale = minOf(canvas.width.toFloat() / movie.width(), canvas.height.toFloat() / movie.height())
                        val left = (canvas.width - movie.width() * scale) / 2f
                        val top = (canvas.height - movie.height() * scale) / 2f
                        canvas.save(); canvas.translate(left, top); canvas.scale(scale, scale); movie.draw(canvas, 0f, 0f, paint); canvas.restore()
                    } else {
                        val drawable = imageDrawable
                        if (drawable != null) {
                            val iw = drawable.intrinsicWidth.coerceAtLeast(1).toFloat()
                            val ih = drawable.intrinsicHeight.coerceAtLeast(1).toFloat()
                            val scale = minOf(canvas.width / iw, canvas.height / ih)
                            val w = (iw * scale).toInt(); val h = (ih * scale).toInt()
                            drawable.alpha = opacity * 255 / 100
                            drawable.setBounds((canvas.width-w)/2, (canvas.height-h)/2, (canvas.width+w)/2, (canvas.height+h)/2)
                            drawable.draw(canvas)
                        } else {
                            val image = bitmap
                            if (image != null) {
                                val scale = minOf(canvas.width.toFloat()/image.width, canvas.height.toFloat()/image.height)
                                val w = image.width*scale; val h = image.height*scale
                                canvas.drawBitmap(image, null, RectF((canvas.width-w)/2f,(canvas.height-h)/2f,(canvas.width+w)/2f,(canvas.height+h)/2f), paint)
                            }
                        }
                    }
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
            handler.removeCallbacks(frame); releasePlayer(); gif = null; bitmap?.recycle(); bitmap = null
            try { (imageDrawable as? Animatable)?.stop() } catch (_: Exception) {}
            imageDrawable = null
            try { gifStream?.close() } catch (_: Exception) {}
            gifStream = null
        }
        override fun onDestroy() { visibleNow = false; releaseMedia(); super.onDestroy() }
    }
}
