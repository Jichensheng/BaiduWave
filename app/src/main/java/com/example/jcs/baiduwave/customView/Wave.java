package com.example.jcs.baiduwave.customView;

import android.animation.ValueAnimator;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.util.AttributeSet;
import android.view.View;
import android.view.animation.LinearInterpolator;

import com.example.jcs.baiduwave.R;
import com.example.jcs.baiduwave.tools.DimentionUtils;


/**
 * author：Jics
 * 2017/7/6 13:06
 */
public class Wave extends View {
    private Paint mPaint;
    private Paint textPaint;
    private int mWidth = DimentionUtils.dip2px(getContext(), 50);
    private int mHeight = DimentionUtils.dip2px(getContext(), 50);
    private Path path;
    private float currentPercent;
    private int color;
    private String text = "贴";
    private int textSize = DimentionUtils.sp2px(getContext(), 25);

    public Wave(Context context) {
        this(context, null);
    }

    public Wave(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public Wave(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context,attrs);

    }

    private void init(Context context, AttributeSet attrs) {
        TypedArray array = context.obtainStyledAttributes(attrs, R.styleable.Wave);
        //自定义颜色和文字
        color = array.getColor(R.styleable.Wave_color, Color.rgb(41, 163, 254));
        text = array.getString(R.styleable.Wave_text);
        array.recycle();
        //图形及路径填充画笔
        mPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mPaint.setStyle(Paint.Style.FILL);
        mPaint.setColor(color);
        mPaint.setDither(true);
        //文字画笔
        textPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        textPaint.setColor(Color.WHITE);
        textPaint.setTypeface(Typeface.DEFAULT_BOLD);
        //闭合波浪路径
        path = new Path();

        ValueAnimator animator = ValueAnimator.ofFloat(0, 1);
        animator.setDuration(1000);
        animator.setInterpolator(new LinearInterpolator());
        animator.setRepeatCount(ValueAnimator.INFINITE);
        animator.setRepeatMode(ValueAnimator.RESTART);
        animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                currentPercent = animation.getAnimatedFraction();
                invalidate();
            }
        });
        animator.start();
    }


    @Override
    protected void onDraw(Canvas canvas) {
        //底部的字
        textPaint.setColor(color);
        drawCenterText(canvas, textPaint, text);
        //上层的字
        textPaint.setColor(Color.WHITE);
        //生成闭合波浪路径
        path = getActionPath(currentPercent);
        canvas.saveLayer(0,0,mWidth,mHeight,mPaint,Canvas.ALL_SAVE_FLAG);
                //裁剪文字
                canvas.clipPath(path);
                //裁剪成圆形（SRC 源像素）
                canvas.drawCircle(mWidth / 2, mHeight / 2, mWidth / 2, mPaint);
                mPaint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.DST_IN));
                //画波浪(DST 目标像素)
                canvas.drawPath(path, mPaint);
                mPaint.setXfermode(null);
                drawCenterText(canvas, textPaint, text);
        canvas.restore();
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int widthSize = MeasureSpec.getSize(widthMeasureSpec);
        int widthMode = MeasureSpec.getMode(widthMeasureSpec);
        int heightSize = MeasureSpec.getSize(heightMeasureSpec);
        int heightMode = MeasureSpec.getMode(heightMeasureSpec);
        if (widthMode == MeasureSpec.EXACTLY) {
            mWidth = widthSize;
        }
        if (heightMode == MeasureSpec.EXACTLY) {
            mHeight = heightSize;
        }
        setMeasuredDimension(mWidth, mHeight);
        textSize = mWidth / 2;
        textPaint.setTextSize(textSize);
    }

    private Path getActionPath(float percent) {
        Path path = new Path();
        int x = -mWidth;
        //当前x点坐标（根据动画进度水平推移，一个动画周期推移的距离为一个mWidth）
        x += percent * mWidth;
        //波形的起点
        path.moveTo(x, mHeight / 2);
        //控制点的相对宽度
        int quadWidth = mWidth / 4;
        //控制点的相对高度
        int quadHeight = mHeight / 20 * 3;
        //第一个周期
        path.rQuadTo(quadWidth, quadHeight, quadWidth * 2, 0);
        path.rQuadTo(quadWidth, -quadHeight, quadWidth * 2, 0);
        //第二个周期
        path.rQuadTo(quadWidth, quadHeight, quadWidth * 2, 0);
        path.rQuadTo(quadWidth, -quadHeight, quadWidth * 2, 0);
        //右侧的直线
        path.lineTo(x + mWidth * 2, mHeight);
        //下边的直线
        path.lineTo(x, mHeight);
        //自动闭合补出左边的直线
        path.close();
        return path;
    }

    private void drawCenterText(Canvas canvas, Paint textPaint, String text) {
        Rect rect = new Rect(0, 0, mWidth, mHeight);
        textPaint.setTextAlign(Paint.Align.CENTER);

        Paint.FontMetrics fontMetrics = textPaint.getFontMetrics();
        float top = fontMetrics.top;
        float bottom = fontMetrics.bottom;

        int centerY = (int) (rect.centerY() - top / 2 - bottom / 2);

        canvas.drawText(text, rect.centerX(), centerY, textPaint);
    }
}
