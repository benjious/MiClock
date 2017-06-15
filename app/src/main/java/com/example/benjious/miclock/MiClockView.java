package com.example.benjious.miclock;

import android.animation.ValueAnimator;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Camera;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.SweepGradient;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;


/**
 * Created by Benjious on 2017/6/9.
 */

public class MiClockView extends View {

    /* 画布 */
    private Canvas mCanvas;
    /* 小时文本画笔 */
    private Paint mHourTextPaint;
    /* 测量小时文本宽高的矩形 */
    private Rect mTextRect;
    /* 小时圆圈画笔 */
    private Paint mCirclePaint;
    /* 小时圆圈线条宽度 */
    private float mCircleStrokeWidth = 2;
    /* 小时圆圈的外接矩形 */
    private RectF mCircleRectF;
    /* 刻度圆弧画笔 */
    private Paint mScaleArcPaint;
    /* 刻度圆弧的外接矩形 */
    private RectF mScaleArcRectF;
    /* 刻度线画笔 */
    private Paint mScaleLinePaint;
    /* 时针画笔 */
    private Paint mHourHandPaint;
    /* 分针画笔 */
    private Paint mMinuteHandPaint;
    /* 秒针画笔 */
    private Paint mSecondHandPaint;
    /* 时针路径 */
    private Path mHourHandPath;
    /* 分针路径 */
    private Path mMinuteHandPath;
    /* 秒针路径 */
    private Path mSecondHandPath;

    /* 亮色，用于分针、秒针、渐变终止色 */
    private int mLightColor;
    /* 暗色，圆弧、刻度线、时针、渐变起始色 */
    private int mDarkColor;
    /* 背景色 */
    private int mBackgroundColor;
    /* 小时文本字体大小 */
    private float mTextSize;
    /* 时钟半径，不包括padding值 */
    private float mRadius;
    /* 刻度线长度 */
    private float mScaleLength;

    /* 时针角度 */
    private float mHourDegree;
    /* 分针角度 */
    private float mMinuteDegree;
    /* 秒针角度 */
    private float mSecondDegree;

    /* 加一个默认的padding值，为了防止用camera旋转时钟时造成四周超出view大小 */
    private float mDefaultPadding;
    private float mPaddingLeft;
    private float mPaddingTop;
    private float mPaddingRight;
    private float mPaddingBottom;


    //动画的设置
    /* 梯度扫描渐变 */
    private SweepGradient mSweepGradient;
    /* 渐变矩阵，作用在SweepGradient */
    private Matrix mGradientMatrix;
    /* 触摸时作用在Camera的矩阵 */
    private Matrix mCameraMatrix;
    /* 照相机，用于旋转时钟实现3D效果 */
    private Camera mCamera;
    /* camera绕X轴旋转的角度 */
    private float mCameraRotateX;
    /* camera绕Y轴旋转的角度 */
    private float mCameraRotateY;
    /* camera旋转的最大角度 */
    private float mMaxCameraRotate = 10;
    /* 指针的在x轴的位移 */
    private float mCanvasTranslateX;
    /* 指针的在y轴的位移 */
    private float mCanvasTranslateY;
    /* 指针的最大位移 */
    private float mMaxCanvasTranslate;
    /* 手指松开时时钟晃动的动画 */
    private ValueAnimator mShakeAnim;


    private int centerX;
    private int centerY;
    private float[][] hourTextCoordinates;
    private float radiusScaleRing;
    private float radius;
    private int colorScaleRing = 0xccdddddd;

    public static final String TAG = "MiClockView xyz =";

    public MiClockView(Context context) {
        this(context, null);
    }

