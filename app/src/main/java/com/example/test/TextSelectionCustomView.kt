package com.example.test

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.drawable.Drawable
import android.util.AttributeSet
import android.view.View
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import com.google.mlkit.vision.text.Text
import java.lang.Integer.min

class TextSelectionCustomView(context: Context, attributeSet: AttributeSet?) :
    View(context, attributeSet) {
    private var selectionPaint = Paint()
    private var text_list: Text? = null
    private var customWidth: Int = 0
    private var customHeight: Int = 0
    private var drawable: Drawable? = null

    init {
        selectionPaint.color = resources.getColor(R.color.iconGreyColor)
        selectionPaint.alpha = 50
        selectionPaint.strokeWidth = 2F
    }

    fun setTextList(listTextBlock: Text) {
        text_list = listTextBlock
        invalidate()
    }

    // Public method to set the custom width
    fun setCustomWidth(width: Int) {
        customWidth = width
        requestLayout()
    }

    // Public method to set the custom height
    fun setCustomHeight(height: Int) {
        customHeight = height
        requestLayout()
    }
    fun setDrawable(drawableToSet: Drawable){
        drawable = drawableToSet
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        setMeasuredDimension(customWidth, customHeight)
        invalidate()
    }

    @SuppressLint("DrawAllocation")
    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        if (text_list != null) {
            for (textBlock in text_list!!.textBlocks) {
                for (textLine in textBlock.lines) {
                    for (textElement in textLine.elements) {
                        if (drawable !=null){
                            drawable!!.setBounds(textElement.boundingBox!!.left, textElement.boundingBox!!.top, textElement.boundingBox!!.right, textElement.boundingBox!!.bottom)
                            drawable!!.draw(canvas);
                        }else{
                            canvas.drawRect(textElement.boundingBox!!, selectionPaint)
                        }
                    }
                }
            }
        }
    }
}
