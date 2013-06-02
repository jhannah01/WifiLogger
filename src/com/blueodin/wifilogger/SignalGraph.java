package com.blueodin.wifilogger;

import android.content.Context;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.graphics.Path;
import android.graphics.Path.Direction;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.View;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

public class SignalGraph extends View {
	private List<Integer> mValues = new ArrayList<Integer>();
	
	private float mScaleFactor = 1.0f;
	private ScaleGestureDetector mScaleGestureDetector;

	private Paint mLinePaint;
	private Path mLinePath;
	private Paint mGridPaint;
	private Paint mFillPaint;
	public double maxValue;

	private Paint mOutlinePaint;
	private float mLastTouchX;
	private float mPosX;

	private boolean mScrollable = false;

	private int mBackgroundColor;
	private float mMargin;

	public SignalGraph(Context context, AttributeSet attrs) {
		super(context, attrs);
		setup(attrs);
	}

	public SignalGraph(Context context) {
		super(context);
		setup();
	}

	public SignalGraph(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		setup(attrs);
	}

	public static float dp(float value, Resources resources) {
		return TypedValue.applyDimension(1, value,
				resources.getDisplayMetrics());
	}

	public static float pxToDp(float value, Resources resources) {
		return value / (resources.getDisplayMetrics().densityDpi / 160.0f);
	}

	private void setup() {
		mScaleGestureDetector = new ScaleGestureDetector(getContext(),
				new ScaleListener());

		mLinePaint = new Paint();
		mLinePaint.setAntiAlias(true);
		mLinePaint.setColor(Color.argb(200, 17, 125, 187));
		mLinePaint.setStyle(Style.STROKE);
		mLinePaint.setStrokeWidth(4.0f);
		mLinePaint.setShadowLayer(4, 2, 2, Color.argb(200, 17, 125, 187));

		mOutlinePaint = new Paint();
		mOutlinePaint.setAntiAlias(true);
		mOutlinePaint.setColor(Color.argb(200, 0x15, 0x69, 0x7d));
		mOutlinePaint.setStyle(Paint.Style.STROKE);
		mOutlinePaint.setStrokeWidth(4.0f);

		mFillPaint = new Paint();
		mFillPaint.setAntiAlias(true);
		mFillPaint.setColor(Color.argb(50, 17, 125, 187));

		mMargin = dp(20.0f, getResources());
		mBackgroundColor = -1;

		mGridPaint = new Paint();
		mGridPaint.setAntiAlias(true);
		mGridPaint.setColor(Color.rgb(217, 234, 244));

		mLinePath = new Path();

		maxValue = -1.0d;
	}

	private void setup(AttributeSet attrs) {
		setup();

		if (attrs == null)
			return;

		TypedArray typedArray = getContext().getTheme().obtainStyledAttributes(
				attrs, R.styleable.SignalGraph, 0, 0);

		try {
			mLinePaint.setStrokeWidth(dp(typedArray.getFloat(
					R.styleable.SignalGraph_lineWidth, 2.0f), getResources()));
			mLinePaint.setColor(typedArray.getInt(
					R.styleable.SignalGraph_lineColor,
					Color.argb(200, 17, 125, 187)));
			mFillPaint.setColor(typedArray.getInt(
					R.styleable.SignalGraph_fillColor,
					Color.argb(50, 17, 125, 187)));
			mBackgroundColor = typedArray.getInt(
					R.styleable.SignalGraph_backgroundColor,
					Color.rgb(255, 255, 255));
			mGridPaint
					.setColor(typedArray.getInt(
							R.styleable.SignalGraph_gridColor,
							Color.rgb(217, 234, 244)));
			mGridPaint.setStrokeWidth(dp(typedArray.getFloat(
					R.styleable.SignalGraph_gridWidth, 1.0F), getResources()));
			mMargin = dp(typedArray.getFloat(
					R.styleable.SignalGraph_valuesMargin, 20.0F),
					getResources());
		} finally {
			typedArray.recycle();
		}
	}

	public void addValue(int value, boolean redraw) {
		mValues.add(value);

		if (redraw)
			postInvalidate();
	}