    public MiClockView(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public MiClockView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        TypedArray ta = context.obtainStyledAttributes(attrs, R.styleable.MiClockView, defStyleAttr, 0);
        mBackgroundColor = ta.getColor(R.styleable.MiClockView_backgroundColor, Color.parseColor("#237EAD"));
        setBackgroundColor(mBackgroundColor);
        mLightColor = ta.getColor(R.styleable.MiClockView_lightColor, Color.parseColor("#ffffff"));
        mDarkColor = ta.getColor(R.styleable.MiClockView_darkColor, Color.parseColor("#80ffffff"));
        mTextSize = ta.getDimension(R.styleable.MiClockView_textSize, DensityUtils.sp2px(context, 13));
        ta.recycle();

        mHourHandPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mHourHandPaint.setStyle(Paint.Style.FILL);
        mHourHandPaint.setColor(mDarkColor);

        mMinuteHandPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mMinuteHandPaint.setColor(mLightColor);

        mSecondHandPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mSecondHandPaint.setStyle(Paint.Style.FILL);
        mSecondHandPaint.setColor(mLightColor);

        ///* 刻度线画笔
        /// Paint.Style.STROKE ：特点是描边，“边”可以调整大小
        mScaleLinePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mScaleLinePaint.setStyle(Paint.Style.STROKE);
        mScaleLinePaint.setColor(mBackgroundColor);

        ///* 刻度圆弧画笔 */
        mScaleArcPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mScaleArcPaint.setStyle(Paint.Style.STROKE);

        mHourTextPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mHourTextPaint.setAntiAlias(true);
        mHourTextPaint.setStyle(Paint.Style.FILL);
        // mHourTextPaint.setColor(mDarkColor);
        mHourTextPaint.setColor(colorScaleRing);
        mHourTextPaint.setTextSize(mTextSize);

        mCirclePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mCirclePaint.setStyle(Paint.Style.STROKE);
        mCirclePaint.setStrokeWidth(mCircleStrokeWidth);
        mCirclePaint.setColor(mDarkColor);
    }

//    @Override
//    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
//        setMeasuredDimension(measureDimension(widthMeasureSpec), measureDimension(heightMeasureSpec));
//    }

    private int measureDimension(int measureSpec) {
        int result;
        int mode = MeasureSpec.getMode(measureSpec);
        int size = MeasureSpec.getSize(measureSpec);
        if (mode == MeasureSpec.EXACTLY) {
            result = size;
        } else {
            result = 800;
            if (mode == MeasureSpec.AT_MOST) {
                result = Math.min(result, size);
            }
        }
        return result;
    }


    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        //宽和高分别去掉padding值，取min的一半即表盘的半径，在这个图形中，getPaddingTop,Left..都是0
        mRadius = Math.min(w - getPaddingLeft() - getPaddingRight(),
                h - getPaddingTop() - getPaddingBottom()) / 2;
        //加一个默认的padding值，为了防止用camera旋转时钟时造成四周超出view大小
        //根据比例确定默认padding大小
        mDefaultPadding = 0.12f * mRadius;
        //为了适配控件大小match_parent、wrap_content、精确数值以及padding属性
        //这个要结合图形看，可以计算出这样的padding可以使图形处于屏幕中心
        mPaddingLeft = mDefaultPadding + w / 2 - mRadius + getPaddingLeft();
        mPaddingTop = mDefaultPadding + h / 2 - mRadius + getPaddingTop();
        mPaddingRight = mPaddingLeft;
        mPaddingBottom = mPaddingTop;
        mScaleLength = 0.12f * mRadius;//根据比例确定刻度线长度
        mScaleArcPaint.setStrokeWidth(mScaleLength);//刻度盘的弧宽
        mScaleLinePaint.setStrokeWidth(0.012f * mRadius);//刻度线的宽度
        //梯度扫描渐变，以(w/2,h/2)为中心点，两种起止颜色梯度渐变
        //float数组表示，[0,0.75)为起始颜色所占比例，[0.75,1}为起止颜色渐变所占比例
        mSweepGradient = new SweepGradient(w / 2, h / 2,
                new int[]{mDarkColor, mLightColor}, new float[]{0.75f, 1});

        Log.d(TAG, "xyz  onSizeChanged: " + "getWidth()  " + getWidth());
        radius = getWidth() * 3 / 7;
        Log.d(TAG, "xyz  onSizeChanged: " + "radius :" + radius);
        radiusScaleRing = radius * 4 / 3;
        Log.d(TAG, "xyz  onSizeChanged: " + "radiusScla :" + radiusScaleRing);

