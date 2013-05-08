package jp.suwashimizu.counter;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Paint.FontMetrics;
import android.graphics.Paint.Style;
import android.util.AttributeSet;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;

public class _CounterView extends SurfaceView implements SurfaceHolder.Callback,Runnable{

	static final long FPS = 20;
	static final long FRAME_TIME = 1000 / FPS;
	static final int BALL_R = 30;
	SurfaceHolder surfaceHolder;
	Thread thread;
	int cx = BALL_R, cy = BALL_R;
	int screen_width, screen_height;

	public _CounterView(Context context,AttributeSet attrs) {
		super(context,attrs);
		surfaceHolder = getHolder();
		surfaceHolder.addCallback(this);
	}

	@Override
	public void run() {
				
		Canvas canvas = null;
		Paint paint = new Paint();
		Paint bgPaint = new Paint();
		
		// Background
		bgPaint.setStyle(Style.FILL);
		bgPaint.setColor(Color.WHITE);
		// Ball
		paint.setStyle(Style.FILL);
		paint.setColor(Color.BLUE);
		
		long loopCount = 0;
		long waitTime = 0;
		long startTime = System.currentTimeMillis();

		while(thread != null){

			try{
				loopCount++;
				canvas = surfaceHolder.lockCanvas();

				canvas.drawRect(
					0, 0, 
					screen_width, screen_height,
					bgPaint);
				canvas.drawCircle(
					cx++, cy++, BALL_R, 
					paint);

				surfaceHolder.unlockCanvasAndPost(canvas);

				waitTime = (loopCount * FRAME_TIME) 
					- System.currentTimeMillis() - startTime;

				if( waitTime > 0 ){
					Thread.sleep(waitTime);
				}
			}
			catch(Exception e){}
		}

	}

	@Override
	public void surfaceChanged(
		SurfaceHolder holder, 
		int format, 
		int width, 
		int height) {
		screen_width = width;
		screen_height = height;
	}

	@Override
	public void surfaceCreated(SurfaceHolder holder) {
		
		thread = new Thread(this);
		thread.start();
	}

	@Override
	public void surfaceDestroyed(SurfaceHolder holder) {
		thread = null;
	}
	
}