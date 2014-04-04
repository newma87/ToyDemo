package com.smarttoy.client.ui;

import com.smarttoy.R;
import com.smarttoy.util.UtilHelper;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PixelFormat;
import android.graphics.Point;
import android.graphics.PorterDuff.Mode;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceHolder.Callback;
import android.view.SurfaceView;

public class VirtualJoyStick extends SurfaceView implements Callback {

	private SurfaceHolder m_holder;
	private Paint m_painter;

	private Point m_rockerPosition; // 摇杆位置
	private Point m_ctrlPoint;// 摇杆起始位置
	public int m_baseRadius; // 底盘半径
	public int m_thumbRadius; // 摇杆半径
	private int m_wheelRadius;// 摇杆活动范围半径

	private SingleRudderListener m_listener = null; // 事件回调接口

	private Bitmap m_baseBitmap; // 底盘
	private Bitmap m_thumbBitmap; // 摇杆

	public static final int ACTION_START = 1;
	public static final int ACTION_RUDDER = 2;
	public static final int ACTION_STOP = 3;

	private static final int EXTRA_SPACE = 20; // dip
	private static final int EFFECT_TIME = 50;	// (ms)响应时间间隔，大概1秒内响应20次
	
	public VirtualJoyStick(Context context) {
		super(context);
		init();
	}

	public VirtualJoyStick(Context context, AttributeSet attribute) {
		super(context, attribute);
		init();
	}

	private void init() {

		this.setKeepScreenOn(true);
		m_holder = getHolder();
		m_holder.addCallback(this);
		m_holder.setFormat(PixelFormat.TRANSPARENT);// 设置背景透明

		m_painter = new Paint();
		m_painter.setColor(Color.GREEN);
		m_painter.setAntiAlias(true);// 抗锯齿

		setFocusable(true);
		setFocusableInTouchMode(true);
		setZOrderOnTop(true);

		// joy stick base
		m_baseBitmap = BitmapFactory.decodeResource(getResources(),
				R.drawable.joy_stick_base);
		// joy stick thumb
		m_thumbBitmap = BitmapFactory.decodeResource(getResources(),
				R.drawable.joy_stick_thumb);
	}

	private void initDimession() {
		int w = getWidth();
		int h = getHeight();
		
		m_baseRadius = w - UtilHelper.dip2px(getContext(), EXTRA_SPACE);
		m_thumbRadius = m_baseRadius / 3;
		m_wheelRadius = m_baseRadius / 2;

		m_baseBitmap = Bitmap.createScaledBitmap(m_baseBitmap, m_baseRadius,
				m_baseRadius, false);
		m_thumbBitmap = Bitmap.createScaledBitmap(m_thumbBitmap, m_thumbRadius,
				m_thumbRadius, false);

		m_ctrlPoint = new Point(w / 2, h / 2);
		m_rockerPosition = new Point(m_ctrlPoint);
	}
	
	// 回调接口
	public interface SingleRudderListener {
		void onSteeringWheelChanged(int action, int angle);
	}

	// 设置回调接口
	public void setSingleRudderListener(SingleRudderListener rockerListener) {
		m_listener = rockerListener;
	}

	int len;
	@Override
	public boolean onTouchEvent(MotionEvent event) {
		try {
			switch (event.getAction()) {
			case MotionEvent.ACTION_DOWN:
				len = UtilHelper.getLength(m_ctrlPoint.x, m_ctrlPoint.y,
						(int) event.getX(), (int) event.getY());
				
				if (m_listener != null) {
					m_listener.onSteeringWheelChanged(ACTION_START, 0);
				}
				// 如果屏幕接触点不在摇杆挥动范围内,则不处理
				if (len > m_wheelRadius) {
					return true;
				}
				break;
			case MotionEvent.ACTION_MOVE:
				// 如果手指在摇杆活动范围内，则摇杆处于手指触摸位置
				m_rockerPosition = getBorderPoint(new Point((int) event.getX(),
						(int) event.getY()));
				if (m_listener != null) {
					float angle = UtilHelper.getAngle(m_ctrlPoint, new Point(
							(int) event.getX(), (int) event.getY()));
					m_listener.onSteeringWheelChanged(ACTION_RUDDER,
							Math.round(angle));
				}
				break;
			case MotionEvent.ACTION_UP:
				m_rockerPosition = new Point(m_ctrlPoint);
				if (m_listener != null) {
					m_listener.onSteeringWheelChanged(ACTION_STOP, 0);
				}
				break;
			}
			
			drawJoyStick();
			Thread.sleep(EFFECT_TIME);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return true;
	}

	private Point getBorderPoint(Point ptTarget) {
		int length = (m_ctrlPoint.x - ptTarget.x)
				* (m_ctrlPoint.x - ptTarget.x) + (m_ctrlPoint.y - ptTarget.y)
				* (m_ctrlPoint.y - ptTarget.y);
		int radius = m_wheelRadius - m_thumbRadius / 2;
		int maxLength = radius * radius;

		if (length <= maxLength) {
			return ptTarget;
		}

		if (length == 0) {
			return null;
		}

		double rate = radius / Math.sqrt(length);

		int x = Math.round((float) (rate * (ptTarget.x - m_ctrlPoint.x)));
		int y = Math.round((float) (rate * (ptTarget.y - m_ctrlPoint.y)));

		return new Point(m_ctrlPoint.x + x, m_ctrlPoint.y + y);
	}

	public void surfaceCreated(SurfaceHolder holder) {
		initDimession();
		drawJoyStick();
	}

	public void surfaceChanged(SurfaceHolder holder, int format, int width,
			int height) {
	}

	public void surfaceDestroyed(SurfaceHolder holder) {
	}
	
	private void drawJoyStick() {
		Canvas canvas = null;
		try {
			canvas = m_holder.lockCanvas();
			canvas.drawColor(Color.TRANSPARENT, Mode.CLEAR);// 清除屏幕
			canvas.drawBitmap(m_baseBitmap, m_ctrlPoint.x
					- (m_baseRadius / 2), m_ctrlPoint.y
					- (m_baseRadius / 2), m_painter);// 这里的50px是最外围的图片的半径
			canvas.drawBitmap(m_thumbBitmap, m_rockerPosition.x
					- (m_thumbRadius / 2), m_rockerPosition.y
					- (m_thumbRadius / 2), m_painter);// 这里的20px是最里面的图片的半径
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if (canvas != null) {
				m_holder.unlockCanvasAndPost(canvas);
			}
		}		
	}
}