        centerX = getWidth() / 2;
        centerY = getHeight() / 2;
        init();

    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        mCanvas = canvas;
        drawTimeText();
    }

    private void init() {

        float scaleTextWidthTwo = mHourTextPaint.measureText("12");
        float scaleTextWidthOne = mHourTextPaint.measureText("6");
        float scaleTextHeight = mHourTextPaint.measureText("12");

        float centerTop = centerY - radius;
        float centerBottom = centerY + radius;
        float centerLeft = getHeight() / 2;
        float centerRight = centerLeft;


        RectF topText = new RectF();
        topText.left = centerX - scaleTextWidthTwo / 2;
        topText.top = centerY - radius - scaleTextHeight / 2;
        topText.right = centerX + scaleTextWidthTwo / 2;
        topText.bottom = centerY - radius + scaleTextHeight / 2;


        RectF bottomText = new RectF();
        bottomText.left = centerX - scaleTextWidthOne / 2;
        bottomText.top = centerY + radius - scaleTextHeight / 2;
        bottomText.right = centerX + scaleTextWidthOne / 2;
        bottomText.bottom = centerY + radius + scaleTextHeight / 2;

        RectF leftTextBound = new RectF();
        leftTextBound.left = centerX - radius - scaleTextWidthOne / 2;
        leftTextBound.top = centerY - scaleTextHeight / 2;
        leftTextBound.right = centerX - radius + scaleTextWidthOne / 2;
        leftTextBound.bottom = centerY + scaleTextHeight / 2;

        RectF rightTextBound = new RectF();
        rightTextBound.left = centerX + radius - scaleTextWidthOne / 2;
        rightTextBound.top = leftTextBound.top;
        rightTextBound.right = centerX + radius + scaleTextWidthOne / 2;
        rightTextBound.bottom = leftTextBound.bottom;


        //baseline = center + (FontMetrics.bottom - FontMetrics.top)/2 - FontMetrics.bottom;
        //12,6,9,3
        Paint.FontMetrics fm = mHourTextPaint.getFontMetrics();
        hourTextCoordinates = new float[4][2];
        hourTextCoordinates[0][0] = topText.left;
        hourTextCoordinates[0][1] = centerTop + (fm.bottom - fm.top) / 2 - fm.bottom;
        hourTextCoordinates[1][0] = bottomText.left;
        hourTextCoordinates[1][1] = centerBottom + (fm.bottom - fm.top) / 2 - fm
                .bottom;
        hourTextCoordinates[2][0] = leftTextBound.left;
        hourTextCoordinates[2][1] = centerLeft + (fm.bottom - fm.top) / 2 - fm.bottom;
        hourTextCoordinates[3][0] = rightTextBound.left;
        hourTextCoordinates[3][1] = centerRight + (fm.bottom - fm.top) / 2 - fm.bottom;


    }


    public void drawTimeText() {
        mCanvas.save();
        mCanvas.drawText("12", hourTextCoordinates[0][0], hourTextCoordinates[0][1], mHourTextPaint);
        mCanvas.drawText("6", hourTextCoordinates[1][0], hourTextCoordinates[1][1], mHourTextPaint);
        mCanvas.drawText("9", hourTextCoordinates[2][0], hourTextCoordinates[2][1], mHourTextPaint);
        mCanvas.drawText("3", hourTextCoordinates[3][0], hourTextCoordinates[3][1], mHourTextPaint);

        //设置paint
        Paint paint = new Paint();
        paint.setTextSize(120); //以px为单位
        paint.setTextAlign(Paint.Align.LEFT);
        paint.setColor(Color.RED);
        mCanvas.drawLine(0, getHeight() / 2, 3000, getHeight() / 2, paint);
        mCanvas.drawLine(getWidth() / 2, 0, getWidth() / 2, 3000, paint);
        mCanvas.restore();


//
//        //画4个弧
//        mCircleRectF.set(mPaddingLeft + mTextRect.height() / 2 + mCircleStrokeWidth / 2,
//                mPaddingTop + mTextRect.height() / 2 + mCircleStrokeWidth / 2,
//                getWidth() - mPaddingRight - mTextRect.height() / 2 + mCircleStrokeWidth / 2,
//                getHeight() - mPaddingBottom - mTextRect.height() / 2 + mCircleStrokeWidth / 2);
//        for (int i = 0; i < 4; i++) {
//            mCanvas.drawArc(mCircleRectF, 5 + 90 * i, 80, false, mCirclePaint);
//        }

    }

}
