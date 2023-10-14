package com.example.test

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Rect
import android.graphics.drawable.Drawable
import android.os.Build
import android.util.AttributeSet
import android.util.Log
import android.view.MotionEvent
import android.view.View
import android.widget.TextView
import androidx.annotation.RequiresApi
import com.google.mlkit.vision.text.Text

@RequiresApi(Build.VERSION_CODES.Q)
class TextOverlayViewGroupV2(context: Context?, attrs: AttributeSet?) : View(context, attrs) {
    private class SelectionModel(var blockIndex: Int, var lineIndex: Int, var elementIndex: Int) {
        fun setNewSelections(blockIndex1: Int, lineIndex1: Int, elementIndex1: Int) {
            this.blockIndex = blockIndex1
            this.lineIndex = lineIndex1
            this.elementIndex = elementIndex1
        }
    }

    private var textModels: MutableList<TextModel>? = null
    private var selectedTextModels: MutableList<TextModel>? = null
    private var currentSelection: TextModel? = null
    private lateinit var textPaint: Paint
    private lateinit var selectedTextPaint: Paint
    private var customWidth = 0
    private var customHeight = 0
    private var selectionPos: SelectionModel = SelectionModel(-1, -1, -1)
    private var endPos: SelectionModel = SelectionModel(-1, -1, -1)
    private var text = TextView(context)
    private var drawableLeft: Drawable? = text.textSelectHandleLeft
    private var drawableRight: Drawable? = text.textSelectHandleRight
    private var startingTextModel: TextModel? = null
    private var endingTextModel: TextModel? = null
    private var tracing: Boolean = false
    private var canvas: Canvas? = null
    init {
        init()
        setDrawables(text.textSelectHandleLeft!!, text.textSelectHandleRight!!)
    }


    fun setCustomWidth(width: Int) {
        customWidth = width
        requestLayout()
    }

    fun setCustomHeight(height: Int) {
        customHeight = height
        requestLayout()
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        if (customHeight != 0 && customWidth != 0) {
            setMeasuredDimension(customWidth, customHeight)
        } else {
            setMeasuredDimension(widthMeasureSpec, heightMeasureSpec)
        }
        invalidate()
    }

    private fun init() {
        textModels = ArrayList()
        selectedTextModels = ArrayList()
        textPaint = Paint()
        textPaint.textSize = 30f
        selectedTextPaint = Paint()
        selectedTextPaint.textSize = 30f
        selectedTextPaint.color = resources.getColor(R.color.selectedTextBG)
    }

    fun setSelectionBackgroundColor(color: Int) {
        selectedTextPaint.color = resources.getColor(color)
    }

    fun setDrawables(drawableLeft: Drawable, drawableRight: Drawable) {
        this.drawableLeft = drawableLeft
        this.drawableRight = drawableRight
        invalidate()
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        this.canvas = canvas
        for (model in selectedTextModels!!) {
            if (model.isSelected) {
                canvas.drawRect(model.rect, selectedTextPaint)
            }
        }
        if (tracing) {
            if (selectionPos.blockIndex != -1) {
                for (model in textModels!!) {
                    if (selectionPos.blockIndex == model.blockIndex && selectionPos.lineIndex == model.lineIndex && selectionPos.elementIndex == model.elementIndex) {
                        startingTextModel = model
                    }
                    if (endPos.blockIndex == model.blockIndex && endPos.lineIndex == model.lineIndex && endPos.elementIndex == model.elementIndex) {
                        endingTextModel = model
                    }
                }
                DrawCursor(canvas)
            }

        }
    }

