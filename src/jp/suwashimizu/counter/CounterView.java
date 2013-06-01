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

public class CounterView extends SurfaceView implements SurfaceHolder.Callback,Runnable{

	private static final String TAG = "surface";

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
			canvas.drawColor(Color.BLACK);

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

					//srcs[i] = new Rect(0,height*10,w,height*10+h);
					srcs[i] = new Rect(0, height *10 - height * myCounts[i], drumOneWidth, height *10 -height * myCounts[i] +height);
					dsts[i] = new Rect(w*i,(int)(0+height-textY),w*i+w,(int)(h+height-textY));

				}
			}
			for(int i=0;i<drums.length;i++){
				//				canvas.drawBitmap(drums[i], i*w,(-height * 10) + height*0 + h - textY, null);
				Log.d(TAG,""+myCounts[i]);
				//canvas.drawBitmap(drums[i], i*drumOneWidth, (-height * 9) + height*myCounts[i] - textY , null);
				canvas.drawBitmap(drums[i], srcs[i], dsts[i], null);
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
	 * アニメは固定長ループ
	 * 固定長はドラムViewの高さ分で１ループ（数字1文字分）
	 * 
	 *Rect[]で現在値比較してadd subを決める
	 * 
	 *	 
	 */


	//末尾以外でカウントがずれるバグあり
	private int[] h=new int[5];
	static final int velocity = 20;
	private int[] updown = new int[5];

	@Override
	public synchronized void run() {


		Canvas canvas=null;

		boolean countMath=false;
		int count=0;
		int lastCount=0;

		while(thread != null && !countMath){
			try{
				//while(!countMath){
				countMath = true;
				canvas = mHolder.lockCanvas();
				if(canvas != null){

					canvas.drawColor(Color.BLACK);
					//poolから取りに行ってるのに同じ桁をチェックしてたので修正 先頭～末尾の手前までチェック
					
					
					//桁の差を求める
					for(int i=0;i<drums.length-1;i++){
						int countDigit = counter.getDigits(i);
						int viewDigit =  (height * 10 - srcs[i].top) / height;
						if(viewDigit == 10)
							viewDigit = 0;
						
						if(viewDigit != countDigit){
							updown[i] = viewDigit < countDigit?1:-1;
							
							if(viewDigit == 9 && countDigit == 0){
								count = 1;
								srcs[i].set(0, height * 10, drumOneWidth, height * 10 +height);
							}
							if(viewDigit == 0 && countDigit == 9){
								srcs[i].set(0, 0, drumOneWidth, height);
								count = -1;
							}
						}else
							updown[i] = 0;
					}
					
					
					for(int i=0;i<drums.length-1;i++){
/*
						//桁毎にマッチしてるか View側の桁数 なぜ毎回取りに行くのか
						int countDigit = counter.getDigits(i);

						//位置がわかる 0が2つある点に注意
						double viewDigit =  (double)((height * 10 - srcs[i].top)) / height;
						if(viewDigit == 10)
							viewDigit = 0;
				*/		
						
						
						//ここから
						if(updown[i] != 0){
							countMath = false;
							//9 -> or <- 0のとき不具合あり
							//setした時Poolがないので不具合あり
							count = updown[i];

							srcs[i].offset(0, velocity * -count);
							h[i] += velocity;

							//9Over
							//							if(srcs[i].top < 0){
							//								srcs[i].set(0, height * 10, drumOneWidth, height * 10 +height);
							//
							/*}
							if(srcs[i].top < 0){
								srcs[i].set(0, height * 10, drumOneWidth, height * 10 +height);
							}
							//0Less
							else if(srcs[i].top > height * 10){
								srcs[i].set(0, 0, drumOneWidth, height);
							}
							*/

							if(h[i] >= height){

								h[i] = 0;
								myCounts[i] += count;
								srcs[i].set(0, height *10 - height * myCounts[i], drumOneWidth, height *10 -height * myCounts[i] +height);

								if(myCounts[i] > 9)
									myCounts[i] = 0;
								if(myCounts[i] < 0)
									myCounts[i] = 9;

							}
						}
						//ここまでアニメ処理
						canvas.drawBitmap(drums[i], srcs[i], dsts[i], null);
					}//先頭4桁ループ終わり


					//末尾ループ

					//Poolなし
//					 = counter.getPool();
					if(lastCount == 0){
						lastCount = counter.getPool();
						double myDigit =  (height * 10 - srcs[4].top) /(double)height;
						int _d = counter.getDigits(4);
						if(myDigit != _d){
							lastCount = myDigit < _d?1:-1;

							if(myDigit == 9 && _d == 0)
								lastCount = 1;
							if(myDigit == 0 && _d == 9)
								lastCount = -1;
						}
					}

					if(lastCount != 0){
						countMath = false;

						//一番下でSubの時Topへ移動
//						if(srcs[4].top > height * 10 && lastCount < 0)
//							srcs[4].set(0, 0, drumOneWidth, +height);
						
						//9Over
						if(srcs[4].top < 0){
							srcs[4].set(0, height * 10, drumOneWidth, height * 10 +height);
						}
						//0Less
						else if(srcs[4].top > height * 10){
							srcs[4].set(0, height, drumOneWidth, height+height);
						}

						srcs[4].offset(0, -velocity * lastCount);
						h[4] += velocity;
						
						if(h[4] >= height){
							h[4] = 0;
							myCounts[4] += lastCount;

							if(myCounts[4] > 9)
								myCounts[4] = 0;
							if(myCounts[4] < 0)
								myCounts[4] = 9;

							lastCount = counter.getPool();
							srcs[4].set(0, height *10 - height * myCounts[4], drumOneWidth, height *10 -height * myCounts[4] +height);
							
//							//9Over
//							if(srcs[4].top < 0){
//								srcs[4].set(0, height * 10, drumOneWidth, height * 10 +height);
//							}
//							//0Less
//							else if(srcs[4].top > height * 10){
//								srcs[4].set(0, height, drumOneWidth, height+height);
//							}
						}
					}
					canvas.drawBitmap(drums[4], srcs[4], dsts[4], null);

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