	@Override
	protected void onDraw(Canvas canvas) {
		float height = canvas.getHeight() - 20;
		float width = canvas.getWidth();
		
		// int c = (int)(mValues.size() * mScaleFactor);
		int c = Math.min(mValues.size(), 80);
		
		List<Integer> values = mValues.subList(mValues.size()-c, mValues.size());
		
		int maxValue = getMaxValue(values);
		int minValue = getMinValue(values);
		
		float step = width / (c - 1);
		float diffY = maxValue - minValue;

		mLinePath.reset();
		float y = 0.0f;
		float lastY = 0.0f;

		canvas.save();
		canvas.translate(mPosX, 0);

		for (int i = 0; i < c; i++) {
			float valY = Math.abs(values.get(i).floatValue()) - minValue;
			float ratY = valY / diffY;
			lastY = y;
			y = height * ratY;
			float x = i * step;

			y = Math.max(15.0f, y);
			y = Math.min(height-15.0f, y);

			if (i == 0)
				mLinePath.moveTo(10.0f, y);
			else {
				if (i == (c - 1))
					x -= 10.0f;

				mLinePath.lineTo(x, y);
			}

			if (Math.abs(lastY - y) > 10) {
				mLinePath.addCircle(x, y, 8, Direction.CW);
				canvas.drawText(String.format("#%d", i), x, height + 10.0f,
						mGridPaint);
			}

		}

		mLinePath.moveTo(0.0f, y);
		// mLinePath.close();

		// canvas.scale(mScaleFactor, mScaleFactor);
		canvas.drawPath(mLinePath, mLinePaint);
		canvas.restore();

		canvas.drawRect(0.0f, 0.0f, getWidth(), getHeight(), mOutlinePaint);
	}

	@Override
	public boolean onTouchEvent(MotionEvent event) {
		mScaleGestureDetector.onTouchEvent(event);

		switch (event.getAction()) {
		case MotionEvent.ACTION_DOWN:
			if (Math.abs(event.getX() - mLastTouchX) > 20)
				mLastTouchX = event.getX();
			else {
				RectF bounds = new RectF();
				mLinePath.computeBounds(bounds, true);
				float x = event.getX() - mPosX;
				if (x > bounds.left) {
					int idx = (int) ((x - bounds.left) / (bounds.right / mValues
							.size()));
					Toast.makeText(getContext(),
							String.format("Index: %d", idx), Toast.LENGTH_SHORT)
							.show();
				}
			}
			break;

		case MotionEvent.ACTION_MOVE:
			if (!mScaleGestureDetector.isInProgress() && mScrollable ) {
				float dx = event.getX() - mLastTouchX;
				float delta = Math.abs(dx);

				if ((delta < (getWidth() / 0.5)) && (delta > 10)) {
					mPosX += dx;
					invalidate();
				}

				mLastTouchX = event.getX();
			}
			break;

		case MotionEvent.ACTION_UP:

			break;
		}

		return true;
	}

	private int getMaxValue(List<Integer> values) {
		int max = 0;
		for (int val : values) {
			val = Math.abs(val);
			if (val > max)
				max = val;
		}

		return max;
	}

	private int getMinValue(List<Integer> values) {
		int min = Integer.MAX_VALUE;
		for (int val : values) {
			val = Math.abs(val);
			if (val < min)
				min = val;
		}

		return min;
	}

	private class ScaleListener extends
			ScaleGestureDetector.SimpleOnScaleGestureListener {
		@Override
		public boolean onScale(ScaleGestureDetector detector) {
			mScaleFactor /= detector.getScaleFactor();

			mScaleFactor = Math.max(0.4f, Math.min(mScaleFactor, 1.0f));

			invalidate();

			return true;
		}
	}

	public void resetValues() {
		mValues.clear();
		mPosX = 0;
		mScaleFactor = 1;
		invalidate();
	}
	
	public int getBackgroundColor() {
		return mBackgroundColor;
	}
	
	public void setBackgroundColor(int color) {
		mBackgroundColor = color;
		postInvalidate();
	}
	
	public float getMargin() {
		return mMargin;
	}
	
	public void setMargin(float margin) {
		mMargin = margin;
		postInvalidate();
	}
}