    private fun DrawCursor(canvas: Canvas) {
        if (startingTextModel != null) {
            drawableLeft!!.setBounds(
                startingTextModel!!.rect.left - drawableLeft!!.intrinsicWidth + 10,
                startingTextModel!!.rect.top,
                startingTextModel!!.rect.left,
                startingTextModel!!.rect.bottom
            )
            drawableLeft!!.draw(canvas)
        }
        if (endingTextModel != null) {
            drawableRight!!.setBounds(
                endingTextModel!!.rect.right,
                endingTextModel!!.rect.top,
                endingTextModel!!.rect.right + drawableRight!!.intrinsicWidth,
                endingTextModel!!.rect.bottom
            )
            drawableRight!!.draw(canvas)
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onTouchEvent(event: MotionEvent): Boolean {
        val x = event.x
        val y = event.y
        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                tracing = true
                selectedTextModels!!.clear()
                for (textModel in textModels!!) {
                    if (textModel.rect.contains(x.toInt(), y.toInt())) {
                        selectionPos.setNewSelections(
                            textModel.blockIndex, textModel.lineIndex, textModel.elementIndex
                        )
                        endPos.setNewSelections(
                            textModel.blockIndex, textModel.lineIndex, textModel.elementIndex
                        )
                        selectByRange(selectionPos, endPos)
                        if (!textModel.isSelected && !selectedTextModels!!.contains(textModel)) {
                            selectedTextModels!!.add(textModel)
                            currentSelection = textModel
                            textModel.isSelected = true
                        }
                        break
                    }
                }
                invalidate()
            }

            MotionEvent.ACTION_MOVE -> {
                for (textModel in textModels!!) {
                    if (textModel.rect.contains(x.toInt(), y.toInt())) {
                        if (textModel.elementIndex > selectionPos.elementIndex) {
                            endPos.setNewSelections(
                                textModel.blockIndex, textModel.lineIndex, textModel.elementIndex
                            )
                            selectByRange(selectionPos, endPos)
                        }
                    }
                }
                invalidate()
            }

            MotionEvent.ACTION_UP -> {
                tracing = false
                selectionPos.setNewSelections(-1, 0, 0)
                setOnTextListener(object : onTextSelectedListener {
                    override fun TextRecievedListener(textRecieved: StringBuilder) {
                        Log.d("TOVGV2", "onTouchEvent: $textRecieved")
                    }
                })
                this.canvas?.let { DrawCursor(it) }
            }
        }
        return true
    }

    private fun selectByRange(startingRange: SelectionModel, endingRange: SelectionModel) {
        for (textModel in textModels!!) {
            val toBeSelected = isInsideRange(textModel, startingRange, endingRange)
            textModel.isSelected = toBeSelected
            if (toBeSelected) {
                if (!selectedTextModels!!.contains(textModel)) {
                    selectedTextModels?.add(textModel)
                }
            }

        }
    }

    private fun isInsideRange(
        textModel: TextModel, startingRange: SelectionModel, endingRange: SelectionModel
    ): Boolean {
        val blockInRange = textModel.blockIndex in startingRange.blockIndex..endingRange.blockIndex
        val lineInRange = when (textModel.blockIndex) {
            startingRange.blockIndex -> {
                if (textModel.blockIndex == endingRange.blockIndex) {
                    // Selection is within the same block
                    textModel.lineIndex in startingRange.lineIndex..endingRange.lineIndex
                } else {
                    // Selection spans multiple blocks, and textModel is in the starting block
                    textModel.lineIndex >= startingRange.lineIndex
                }
            }

            endingRange.blockIndex -> {
                // Selection spans multiple blocks, and textModel is in the ending block
                textModel.lineIndex <= endingRange.lineIndex
            }

            else -> true
        }
        val elementInRange = when {
            textModel.blockIndex == startingRange.blockIndex && textModel.lineIndex == startingRange.lineIndex -> {
                if (textModel.lineIndex == endingRange.lineIndex) {
                    // Selection is within the same line
                    textModel.elementIndex in startingRange.elementIndex..endingRange.elementIndex
                } else {
                    // Selection spans multiple lines, and textModel is in the starting line
                    textModel.elementIndex >= startingRange.elementIndex
                }
            }

            textModel.blockIndex == endingRange.blockIndex && textModel.lineIndex == endingRange.lineIndex -> {
                // Selection spans multiple lines, and textModel is in the ending line
                textModel.elementIndex <= endingRange.elementIndex
            }

            else -> true
        }
        return blockInRange && lineInRange && elementInRange
    }

    private fun logdAllTextModels() {
        for (models: TextModel in this.textModels!!) {
            Log.d(
                "TOVGV2",
                "LogdAllTextModels: text: " + models.text + ",rect: " + models.rect + ",isSelected: " + models.isSelected + ",textBlock_index: " + models.blockIndex + ",line_index:" + models.lineIndex + ",element_index:" + models.elementIndex
            )
        }

    }

    fun setOnTextListener(listener: onTextSelectedListener) {
        val string: StringBuilder = java.lang.StringBuilder()
        for (text: TextModel in selectedTextModels!!) {
            string.append(" " + text.text)
        }
        listener.TextRecievedListener(string)
    }

    fun addTextModel(textList: Text) {
        for (textBlock in textList.textBlocks.withIndex()) {
            for (textLine in textBlock.value.lines.withIndex()) {
                for (textElement in textLine.value.elements.withIndex()) {
                    this.textModels?.add(
                        TextModel(
                            textElement.value.text,
                            textElement.value.boundingBox!!,
                            false,
                            textBlock.index, // textBlock index
                            textLine.index, // Line index
                            textElement.index // Element index
                        )
                    )
                }
            }
        }
//        LogdAllTextModels()
        invalidate()
    }
}

class TextModel(
    var text: String,
    var rect: Rect,
    var isSelected: Boolean = false,
    var blockIndex: Int,
    var lineIndex: Int,
    var elementIndex: Int
)