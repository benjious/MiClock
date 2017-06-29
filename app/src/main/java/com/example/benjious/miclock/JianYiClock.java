package com.example.benjious.miclock;

import android.animation.PropertyValuesHolder;
import android.animation.TimeInterpolator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Camera;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.DashPathEffect;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.SweepGradient;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.MotionEvent;
import android.view.View;

import java.util.Calendar;



/**
 * Created by Benjious on 2017/6/18.
 */

public class JianYiClock extends View {

    public static final String TAG = "MiClockView xyz =";


    /* 画布 */
    private Canvas mCanvas;
    /* 小时文本画笔 */
    private Paint mTextPaint;
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


    private int radiusScaleRing;
    private int radius;
    private int centerX;
    private int centerY;
    private float[][] hourTextCoordinates;
    private Paint paintBgRing;
    private Paint paintProgressRing;
    private float strokeWidthRing = 30;
    private float[] ringDashIntervals = new float[]{3f, 6f};


    public JianYiClock(Context context) {
        this(context, null);
    }

    public JianYiClock(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public JianYiClock(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        TypedArray ta = context.obtainStyledAttributes(attrs, R.styleable.MiClockView, defStyleAttr, 0);
        mBackgroundColor = ta.getColor(R.styleable.MiClockView_backgroundColor, Color.parseColor("#237EAD"));
        setBackgroundColor(mBackgroundColor);
        mLightColor = ta.getColor(R.styleable.MiClockView_lightColor, Color.parseColor("#ffffff"));
        mDarkColor = ta.getColor(R.styleable.MiClockView_darkColor, Color.parseColor("#80ffffff"));
        mTextSize = ta.getDimension(R.styleable.MiClockView_textSize, DensityUtils.sp2px(context, 14));
        ta.recycle();
        mHourHandPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mHourHandPaint.setStyle(Paint.Style.FILL);
        mHourHandPaint.setColor(mDarkColor);

        mMinuteHandPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mMinuteHandPaint.setColor(mLightColor);

        mSecondHandPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mSecondHandPaint.setStyle(Paint.Style.FILL);
        mSecondHandPaint.setColor(mLightColor);

        mScaleLinePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mScaleLinePaint.setStyle(Paint.Style.STROKE);
        mScaleLinePaint.setColor(mBackgroundColor);

        mScaleArcPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mScaleArcPaint.setStyle(Paint.Style.STROKE);

        mTextPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mTextPaint.setStyle(Paint.Style.FILL);
        mTextPaint.setColor(mDarkColor);
        mTextPaint.setTextSize(mTextSize);

        mCirclePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mCirclePaint.setStyle(Paint.Style.STROKE);
        mCirclePaint.setStrokeWidth(mCircleStrokeWidth);
        mCirclePaint.setColor(mDarkColor);

        mTextRect = new Rect();
        mCircleRectF = new RectF();
        mScaleArcRectF = new RectF();
        mHourHandPath = new Path();
        mMinuteHandPath = new Path();
        mSecondHandPath = new Path();

        mGradientMatrix = new Matrix();
        mCameraMatrix = new Matrix();
        mCamera = new Camera();

        // Stroke width
        strokeWidthRing = dp2px(25);
        //秒刻度背景画笔
        paintBgRing = new Paint();
        paintBgRing.setStyle(Paint.Style.STROKE);
        paintBgRing.setStrokeWidth(strokeWidthRing);
        paintBgRing.setAntiAlias(true);
        paintBgRing.setPathEffect(new DashPathEffect(ringDashIntervals, 0));
        paintBgRing.setColor(mBackgroundColor);
        //秒刻度移动画笔
        paintProgressRing = new Paint(paintBgRing);
        paintProgressRing.setColor(Color.WHITE);

    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        setMeasuredDimension(measureDimension(widthMeasureSpec), measureDimension(heightMeasureSpec));
    }


    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);

        //本来mRadius 是不用的，但是为了计算一些比例尺寸，还是留下了
        //宽和高分别去掉padding值，取min的一半即表盘的半径
        mRadius = Math.min(w - getPaddingLeft() - getPaddingRight(),
                h - getPaddingTop() - getPaddingBottom()) / 2;
        //根据比例确定默认padding大小
        mDefaultPadding = 0.12f * mRadius;
        mPaddingLeft = mDefaultPadding + w / 2 - mRadius + getPaddingLeft();
        mPaddingTop = mDefaultPadding + h / 2 - mRadius + getPaddingTop();
        mPaddingRight = mPaddingLeft;
        mPaddingBottom = mPaddingTop;

