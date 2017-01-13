package com.tct.weather.view;

import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PointF;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewParent;
import android.view.animation.AccelerateInterpolator;

import com.tct.weather.R;

/**
 * Created by jiajun.shen on 8/7/15.
 */
public class ExpandButton extends View {
    private float halfWidth, halfHeight;
    private Paint mPaint;
    private PointF mainPoint, leftPoint, rightPoint;
    private boolean isExpanded;
    private int mDegree = 0;
    private ExpandButtonListener mListener;
    Bitmap arrowBitmap;
    int arrowHeight;
    int arrowWidth;
    static private String TAG = "weather ExpandButton";

    public ExpandButton(Context context) {
        this(context, null);
    }

    public ExpandButton(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public ExpandButton(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);


        mPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mPaint.setColor(Color.DKGRAY);
        mPaint.setStrokeWidth(5);
        mPaint.setStrokeCap(Paint.Cap.ROUND);
        arrowBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.arrow_up_b);
        arrowHeight = arrowBitmap.getHeight() / 2;
        arrowWidth = arrowBitmap.getWidth() / 2;
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        halfWidth = w / 2;
        halfHeight = h / 2;


//        mainPoint = new PointF(halfWidth, halfHeight);
//        leftPoint = new PointF((float) (0.7 * halfWidth), (float) (0.3 * halfWidth + halfHeight));
//        rightPoint = new PointF((float) (1.3 * halfWidth), (float) (0.3 * halfWidth + halfHeight));

    }


    @Override
    protected void onDraw(Canvas canvas) {
        //Log.i(TAG, "ondraw mdegree=" + mDegree);
        super.onDraw(canvas);
        canvas.save();
        canvas.rotate(mDegree, halfWidth, halfHeight);
        canvas.drawBitmap(arrowBitmap, halfWidth - arrowWidth, halfHeight - arrowHeight, mPaint);
//        canvas.drawLine(leftPoint.x, leftPoint.y, mainPoint.x, mainPoint.y, mPaint);
//        canvas.drawLine(mainPoint.x, mainPoint.y, rightPoint.x, rightPoint.y, mPaint);
        canvas.restore();
    }


    @Override
    public boolean onTouchEvent(MotionEvent event) {
        int action = event.getAction();
        ViewParent parent = getParent();
        if (parent != null) {
            parent.requestDisallowInterceptTouchEvent(true);
        }
        if (action == MotionEvent.ACTION_DOWN) {
            this.setPressed(true);
            Log.i(TAG, "ontouch degree=" + mDegree + "isExpand" + isExpanded);
            if (mListener != null) {
                mListener.onClick();
            }
            startAnimation();
            isExpanded = !isExpanded;
        }
        return true;
    }

    private void startAnimation() {
        ValueAnimator valueAnimator;
        if (isExpanded) {
            valueAnimator = ValueAnimator.ofFloat(1, 0);
        } else {
            valueAnimator = ValueAnimator.ofFloat(0, 1);
        }
        valueAnimator.setDuration(200);
        valueAnimator.setInterpolator(new AccelerateInterpolator());
        valueAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator valueAnimator) {
                Float value = (Float) valueAnimator.getAnimatedValue();
                mDegree = (int) (180 * value);
                invalidate();
            }
        });
        setPressed(false);
        valueAnimator.start();
    }

    public void setColor(int color) {
        mPaint.setColor(color);
        if (color != getResources().getColor(R.color.text_color_black)) {
            arrowBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.arrow_up_p);
            arrowHeight = arrowBitmap.getHeight() / 2;
            arrowWidth = arrowBitmap.getWidth() / 2;
        }
        invalidate();
    }

//    static public boolean isExpanded()
//    {
//        Log.e("SJJ","button isExpand"+isExpanded);
//        return isExpanded;
//    }

    public void setExpandButtonListener(ExpandButtonListener expandButtonListener) {
        mListener = expandButtonListener;
    }

    interface ExpandButtonListener {
        void onClick();
    }
}
