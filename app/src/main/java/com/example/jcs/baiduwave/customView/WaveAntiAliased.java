package com.example.jcs.baiduwave.customView;

import android.animation.ValueAnimator;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
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
 * 2017/7/27 10:45
 * 用Xfermode方式代替Canvas.clipPath裁剪来消除锯齿
 *
 */
public class WaveAntiAliased extends View {
	private PorterDuffXfermode xfermode = new PorterDuffXfermode(PorterDuff.Mode.SRC_IN);
	private PorterDuffXfermode xfermode_text = new PorterDuffXfermode(PorterDuff.Mode.SRC_ATOP);
	private Paint mPaint;
	private Paint textPaint;
	private int mWidth = DimentionUtils.dip2px(getContext(), 50);
	private int mHeight = DimentionUtils.dip2px(getContext(), 50);
	private Path path;
	private float currentPercent;
	private int color;
	private String text = "贴";
	private int textSize = DimentionUtils.sp2px(getContext(), 25);

	private Bitmap bitmap;

	public WaveAntiAliased(Context context) {
		this(context, null);
	}

	public WaveAntiAliased(Context context, AttributeSet attrs) {
		this(context, attrs, 0);
	}

	public WaveAntiAliased(Context context, AttributeSet attrs, int defStyleAttr) {
		super(context, attrs, defStyleAttr);
		init(context, attrs);

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

		//新建图层实现离屏缓冲
		int flag = canvas.saveLayer(0, 0, canvas.getWidth(), canvas.getHeight(), null, Canvas.ALL_SAVE_FLAG);
			//绘制蓝色波浪
			canvas.drawPath(path, mPaint);
			mPaint.setXfermode(xfermode);
			//用带有SRC_IN模式的画笔绘制圆形图像，这样圆和波浪相交的地方就只显示圆了
			canvas.drawBitmap(bitmap,0,0,mPaint);
			textPaint.setXfermode(xfermode_text);
			//用带有SRC_ATOP的文字画笔绘制文字，这样在圆形波浪和文字相交的地方绘制文字，在不相交的地方绘制圆形波浪
			drawCenterText(canvas, textPaint, text);
		//合并图层到canvas上
		canvas.restoreToCount(flag);

		textPaint.setXfermode(null);
		mPaint.setXfermode(null);
	}

	/**
	 * 绘制圆形bitmap
	 * @param width
	 * @param height
	 * @return
	 */
	private Bitmap getCircleBitmap(int width, int height){
		Bitmap bitmap=Bitmap.createBitmap(width,height, Bitmap.Config.ARGB_8888);
//		bitmap.eraseColor(Color.parseColor("#FF0000"));//填充颜色
		Canvas canvas=new Canvas(bitmap);
		canvas.drawCircle(width / 2, height / 2, width / 2, mPaint);
		return bitmap;
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
		bitmap=getCircleBitmap(mWidth,mHeight);
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
