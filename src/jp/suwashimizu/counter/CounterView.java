package jp.suwashimizu.counter;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.BitmapFactory;
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

public class CounterView extends SurfaceView implements SurfaceHolder.Callback,Runnable{

	private static final String TAG = "surface";

	private Paint paint;
	private int[] myCounts;
	private int myCount;
	private Bitmap drums;
	private int height;
	private int drumOneWidth;
	private SurfaceHolder mHolder;
	private float textY;
	private CounterDrum counter;
	private Rect[] dsts;
	private Rect[] srcs;
	private Bitmap backImg;
	
	private Matrix mMatrix;
	
	public boolean isCreate;
	
	private boolean isDestroy;

	private ExecutorService animeExe = Executors.newFixedThreadPool(1);

	public CounterView(Context context, AttributeSet attrs) {
		super(context,attrs);
		initialize(context);
	}

	private void initialize(Context context){
		mHolder =  getHolder();
		mHolder.addCallback(this);

		paint = new Paint(Paint.ANTI_ALIAS_FLAG);
		paint.setColor(Color.rgb(235, 235, 235));
		paint.setTextSize(100);
		myCounts = new int[]{0,0,0,0,0};
	}

	public void setCounter(CounterDrum _counter){
		counter = _counter;
	}

	public void setCount(){

		myCount = counter.getCount();
		animeExe.execute(this);

	}

	//画面回転Resumeでやっぱ呼ばれる
	@Override
	public void surfaceChanged(SurfaceHolder holder, int format, int width,int height) {
		Log.d(TAG,"change!");
		
	}

	//Resumeでも呼ばれる
	@Override
	public void surfaceCreated(SurfaceHolder holder) {
		Log.d(TAG,"created!");
		isDestroy = false;
		Canvas canvas = null;
		try{
			canvas = holder.lockCanvas();
			canvas.drawColor(Color.DKGRAY);
			
			int w = (int) (canvas.getWidth()*0.75 / 5) ;
			int h =(int) (canvas.getWidth() / 4.5) ;//canvas.getHeight();
			height = h;
			drumOneWidth = w;
			paint.setTextSize(h);

			backImg = BitmapFactory.decodeResource(getResources(), R.drawable.rect4006);
			
			if(mMatrix == null){
				mMatrix = new Matrix();
				int imgW = backImg.getWidth();
				int viewW = canvas.getWidth();
				
				float size = (float)viewW / imgW;
				
				mMatrix.setScale(size, size);
			}
			
			
			float baseY = h/2;
			FontMetrics fontMetrics = paint.getFontMetrics();
			textY = baseY - (fontMetrics.ascent - fontMetrics.descent)/2;
			if(drums == null){

				dsts = new Rect[counter.getLength()];
				srcs = new Rect[counter.getLength()];
				
				drums = Bitmap.createBitmap(w, h*11, Config.ARGB_8888);
				Canvas dCanvas = new Canvas(drums);
				for(int j=11;j>0;j--){//(h -j*h)+h+h-textY
					dCanvas.drawText(String.valueOf(j!=1?11-j:0), 0, h*j+h - textY, paint);
				}
				
				for(int i=0;i<counter.getLength();i++){

					//srcs[i] = new Rect(0, height *10 - height * myCounts[i], drumOneWidth, height *10 -height * myCounts[i] +height);
					
					//マージンを取るよ
					dsts[i] = new Rect((int)(canvas.getWidth()*0.13+w*i),(int)(0+height-textY)+(int)(height*0.5f),
							(int)(canvas.getWidth()*0.13)+w*i+w,(int)(h+height-textY)+(int)(height*0.5f));

				}
			}
			
//			canvas.drawBitmap(backImg,0,0, null);
			canvas.drawBitmap(backImg,mMatrix, null);
			
			for(int i=0;i<counter.getLength();i++){
				srcs[i] = new Rect(0, height *10 - height * myCounts[i], drumOneWidth, height *10 -height * myCounts[i] +height);
			}
			
			
			for(int i=0;i<counter.getLength();i++){
				//				canvas.drawBitmap(drums[i], i*w,(-height * 10) + height*0 + h - textY, null);
				//Log.d(TAG,""+myCounts[i]);
				//canvas.drawBitmap(drums[i], i*drumOneWidth, (-height * 9) + height*myCounts[i] - textY , null);
				
				canvas.drawBitmap(drums, srcs[i], dsts[i], null);
			}
		}finally{
			if(canvas != null)
				holder.unlockCanvasAndPost(canvas);
		}

		isCreate = true;
		setCount();
	}

