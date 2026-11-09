package com.satse.jarj2me.emulator

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.util.AttributeSet
import android.view.SurfaceView
import android.view.View
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class J2MEEmulator @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : SurfaceView(context, attrs, defStyleAttr), Runnable {

    private var isRunning = false
    private val paint = Paint().apply {
        color = Color.WHITE
        textSize = 48f
        isAntiAlias = true
    }
    private val bgPaint = Paint().apply { color = Color.BLACK }
    private var gamePath: String? = null
    private var midletClass: String? = null

    init {
        setWillNotDraw(false)
        setFocusable(true)
        setFocusableInTouchMode(true)
    }

    fun loadGame(jarPath: String, mainClass: String?) {
        gamePath = jarPath
        midletClass = mainClass
        // TODO: Initialize J2ME VM (MicroEmulator/FreeJ2ME)
        // For now, just show placeholder
    }

    fun start() {
        if (isRunning) return
        isRunning = true
        CoroutineScope(Dispatchers.Default).launch { run() }
    }

    fun stop() {
        isRunning = false
    }

    override fun run() {
        while (isRunning) {
            drawFrame()
            Thread.sleep(16) // ~60 FPS
        }
    }

    private fun drawFrame() {
        val canvas = holder.lockCanvas() ?: return
        try {
            canvas.drawRect(0f, 0f, width.toFloat(), height.toFloat(), bgPaint)
            
            val msg = if (gamePath != null) {
                "Running: ${gamePath}\nClass: ${midletClass ?: "unknown"}\n\n[J2ME Emulator Placeholder]\nIntegrate MicroEmulator/FreeJ2ME here"
            } else {
                "No game loaded\nTap to select JAR/JAD file"
            }
            
            val lines = msg.split("\n")
            var y = height / 2f - (lines.size * paint.textSize) / 2f
            for (line in lines) {
                val x = (width - paint.measureText(line)) / 2f
                canvas.drawText(line, x, y, paint)
                y += paint.textSize * 1.2f
            }
        } finally {
            holder.unlockCanvasAndPost(canvas)
        }
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        // Called when not using SurfaceView thread
    }
}