
package com.screenmask

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.PixelFormat
import android.os.Bundle
import android.view.MotionEvent
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class SelectAreaActivity : AppCompatActivity() {

    private lateinit var selectionView: SelectionView
    private val ruleManager by lazy { RuleManager(this) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        window.setFormat(PixelFormat.TRANSLUCENT)

        selectionView = SelectionView(this) { left, top, right, bottom ->
            val color = captureColor(left, top, right, bottom)
            saveRule(left, top, right, bottom, color)
        }

        setContentView(selectionView)
    }

    private fun captureColor(left: Int, top: Int, right: Int, bottom: Int): Int {
        try {
            val rootView = window.decorView.rootView
            rootView.isDrawingCacheEnabled = true
            val bitmap = Bitmap.createBitmap(rootView.width, rootView.height, Bitmap.Config.ARGB_8888)
            val canvas = Canvas(bitmap)
            rootView.draw(canvas)

            val centerX = (left + right) / 2
            val centerY = (top + bottom) / 2

            if (centerX >= 0 && centerX < bitmap.width && centerY >= 0 && centerY < bitmap.height) {
                val pixel = bitmap.getPixel(centerX, centerY)
                bitmap.recycle()
                return pixel
            }

            bitmap.recycle()
        } catch (e: Exception) {
            e.printStackTrace()
        }

        return Color.parseColor("#80000000")
    }

    private fun saveRule(left: Int, top: Int, right: Int, bottom: Int, color: Int) {
        val rule = Rule(
            id = System.currentTimeMillis(),
            left = left,
            top = top,
            right = right,
            bottom = bottom,
            color = color,
            enabled = true
        )

        ruleManager.addRule(rule)

        runOnUiThread {
            Toast.makeText(this, "已保存遮挡规则", Toast.LENGTH_SHORT).show()
            finish()
        }
    }
}

class SelectionView(
    context: android.content.Context,
    private val onAreaSelected: (Int, Int, Int, Int) -> Unit
) : View(context) {

    private val paint = Paint().apply {
        color = Color.parseColor("#4000FF00")
        style = Paint.Style.FILL
    }

    private val strokePaint = Paint().apply {
        color = Color.GREEN
        style = Paint.Style.STROKE
        strokeWidth = 4f
    }

    private var startX = 0f
    private var startY = 0f
    private var endX = 0f
    private var endY = 0f
    private var isSelecting = false

    override fun onTouchEvent(event: MotionEvent): Boolean {
        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                startX = event.x
                startY = event.y
                endX = startX
                endY = startY
                isSelecting = true
                invalidate()
                return true
            }
            MotionEvent.ACTION_MOVE -> {
                if (isSelecting) {
                    endX = event.x
                    endY = event.y
                    invalidate()
                }
                return true
            }
            MotionEvent.ACTION_UP -> {
                if (isSelecting) {
                    endX = event.x
                    endY = event.y
                    isSelecting = false

                    val left = minOf(startX, endX).toInt()
                    val top = minOf(startY, endY).toInt()
                    val right = maxOf(startX, endX).toInt()
                    val bottom = maxOf(startY, endY).toInt()

                    if (right - left > 10 && bottom - top > 10) {
                        onAreaSelected(left, top, right, bottom)
                    }
                }
                return true
            }
        }
        return super.onTouchEvent(event)
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        if (isSelecting || (endX != startX && endY != startY)) {
            val left = minOf(startX, endX)
            val top = minOf(startY, endY)
            val right = maxOf(startX, endX)
            val bottom = maxOf(startY, endY)

            canvas.drawRect(left, top, right, bottom, paint)
            canvas.drawRect(left, top, right, bottom, strokePaint)
        }
    }
}