	//pausedでも呼ばれる
	@Override
	public void surfaceDestroyed(SurfaceHolder holder) {
		Log.d(TAG,"destroy!");
		isDestroy = true;
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

		boolean countMath=false;
		int lastCount=0;


		countMath = true;

		//桁の先頭から取りに行く
		for(int i=0;i<counter.getLength()-1;i++){
			//カウンタから取得
			int countDigit = counter.getDigits(i);
			//Viewのカウンタ取得
			int viewDigit =  (height * 10 - srcs[i].top) / height;
			
			//10はあり得るか？
			if(viewDigit == 10)
				viewDigit = 0;

			if(viewDigit != countDigit){

				countMath = false;

				//countとviewの差を求める
				updown[i] = viewDigit < countDigit?1:-1;

				//9から10or0から9の例外処理
				if(viewDigit == 9 && countDigit == 0){
					updown[i] = -updown[i];
					//srcs[i].set(0, height * 10, drumOneWidth, height * 10 +height);
				}
				else if(viewDigit == 0 && countDigit == 9){
					srcs[i].set(0, 0, drumOneWidth, height);
					updown[i] = -updown[i];
				}
			}else
				updown[i] = 0;
		}

		//末尾
		lastCount = counter.getPool();
		//Log.d(TAG+"1",""+lastCount);
		if(lastCount != 0){

			countMath = false;

			int myDigit =  (height * 10 - srcs[4].top) / height;
			//Log.d(TAG+"1",""+srcs[4].top);
			int _d = counter.getDigits(4);
			if(myDigit != _d){
				countMath = false;
				
				//lastCount = myDigit < _d?1:-1;

//				if(myDigit == 9 && _d == 0)
//					lastCount = 1;
//				if(myDigit == 0 && _d == 9){
//					lastCount = -1;
//					srcs[4].set(0, 0 , 
//							drumOneWidth, height);
//
//				}
			}
		}

		if(lastCount == 0){
			//Log.d(TAG+"2",""+srcs[4].top);
			int myDigit =  (height * 10 - srcs[4].top) /height;
			int _d = counter.getDigits(4);
			if(myDigit != _d){
				countMath = false;
				lastCount = myDigit < _d?1:-1;

//				if(myDigit == 9 && _d == 0){
//					lastCount = 1;
//
//				}
//				if(myDigit == 0 && _d == 9){
//					lastCount = -1;
//
//				}
			}
		}
		updown[4] = lastCount;
		if(!countMath && !isDestroy){
			animationLoop();
			animeExe.execute(this);
		}
		
		Log.d("counter:myCount",""+counter.getCount()+":"+myCount);
	}

	private void animationLoop(){

		int moveCount=0;

		//1ループ
		while(moveCount < height && !isDestroy){

			Canvas canvas = null;
			canvas = mHolder.lockCanvas();

			if(canvas != null){
				canvas.drawColor(Color.DKGRAY);
				moveCount += velocity;
				
				for(int i=0;i<counter.getLength();i++){

					//先頭～
					if(updown[i] != 0){
						
						if(srcs[i].top+velocity * -updown[i] < 0){
							srcs[i].set(0,height * 10,drumOneWidth,height * 10 + height);
						}
						if(srcs[i].top+velocity * -updown[i] > height * 10){
							srcs[i].set(0,0,drumOneWidth,height);
						}
						
						//チョットづつずらす
						srcs[i].offset(0, velocity * -updown[i]);
						h[i] += velocity;

						if(moveCount >= height){

							myCounts[i] += updown[i];
							if(myCounts[i] > 9)
								myCounts[i] = 0;
							if(myCounts[i] < 0)
								myCounts[i] = 9;

							srcs[i].set(0, height*10-myCounts[i] * height, 
									drumOneWidth, height*10-myCounts[i] * height+height);						
							//Log.d(TAG+"3",""+srcs[4].top);
						}
					}
					canvas.drawBitmap(drums, srcs[i], dsts[i], null);
				}
//				canvas.drawBitmap(backImg,0,0, null);.
				canvas.drawBitmap(backImg,mMatrix, null);
				mHolder.unlockCanvasAndPost(canvas);
			}
		}
	}
}