        mScaleLength = 0.12f * mRadius;//根据比例确定刻度线长度
        mScaleArcPaint.setStrokeWidth(mScaleLength);
        mScaleLinePaint.setStrokeWidth(0.012f * mRadius);
        mMaxCanvasTranslate = 0.02f * mRadius;

        //梯度扫描渐变，以(w/2,h/2)为中心点，两种起止颜色梯度渐变
        //float数组表示，[0,0.75)为起始颜色所占比例，[0.75,1}为起止颜色渐变所占比例
        mSweepGradient = new SweepGradient(w / 2, h / 2,
                new int[]{mDarkColor, mLightColor}, new float[]{0.75f, 1});

        radius = getWidth() * 3 / 7;
        radiusScaleRing = radius * 4 / 5;

        centerX = getWidth() / 2;
        centerY = getHeight() / 2;

        initHourText();
        initSecond();

        View view = new JianYiClock(getContext());
        view.setPadding((int) mPaddingLeft, (int) mPaddingTop, (int) mPaddingRight, (int) mPaddingBottom);

    }

    @Override
    protected void onDraw(Canvas canvas) {
        mCanvas = canvas;
        setCameraRotate();
        getTimeDegree();
        //小时Text，小时圆弧
          drawCenterLine();
        drawTimeText();
        //秒针刻度
        drawScaleLine();
        //表针指示
        drawSecondHand();
        //小时指示
        drawHourHand();
        //小时指示
        drawMinuteHand();

        invalidate();
    }


    @Override
    public boolean onTouchEvent(MotionEvent event) {
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                if (mShakeAnim != null && mShakeAnim.isRunning()) {
                    mShakeAnim.cancel();
                }
                getCameraRotate(event);
                getCanvasTranslate(event);
                break;
            case MotionEvent.ACTION_MOVE:
                //根据手指坐标计算camera应该旋转的大小
                getCameraRotate(event);
                getCanvasTranslate(event);
                break;
            case MotionEvent.ACTION_UP:
                //松开手指，时钟复原并伴随晃动动画
                startShakeAnim();
                break;
        }
        return true;
    }


    private void initSecond() {

    }

    private void initHourText() {

        float scaleTextWidthTwo = mTextPaint.measureText("12");
        float scaleTextWidthOne = mTextPaint.measureText("6");
        float scaleTextHeight = mTextPaint.measureText("12");

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
        Paint.FontMetrics fm = this.mTextPaint.getFontMetrics();
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


    private void drawCenterLine() {
        mCanvas.drawLine(0, getHeight() / 2, 3000, getHeight() / 2, mTextPaint);
        mCanvas.drawLine(getWidth() / 2, 0, getWidth() / 2, 3000, mTextPaint);

    }


    /**
     * 画最外圈的时间文本和4个弧线
     */
    private void drawTimeText() {
        mCanvas.save();
        mCanvas.drawText("12", hourTextCoordinates[0][0], hourTextCoordinates[0][1], mTextPaint);
        mCanvas.drawText("6", hourTextCoordinates[1][0], hourTextCoordinates[1][1], mTextPaint);
        mCanvas.drawText("9", hourTextCoordinates[2][0], hourTextCoordinates[2][1], mTextPaint);
        mCanvas.drawText("3", hourTextCoordinates[3][0], hourTextCoordinates[3][1], mTextPaint);


        //画四个圆弧
        mCircleRectF.set(centerX - radius, centerY - radius, centerX + radius, centerY + radius);
        for (int i = 0; i < 4; i++) {
            mCanvas.drawArc(mCircleRectF, -85 + 90 * i, 80, false, mCirclePaint);
        }
        mCanvas.restore();
    }

    /**
     * 画一圈梯度渲染的亮暗色渐变圆弧，重绘时不断旋转，上面盖一圈背景色的刻度线
     * Canvas.translate(): 表示平移，这里是为了达到立体偏移的效果
     * 当触摸的时候,晃动的效果
     * Canvas.rotate(): 绕着某个轴旋转一定的角度
     */
    private void drawScaleLine() {

        mCanvas.save();
        //当我触碰到clock表面时，为了有立体的效果，我们需要画布（mCanvas）x,y轴移动一段距离
        mCanvas.translate(mCanvasTranslateX, mCanvasTranslateY);
        mScaleArcRectF.set(getWidth() / 2 - radiusScaleRing, getHeight() / 2 - radiusScaleRing, getWidth() / 2 + radiusScaleRing, getHeight() / 2 + radiusScaleRing);

        //为paintProgressRing 设置旋转渐变颜色效果
        mGradientMatrix.setRotate(mSecondDegree - 90, getWidth() / 2, getHeight() / 2);
        mSweepGradient.setLocalMatrix(mGradientMatrix);
        paintProgressRing.setShader(mSweepGradient);

        //第一个圆弧是白色线圆弧（颜色比较浅），注意两个圆弧都是虚线，只是线宽比较大，
        mCanvas.drawArc(mScaleArcRectF, 0, 359.5f, false, paintBgRing);
        //第二个圆弧是颜色渐变效果
        mCanvas.drawArc(mScaleArcRectF, 0, 359.5f, false, paintProgressRing);
        mCanvas.restore();

    }

    /**
     * 画秒针，根据不断变化的秒针角度旋转画布
     */
    private void drawSecondHand() {
        mCanvas.save();
        mCanvas.translate(mCanvasTranslateX, mCanvasTranslateY);
        mCanvas.rotate(mSecondDegree, getWidth() / 2, getHeight() / 2);

        mSecondHandPath.reset();
        float offset = mPaddingTop + mTextRect.height() / 2;
        mSecondHandPath.moveTo(getWidth() / 2, offset + 0.26f * mRadius);
        mSecondHandPath.lineTo(getWidth() / 2 - 0.05f * mRadius, offset + 0.34f * mRadius);
        mSecondHandPath.lineTo(getWidth() / 2 + 0.05f * mRadius, offset + 0.34f * mRadius);
        mSecondHandPath.close();
        mSecondHandPaint.setColor(mLightColor);
        mCanvas.drawPath(mSecondHandPath, mSecondHandPaint);
        mCanvas.restore();
    }

    /**
     * 画时针，根据不断变化的时针角度旋转画布
     * 针头为圆弧状，使用二阶贝塞尔曲线
     */
    private void drawHourHand() {
        mCanvas.save();
        mCanvas.translate(mCanvasTranslateX * 1.2f, mCanvasTranslateY * 1.2f);
        mCanvas.rotate(mHourDegree, getWidth() / 2, getHeight() / 2);
        mHourHandPath.reset();
        float offset = mPaddingTop + mTextRect.height() / 2;
        //这里的y轴减去一段距离防止,
        mHourHandPath.moveTo(getWidth() / 2 - 0.018f * mRadius, getHeight() / 2 - 0.03f * mRadius);
        mHourHandPath.lineTo(getWidth() / 2 - 0.009f * mRadius, offset + 0.48f * mRadius);
        //二阶塞尔曲线,前两个参数是控制点的xy轴
        mHourHandPath.quadTo(getWidth() / 2, offset + 0.46f * mRadius,
                getWidth() / 2 + 0.009f * mRadius, offset + 0.48f * mRadius);
        mHourHandPath.lineTo(getWidth() / 2 + 0.018f * mRadius, getHeight() / 2 - 0.03f * mRadius);
        mHourHandPath.close();
        mHourHandPaint.setStyle(Paint.Style.FILL);
        mCanvas.drawPath(mHourHandPath, mHourHandPaint);

        mCircleRectF.set(getWidth() / 2 - 0.03f * mRadius, getHeight() / 2 - 0.03f * mRadius,
                getWidth() / 2 + 0.03f * mRadius, getHeight() / 2 + 0.03f * mRadius);
        //这里画了个圆环
        mHourHandPaint.setStyle(Paint.Style.STROKE);
        mHourHandPaint.setStrokeWidth(0.01f * mRadius);
        mCanvas.drawArc(mCircleRectF, 0, 360, false, mHourHandPaint);
        mCanvas.restore();
    }

    /**
     * 画分针，根据不断变化的分针角度旋转画布
     */
    private void drawMinuteHand() {
        mCanvas.save();
        mCanvas.translate(mCanvasTranslateX * 2f, mCanvasTranslateY * 2f);
        mCanvas.rotate(mMinuteDegree, getWidth() / 2, getHeight() / 2);
        mMinuteHandPath.reset();
        float offset = mPaddingTop + mTextRect.height() / 2;
        mMinuteHandPath.moveTo(getWidth() / 2 - 0.01f * mRadius, getHeight() / 2 - 0.03f * mRadius);
        mMinuteHandPath.lineTo(getWidth() / 2 - 0.008f * mRadius, offset + 0.365f * mRadius);
        mMinuteHandPath.quadTo(getWidth() / 2, offset + 0.345f * mRadius,
                getWidth() / 2 + 0.008f * mRadius, offset + 0.365f * mRadius);
        mMinuteHandPath.lineTo(getWidth() / 2 + 0.01f * mRadius, getHeight() / 2 - 0.03f * mRadius);
        mMinuteHandPath.close();
        mMinuteHandPaint.setStyle(Paint.Style.FILL);
        mCanvas.drawPath(mMinuteHandPath, mMinuteHandPaint);

        mCircleRectF.set(getWidth() / 2 - 0.03f * mRadius, getHeight() / 2 - 0.03f * mRadius,
                getWidth() / 2 + 0.03f * mRadius, getHeight() / 2 + 0.03f * mRadius);
        mMinuteHandPaint.setStyle(Paint.Style.STROKE);
        mMinuteHandPaint.setStrokeWidth(0.02f * mRadius);
        mCanvas.drawArc(mCircleRectF, 0, 360, false, mMinuteHandPaint);
        mCanvas.restore();

    }


    /**
     * 获取camera旋转的大小
     * 当camera.rotateX(x)的x为正时，图像绕X轴上半部分向里下半部分向外旋转
     * 当camera.rotateY(y)的y为正时，图像绕Y轴右半部分向里左半部分向外旋转
     * @param event motionEvent
     */
    private void getCameraRotate(MotionEvent event) {
        float rotateX =-(event.getY() - getHeight() / 2);
        float rotateY = (event.getX() - getWidth() / 2);
        //求出此时旋转的大小与半径之比
        float[] percentArr = getPercent(rotateX, rotateY);
        //最终旋转的大小按比例匀称改变
        mCameraRotateX = percentArr[0] * mMaxCameraRotate;
        mCameraRotateY = percentArr[1] * mMaxCameraRotate;
    }

    /**
     * 当拨动时钟时，会发现时针、分针、秒针和刻度盘会有一个较小的偏移量，形成近大远小的立体偏移效果
     * 一开始我打算使用 matrix 和 camera 的 mCamera.translate(x, y, z) 方法改变 z 的值
     * 但是并没有效果，所以就动态计算距离，然后在 onDraw()中分零件地 mCanvas.translate(x, y)
     *
     * @param event motionEvent
     */
    private void getCanvasTranslate(MotionEvent event) {
        //translate : 平移
        float translateX = (event.getX() - getWidth() / 2);
        float translateY = (event.getY() - getHeight() / 2);
        //求出此时位移的大小与半径之比
        float[] percentArr = getPercent(translateX, translateY);
        //最终位移的大小按比例匀称改变
        mCanvasTranslateX = percentArr[0] * mMaxCanvasTranslate;
        mCanvasTranslateY = percentArr[1] * mMaxCanvasTranslate;
    }

    /**
     * /**
     * 获取一个操作旋转或位移大小的比例
     *
     * @param x x大小
     * @param y y大小
     * @return 装有xy比例的float数组
     */
    private float[] getPercent(float x, float y) {
        float[] percentArr = new float[2];
        float percentX = x / mRadius;
        float percentY = y / mRadius;
        if (percentX > 1) {
            percentX = 1;
        } else if (percentX < -1) {
            percentX = -1;
        }
        if (percentY > 1) {
            percentY = 1;
        } else if (percentY < -1) {
            percentY = -1;
        }
        percentArr[0] = percentX;
        percentArr[1] = percentY;
        return percentArr;
    }

    /**
     * 设置3D时钟效果，触摸矩阵的相关设置、照相机的旋转大小
     * 应用在绘制图形之前，否则无效
     * 这个方法实际上就是钉住中心点（圆点）
     * 注意： 这里只是旋转了xy轴的角度，Z轴的角度没变
     */
    private void setCameraRotate() {
        mCameraMatrix.reset();
        mCamera.save();
        mCamera.rotateX(mCameraRotateX);//绕x轴旋转角度
        mCamera.rotateY(mCameraRotateY);//绕y轴旋转角度
        mCamera.getMatrix(mCameraMatrix);//相关属性设置到matrix中
        mCamera.restore();
        //camera在view左上角那个点，故旋转默认是以左上角为中心旋转
        //故在动作之前pre将matrix向左移动getWidth()/2长度，向上移动getHeight()/2长度
        mCameraMatrix.preTranslate(-getWidth() / 2, -getHeight() / 2);
        //在动作之后post再回到原位
        mCameraMatrix.postTranslate(getWidth() / 2, getHeight() / 2);
        mCanvas.concat(mCameraMatrix);//matrix与canvas相关联
    }

    /**
     * 时钟晃动动画
     */
    private void startShakeAnim() {
        final String cameraRotateXName = "cameraRotateX";
        final String cameraRotateYName = "cameraRotateY";
        final String canvasTranslateXName = "canvasTranslateX";
        final String canvasTranslateYName = "canvasTranslateY";
        //保持一个属性数据类,动画的效果正是围绕属性数据类运动的
        PropertyValuesHolder cameraRotateXHolder =
                PropertyValuesHolder.ofFloat(cameraRotateXName, mCameraRotateX, 0);
        PropertyValuesHolder cameraRotateYHolder =
                PropertyValuesHolder.ofFloat(cameraRotateYName, mCameraRotateY, 0);
        PropertyValuesHolder canvasTranslateXHolder =
                PropertyValuesHolder.ofFloat(canvasTranslateXName, mCanvasTranslateX, 0);
        PropertyValuesHolder canvasTranslateYHolder =
                PropertyValuesHolder.ofFloat(canvasTranslateYName, mCanvasTranslateY, 0);

        mShakeAnim = ValueAnimator.ofPropertyValuesHolder(cameraRotateXHolder,
                cameraRotateYHolder, canvasTranslateXHolder, canvasTranslateYHolder);
        mShakeAnim.setInterpolator(new TimeInterpolator() {
            @Override
            public float getInterpolation(float input) {
                //http://inloop.github.io/interpolator/
                //这个效果就像东西落入水面的效果
                float f = 0.571429f;
                return (float) (Math.pow(2, -2 * input) * Math.sin((input - f / 4) * (2 * Math.PI) / f) + 1);
            }
        });
//        mShakeAnim.setInterpolator(new BounceInterpolator());
        mShakeAnim.setDuration(1000);
        mShakeAnim.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                //获取每一帧计算好的属性值
                mCameraRotateX = (float) animation.getAnimatedValue(cameraRotateXName);
                mCameraRotateY = (float) animation.getAnimatedValue(cameraRotateYName);
                mCanvasTranslateX = (float) animation.getAnimatedValue(canvasTranslateXName);
                mCanvasTranslateY = (float) animation.getAnimatedValue(canvasTranslateYName);
            }
        });
        mShakeAnim.start();
    }

    /**
     * 获取当前时分秒所对应的角度
     * 为了不让秒针走得像老式挂钟一样僵硬，需要精确到毫秒
     */
    private void getTimeDegree() {
        Calendar calendar = Calendar.getInstance();
        float milliSecond = calendar.get(Calendar.MILLISECOND);
        float second = calendar.get(Calendar.SECOND) + milliSecond / 1000;
        float minute = calendar.get(Calendar.MINUTE) + second / 60;
        float hour = calendar.get(Calendar.HOUR) + minute / 60;
        mSecondDegree = second / 60 * 360;
        mMinuteDegree = minute / 60 * 360;
        mHourDegree = hour / 12 * 360;
    }

    public float dp2px(float dpValue) {
        return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dpValue, getResources().getDisplayMetrics());
    }

    public float sp2px(float spValue) {
        return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, spValue, getResources().getDisplayMetrics());
    }

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
}
