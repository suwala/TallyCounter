package jp.suwashimizu.counter;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Paint.FontMetrics;
import android.util.AttributeSet;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;

public class CounterView extends SurfaceView implements SurfaceHolder.Callback,Runnable{

	private Paint paint;
	private int[] myCounts;
	private int myCount;
	Bitmap[] drums;
	private int height;
	private int drumOneWidth;
	private Thread thread;
	private SurfaceHolder mHolder;
	private float textY;
	private CounterDrum counter;
	private List<Boolean>countPool;

	public CounterView(Context context, AttributeSet attrs) {
		super(context,attrs);
		initialize(context);
	}

	private void initialize(Context context){
		mHolder =  getHolder();
		mHolder.addCallback(this);

		paint = new Paint(Paint.ANTI_ALIAS_FLAG);
		paint.setColor(Color.WHITE);
		paint.setTextSize(100);
		myCounts = new int[]{0,0,0,0,0};
		countPool = new ArrayList<Boolean>();
	}

	public void setCounter(CounterDrum _counter){
		counter = _counter;
	}

	public void setCount(){

		if(thread == null){
			thread = new Thread(this);
			thread.start();
		}


	}

	//画面回転
	@Override
	public void surfaceChanged(SurfaceHolder holder, int format, int width,int height) {

	}

	//Resumeでも呼ばれる
	@Override
	public void surfaceCreated(SurfaceHolder holder) {

		Canvas canvas = null;
		try{
			canvas = holder.lockCanvas();
			canvas.drawColor(Color.RED);

			int w = canvas.getWidth()/5;
			int h = canvas.getHeight();
			height = h;
			drumOneWidth = w;
			paint.setTextSize(h);

			float baseY = h/2;
			FontMetrics fontMetrics = paint.getFontMetrics();
			textY = baseY - (fontMetrics.ascent - fontMetrics.descent)/2;
			if(drums == null){
				drums = new Bitmap[5];
				for(int i=0;i<drums.length;i++){

					drums[i] = Bitmap.createBitmap(w, h*11, Config.ARGB_8888);
					Canvas dCanvas = new Canvas(drums[i]);
					for(int j=11;j>0;j--){//(h -j*h)+h+h-textY
						dCanvas.drawText(String.valueOf(j!=1?11-j:0), 0, h*j+h - textY, paint);
					}


				}
			}
			for(int i=0;i<drums.length;i++){
				//				canvas.drawBitmap(drums[i], i*w,(-height * 10) + height*0 + h - textY, null);
				canvas.drawBitmap(drums[i], i*drumOneWidth, (-height * 9) + height*myCounts[i] - textY , null);
			}
		}finally{
			if(canvas != null)
				holder.unlockCanvasAndPost(canvas);
		}
	}

	//pausedでも呼ばれる
	@Override
	public void surfaceDestroyed(SurfaceHolder holder) {
		thread = null;
	}

	@Override
	public synchronized void run() {


		Canvas canvas=null;

		Matrix matrix = new Matrix();
		int[] h=new int[]{0,0,0,0,0};
		int velocity;

		while(thread != null){
			if(myCount == counter.getCount())
				break;

			//			for(int i=0;i<counter.getLength();i++)
			//				myCounts[i] = counter.getDigits(i);


			boolean isAdd;
			if(myCount < counter.getCount()){
				isAdd = true;
				velocity=20;
			}else{
				isAdd = false;
				velocity=-20;
			}

			//5桁分分離して表示
			try{
				canvas = mHolder.lockCanvas();
				if(canvas != null){
					canvas.drawColor(Color.RED);


					for(int i=0;i<drums.length;i++){
						//canvas.drawBitmap(drums[i], (i)*drumOneWidth, (-height * 9) + height*myCounts[i] + h[i] - textY , null);
						//桁毎に確認 0=先頭取得
						if(myCounts[i] != counter.getDigits(i)){
							//Log.d("myCount:counter",""+myCounts[i]+":"+counter.getDigits(i));
							h[i] += velocity;
							canvas.drawBitmap(drums[i], i*drumOneWidth, (-height * 9) + height*myCounts[i] + h[i] - textY , null);
							if( h[i] > height || h[i] < -height){

								h[i] = 0;
								myCounts[i] += isAdd == true?1:-1;
								if(myCounts[i] > 9)
									myCounts[i] = 0;
								if(myCounts[i] < 0)
									myCounts[i] = 9;


								if(i == 4)
									myCount += isAdd == true?1:-1;
							}
						}else{
							canvas.drawBitmap(drums[i], i*drumOneWidth, (-height * 9) + height*myCounts[i] + h[i] - textY , null);
						}
					}

//					for(int i=0;i<drums.length;i++){
//						//49 > 59 > 50 となる時がある原因調査
//						Log.d("ccc",""+myCounts[4-i]*Math.pow(10, i));
//						myCount += myCounts[4-i]*Math.pow(10, i);
//					}
					
					Log.d("counter:myCount",""+counter.getCount()+":"+myCount);
					Thread.sleep(10);
				}
			} catch (InterruptedException e) {
				e.printStackTrace();
			}finally{
				if(canvas != null){
					mHolder.unlockCanvasAndPost(canvas);
				}
			}
		}
		thread = null;
	}
}
