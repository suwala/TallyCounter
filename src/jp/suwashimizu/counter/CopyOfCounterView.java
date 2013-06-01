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
import android.graphics.Rect;
import android.util.AttributeSet;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;

public class CopyOfCounterView extends SurfaceView implements SurfaceHolder.Callback,Runnable{

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
	private Rect[] dsts;
	private Rect[] srcs;
	
	public CopyOfCounterView(Context context, AttributeSet attrs) {
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
	}

	public void setCounter(CounterDrum _counter){
		counter = _counter;
	}

	public void setCount(){

		myCount = counter.getCount();
		
		
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

				dsts = new Rect[counter.getLength()];
				srcs = new Rect[counter.getLength()];

				drums = new Bitmap[counter.getLength()];
				for(int i=0;i<drums.length;i++){

					drums[i] = Bitmap.createBitmap(w, h*11, Config.ARGB_8888);
					Canvas dCanvas = new Canvas(drums[i]);
					for(int j=11;j>0;j--){//(h -j*h)+h+h-textY
						dCanvas.drawText(String.valueOf(j!=1?11-j:0), 0, h*j+h - textY, paint);
					}

					srcs[i] = new Rect(0,0,w,h);
					dsts[i] = new Rect(w*i,0,w*i+w,h);
					
					
					

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
/*
	//LoopとPoolに分けてる途中 setCount時にLoopさせる
	private void drawCountLoop(){

		Canvas canvas = null;
		boolean countMath=false;
		
		//Set時の差を求める
		int difference = Math.abs(myCount - i_copyCount);
		for(int i=0;i<myCounts.length;i++){
			myCounts[i] = counter.getDigits(i);
		}
				
		while(thread != null){
			if(countMath)
				break;
			if(isCancel)
				break;
			try{
				
				canvas = mHolder.lockCanvas();

				if(canvas != null){
					countMath = true;
					canvas.drawColor(Color.GRAY);

					
					for(int i=0;i<drums.length;i++){
						if(isCancel)
							break;
						
						for(){
						
							//値は-9 ~ +9
							if((copyCount.charAt(i)-'0') - myCounts[i] != 0){
								countMath = false;


							}
						}
						//桁毎にマッチしてるか View側の桁数
						int myDigit = myCounts[i];
						int countDigit = counter.getDigits(i);


						if(myDigit != counter.getDigits(i)){

							countMath = false;

							int count = myDigit < counter.getDigits(i)?1:-1;

							if(myDigit == 9 && countDigit == 0)
								count = 1;
							if(myDigit == 0 && countDigit == 9)
								count = -1;

							srcs[i].offset(0, -velocity * count);
							h[i] += velocity;

							if(h[i] > height){

								h[i] = 0;
								myCounts[i] += count;
								srcs[i].set(0, height *10 - height * myCounts[i], drumOneWidth, height *10 -height * myCounts[i] +height);

								if(myCounts[i] > 9)
									myCounts[i] = 0;
								if(myCounts[i] < 0)
									myCounts[i] = 9;

							}


							//9Over
							if(srcs[i].top < 0){
								srcs[i].set(0, height * 10, drumOneWidth, height * 10 +height);
							}
							//0Less
							else if(srcs[i].top > height * 10){
								srcs[i].set(0, height, drumOneWidth, height+height);
							}

						}
						canvas.drawBitmap(drums[i], srcs[i], dsts[i], null);
					}

				}
			}finally{
				if(canvas != null){
					mHolder.unlockCanvasAndPost(canvas);
				}
			}
		}
		
		
		
		thread = null;
	}

	private void drawCountPool(){
		Canvas canvas=null;

		boolean countMath=false;
		int count=0;
		int lastCount = counter.getPool();

		while(thread != null && !countMath){
			try{
				//while(!countMath){
				countMath = true;
				canvas = mHolder.lockCanvas();
				if(canvas != null){

					canvas.drawColor(Color.RED);
					for(int i=0;i<drums.length;i++){

						//桁毎にマッチしてるか View側の桁数
						int myDigit = myCounts[i];
						int countDigit = counter.getDigits(i);

						if(myDigit != counter.getDigits(i)){
							countMath = false;
							//9 -> or <- 0のとき不具合あり
							//setした時Poolがないので不具合あり
							if(i == 4 && lastCount != 0){
								count = lastCount;
							}else{
								count = myDigit < counter.getDigits(i)?1:-1;

								if(myDigit == 9 && countDigit == 0)
									count = 1;
								if(myDigit == 0 && countDigit == 9)
									count = -1;
							}
							srcs[i].offset(0, -velocity * count);
							h[i] += velocity;

							if(h[i] > height){

								h[i] = 0;
								myCounts[i] += count;
								srcs[i].set(0, height *10 - height * myCounts[i], drumOneWidth, height *10 -height * myCounts[i] +height);

								if(myCounts[i] > 9)
									myCounts[i] = 0;
								if(myCounts[i] < 0)
									myCounts[i] = 9;

								if(i == 4)
									lastCount = counter.getPool();
							}


							//9Over
							if(srcs[i].top < 0){
								srcs[i].set(0, height * 10, drumOneWidth, height * 10 +height);
							}
							//0Less
							else if(srcs[i].top > height * 10){
								srcs[i].set(0, height, drumOneWidth, height+height);
							}

						}
						canvas.drawBitmap(drums[i], srcs[i], dsts[i], null);
					}
					//mHolder.unlockCanvasAndPost(canvas);
				}
				//}
			}finally{
				if(canvas != null){
					mHolder.unlockCanvasAndPost(canvas);
				}
			}

		}
		Log.d("counter:myCount",""+counter.getCount()+":"+myCount);
	}

	/*
	 * アニメは固定長ループ
	 * 固定長はドラムViewの高さ分で１ループ（数字1文字分）
	 * 
	 *Rect[]で現在値比較してadd subを決める
	 * 
	 *	 
	 */

	private int[] h=new int[5];
	static final int velocity = 20;
		
	@Override
	public synchronized void run() {

		
		Canvas canvas=null;

		boolean countMath=false;
		int count=0;
		int lastCount = counter.getPool();

		while(thread != null && !countMath){
			try{
				//while(!countMath){
				countMath = true;
				canvas = mHolder.lockCanvas();
				if(canvas != null){

					canvas.drawColor(Color.RED);
					for(int i=0;i<drums.length;i++){

						//桁毎にマッチしてるか View側の桁数
//						int myDigit = myCounts[i];
						int countDigit = counter.getDigits(i);

						//位置がわかる
						int myDigit =  (height * 10 - srcs[i].top) / height;
						
						//if(countDigit != )
						
						//ここから
						if(myDigit != countDigit){
							countMath = false;
							//9 -> or <- 0のとき不具合あり
							//setした時Poolがないので不具合あり
							
							if(i == 4 && lastCount != 0){
								count = lastCount;
							}else{
								count = myDigit < counter.getDigits(i)?1:-1;

								if(myDigit == 9 && countDigit == 0)
									count = 1;
								if(myDigit == 0 && countDigit == 9)
									count = -1;
							}
							srcs[i].offset(0, -velocity * count);
							h[i] += velocity;

							if(h[i] > height){

								h[i] = 0;
								myCounts[i] += count;
								srcs[i].set(0, height *10 - height * myCounts[i], drumOneWidth, height *10 -height * myCounts[i] +height);

								if(myCounts[i] > 9)
									myCounts[i] = 0;
								if(myCounts[i] < 0)
									myCounts[i] = 9;

								if(i == 4)
									lastCount = counter.getPool();
							}


							//9Over
							if(srcs[i].top < 0){
								srcs[i].set(0, height * 10, drumOneWidth, height * 10 +height);
							}
							//0Less
							else if(srcs[i].top > height * 10){
								srcs[i].set(0, height, drumOneWidth, height+height);
							}

						}
						//ここまでアニメ処理
						canvas.drawBitmap(drums[i], srcs[i], dsts[i], null);
					}
					//mHolder.unlockCanvasAndPost(canvas);
				}
				//}
			}finally{
				if(canvas != null){
					mHolder.unlockCanvasAndPost(canvas);
				}
			}

		}
		Log.d("counter:myCount",""+counter.getCount()+":"+myCount);
		thread = null;
		}
}
