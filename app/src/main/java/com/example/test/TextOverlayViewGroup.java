package com.example.test;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Color;
import android.os.Build;
import android.util.AttributeSet;
import android.util.Log;
import android.util.TypedValue;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.RequiresApi;

import com.google.mlkit.vision.text.Text;
import com.google.mlkit.vision.text.Text.TextBlock;

public class TextOverlayViewGroup extends ViewGroup {

    private Text text;
    private int customWidth = 0;
    private int customHeight = 0;

    public TextOverlayViewGroup(Context context, AttributeSet attrs) {
        super(context, attrs);
        setClickable(true);
    }

    public void setText(Text text) {
        this.text = text;
        requestLayout();
    }

    public void setCustomWidth(int width) {
        customWidth = width;
        requestLayout();
    }

    public void setCustomHeight(int height) {
        customHeight = height;
        requestLayout();
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
//        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        if (customHeight != 0 && customWidth != 0) {
            setMeasuredDimension(customWidth, customHeight);
        } else {
            setMeasuredDimension(widthMeasureSpec, heightMeasureSpec);
        }
        invalidate();
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        if (text != null) {
            for (TextBlock textBlock : text.getTextBlocks()) {
                int left = textBlock.getBoundingBox().left;
                int top = textBlock.getBoundingBox().top;
                int right = textBlock.getBoundingBox().right;
                int bottom = textBlock.getBoundingBox().bottom;
                TextView combinedTextView = new TextView(getContext());
                StringBuilder combinedText = new StringBuilder();
                for (Text.Line line : textBlock.getLines()) {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                            createTextViewForElement(line, combinedText);
                        }
                }
                int textWidth = right - left;
                int textHeight = bottom - top;
                float textSize = Math.min(textWidth, textHeight) * 0.4f;
                combinedTextView.setTextSize(TypedValue.COMPLEX_UNIT_PX, textSize);
                combinedTextView.setTextColor(Color.RED);
                combinedTextView.setBackgroundColor(Color.TRANSPARENT);
                combinedTextView.layout(left, top, right, bottom);
                combinedTextView.setText(combinedText.toString());
                combinedTextView.setTextIsSelectable(true);
                addView(combinedTextView);
                Log.d("TextOverlayViewGroup", "onLayout: combinedTextView, x:" + combinedTextView.getX() + ", y:" + combinedTextView.getY() + ", text:" + combinedTextView.getText());
            }
        }
    }


    @Override
    public boolean onTouchEvent(MotionEvent event) {
        float x = event.getX();
        float y = event.getY();
        if (event.getAction() == MotionEvent.ACTION_DOWN) {
            checkTouchOnTextViews(x, y);
        }
        return true;
    }
    private TextView checkTouchOnTextViews(float x, float y) {
        for (int i = 0; i < getChildCount(); i++) {
            View child = getChildAt(i);
            if (child instanceof TextView) {
                TextView textView = (TextView) child;
                if (isTouchOnTextView(textView, x, y)) {
                        return textView;
                }
            }
        }
        return null;
    }

    private boolean isTouchOnTextView(TextView textView, float x, float y) {
        int[] location = new int[2];
        textView.getLocationOnScreen(location);
        int left = location[0];
        int top = location[1];
        int right = left + textView.getWidth();
        int bottom = top + textView.getHeight();
        return x >= left && x <= right && y >= top && y <= bottom;
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    @SuppressLint("ClickableViewAccessibility")
    private void createTextViewForElement(Text.Line element, StringBuilder combinedText) {
        int left = element.getBoundingBox().left;
        int top = element.getBoundingBox().top;
        int right = element.getBoundingBox().right;
        int bottom = element.getBoundingBox().bottom;

        @SuppressLint("DrawAllocation") TextView textView = new TextView(getContext());
        textView.setText(element.getText());

        // Make the TextView transparent
        textView.setTextColor(Color.TRANSPARENT);
        textView.setBackgroundColor(Color.TRANSPARENT);

        // Calculate the text size based on the bounding box dimensions
        int textWidth = right - left;
        int textHeight = bottom - top;
        float textSize = Math.min(textWidth, textHeight) * 1f; // Adjust the scaling factor as needed

        textView.setTextSize(TypedValue.COMPLEX_UNIT_PX, textSize);

        textView.layout(left, top, right, bottom);
        addView(textView);

        combinedText.append(textView.getText());
        if (textView.getParent() != null) {
            ((ViewGroup) textView.getParent()).removeView(textView);
        }
        addView(textView);
    }
}
