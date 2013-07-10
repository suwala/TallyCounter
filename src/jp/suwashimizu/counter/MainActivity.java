package jp.suwashimizu.counter;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import net.nend.android.NendAdView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.app.ActionBar.LayoutParams;
import com.actionbarsherlock.app.ActionBar.Tab;
import com.actionbarsherlock.app.ActionBar.TabListener;
import com.actionbarsherlock.app.SherlockActivity;
import com.actionbarsherlock.internal.app.ActionBarImpl;
import com.actionbarsherlock.internal.app.ActionBarWrapper;
import com.actionbarsherlock.internal.app.ActionBarWrapper.TabWrapper;
import com.actionbarsherlock.view.MenuItem;
import com.actionbarsherlock.widget.SearchView;
import com.google.ads.AdRequest;
import com.google.ads.AdSize;
import com.google.ads.AdView;

import android.os.Bundle;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.graphics.drawable.Drawable;
import android.support.v4.app.FragmentTransaction;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.SurfaceView;
import android.view.View;
import android.view.View.OnLongClickListener;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends SherlockActivity implements TabListener{

	private CounterView mView;
	private CounterDrum mCounter;
	private static final String TAG="COUNTER";
	private ArrayList<Integer>[] countMemorys;
	private int[] counts;
	private EditText et;
	private Tab selectTab;
	private String[] labels;

	public static final int TAB_LIMIT = 3;

	private AdView adView;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setTheme(R.style.Theme_Sherlock_Light);
		setContentView(R.layout.activity_main);

		//広告領域
		LinearLayout ads = (LinearLayout)findViewById(R.id.ads);

		//国別で分ける
		if(Locale.getDefault().equals(Locale.JAPAN)){
			NendAdView nendView = new NendAdView(getApplicationContext(),71395,"d819dcf91bc6b9ad71706e4ac134bfc7fc4d6c5f");
			ads.addView(nendView,new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT));
		}else{
			adView = new AdView(this, AdSize.BANNER,"a151dd61eeda73c");
			ads.addView(adView);
			adView.loadAd(new AdRequest());
		}
		//

		countMemorys = new ArrayList[TAB_LIMIT];
		for(int i=0;i<TAB_LIMIT;i++){
			countMemorys[i] = new ArrayList<Integer>();

		}
		counts = new int[TAB_LIMIT];
		labels = new String[TAB_LIMIT];

		ActionBar actionBar = getSupportActionBar();
		//Tabの表示
		actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);

		mView = (CounterView)findViewById(R.id.counterView1);
		mCounter = new CounterDrum();
		mView.setCounter(mCounter);

		Button addBtn = (Button)findViewById(R.id.button1);
		addBtn.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				mCounter.add();
				mView.setCount();
			}
		});

		Button subBtn = (Button)findViewById(R.id.button2);
		subBtn.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				mCounter.sub();
				mView.setCount();
			}
		});

		Button memoryBtn = (Button)findViewById(R.id.button3);
		memoryBtn.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				countMemorys[selectTab.getPosition()].add(mCounter.getCount());
				Log.d(TAG,""+countMemorys[selectTab.getPosition()].size());
			}
		});

		Button resetBtn = (Button)findViewById(R.id.button4);
		resetBtn.setOnLongClickListener(new View.OnLongClickListener() {

			@Override
			public boolean onLongClick(View v) {
				mCounter.setCount(0);
				mView.setCount();
				return true;
			}
		});

		Button clearBtn = (Button)findViewById(R.id.button5);
		clearBtn.setOnLongClickListener(new View.OnLongClickListener() {

			@Override
			public boolean onLongClick(View v) {
				mCounter.setCount(0);
				mView.setCount();
				countMemorys[selectTab.getPosition()].clear();
				return true;
			}
		});

		//TabをLIMIT分追加
		for(int i=0;i<TAB_LIMIT;i++){
			String label = readLabelPref(i);
			labels[i] = label;
			actionBar.addTab(actionBar.newTab().setText(label).setTabListener(this));
		}

		readCount();
		readCountMemory();

	}

	@Override
	public boolean onCreateOptionsMenu(com.actionbarsherlock.view.Menu menu) {
		getSupportMenuInflater().inflate(R.menu.main_menu,menu);

		MenuItem item = menu.findItem(R.id.tab_edit);
		View v = item.getActionView();

		et = (EditText)v.findViewById(R.id.editText1);
		et.setFocusable(true);
		et.setFocusableInTouchMode(true);
		et.setText(selectTab.getText().toString());
		et.setOnKeyListener(new View.OnKeyListener() {

			@Override
			public boolean onKey(View v, int keyCode, KeyEvent event) {

				if(event.getAction() == KeyEvent.ACTION_UP && keyCode == KeyEvent.KEYCODE_ENTER){
					Log.d(TAG,"enter!!!!");
					InputMethodManager inputMethodManager = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
					inputMethodManager.hideSoftInputFromWindow(v.getWindowToken(), 0);
					String label = et.getText().toString();
					selectTab.setText(label);
					labels[selectTab.getPosition()] = label;
					et.clearFocus();

					et.setFocusable(false);
					return true;
				}

				return false;
			}
		});
		et.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				v.requestFocus();
				et.setFocusable(true);
				et.setFocusableInTouchMode(true);
			}
		});

		return super.onCreateOptionsMenu(menu);
	}

	@Override
	protected void onResume(){
		super.onResume();
		if(et != null){
			et.clearFocus();
			et.setFocusable(false);
		}
	}

	@Override
	protected void onPause() {
		super.onPause();
		writePrefLables();
		writeCountMemory();
	}

	@Override
	public void onDestroy(){
		adView.destroy();
		super.onDestroy();
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {

		switch (item.getItemId()) {
		case R.id.setting:

			Intent intent = new Intent(MainActivity.this,MemoryListActivity.class);
			int total = 0;
			for(int i=0;i<countMemorys.length;i++){
				intent.putExtra(getString(R.string.intent_list)+i, countMemorys[i]);
				intent.putExtra(getString(R.string.intent_label)+i, labels[i]);
				total += countMemorys[i].size();
			}

			intent.putExtra(getString(R.string.intent_total), total);
			startActivity(intent);
			break;
		}
		return super.onOptionsItemSelected(item);
	}

	private static final String PREF = "TallyCounter";
	private static final String LABEL = "lable_";
	private static final String DEFAULT = "No.";
	/*
	private void readPref(int index){
		SharedPreferences pref = getSharedPreferences(PREF, MODE_PRIVATE);
		int prefCount = (pref.getInt(String.valueOf(index), 0));
		mCounter.setCount(prefCount);
		lastCount = prefCount;
	}
	 */
	private String readLabelPref(int index){
		SharedPreferences pref = getSharedPreferences(PREF, MODE_PRIVATE);
		String label = pref.getString(LABEL+index, DEFAULT+(index+1));
		return label;
	}
	/*
	private void writePref(int index){
		SharedPreferences pref = getSharedPreferences(PREF, MODE_PRIVATE);
		int count = mCounter.getCount();
		Editor edit = pref.edit();
		edit.putInt(String.valueOf(index),count).commit();
	}
	 */

	private void writePrefLables(){
		SharedPreferences pref = getSharedPreferences(PREF, MODE_PRIVATE);
		Editor edit = pref.edit();
		//		edit.putString(LABEL+selectTab.getPosition(), label).commit();
		/*
		ActionBar actionBar = getSupportActionBar();
		for(int i=0;i<actionBar.getTabCount();i++){
			String label = actionBar.getTabAt(i).getText().toString();
			edit.putString(LABEL+i, label);
		}
		 */
		for(int i=0;i<TAB_LIMIT;i++){
			String label = labels[i];
			edit.putString(LABEL+i, label);
		}

		edit.commit();

	}


	/*jsonにエンコードしてPrefにセット 個々のラストカウントもセット
	 * [index,count.....]な方式で収容
	 * prefへのKeyは"index_m"
	 */
	private static final String M = "_m";
	private void writeCountMemory(){

		SharedPreferences pref = getSharedPreferences(PREF, MODE_PRIVATE);
		Editor edit = pref.edit();

		for(int i=0;i<TAB_LIMIT;i++){
			JSONArray jArray = new JSONArray();
			jArray.put(i);
			for(int c:countMemorys[i]){
				jArray.put(c);
			}

			edit.putString(String.valueOf(i)+M,jArray.toString());
			Log.d(TAG+"M_WRITE="+i,jArray.toString());

			int count = counts[i];
			edit.putInt(String.valueOf(i),count);
		}

		edit.commit();

		/*
		JSONArray jArray = new JSONArray();
		jArray.put(index);
		for(int i:countMemorys){
			jArray.put(i);
		}

		Log.d(TAG+"M_WRITE",jArray.toString());

		SharedPreferences pref = getSharedPreferences(PREF, MODE_PRIVATE);
		Editor edit = pref.edit();
		edit.putString(String.valueOf(index)+M,jArray.toString()).commit();
		 */

	}

	private void readCount(){
		SharedPreferences pref = getSharedPreferences(PREF, MODE_PRIVATE);
		for(int i=0;i<TAB_LIMIT;i++){
			int prefCount = (pref.getInt(String.valueOf(i), 0));
			counts[i] = prefCount;
		}
		//		mCounter.setCount(prefCount);
		//		lastCount = prefCount;
	}

	//jsonからデコードしてListにセット
	private void readCountMemory(){
		SharedPreferences pref = getSharedPreferences(PREF, MODE_PRIVATE);

		for(int i=0;i<TAB_LIMIT;i++){
			String memory = pref.getString(i+M, null);
			if(memory != null){

				try {
					JSONArray jArray = new JSONArray(memory);
					for(int j=1;j<jArray.length();j++){
						countMemorys[i].add((Integer) jArray.get(j));
					}

					Log.d(TAG+"M_READ_"+i,jArray.toString());
				} catch (JSONException e) {
					e.printStackTrace();
				}

			}
			/*
		String memory = pref.getString(index+M, null);
		if(memory != null){

			try {
				JSONArray jArray = new JSONArray(memory);
				for(int i=1;i<jArray.length();i++){
					countMemorys.add((Integer) jArray.get(i));
				}

				Log.d(TAG+"M_READ",jArray.toString());
			} catch (JSONException e) {
				e.printStackTrace();
			}

		}
			 */
			int prefCount = (pref.getInt(String.valueOf(selectTab.getPosition()), 0));
			mCounter.setCount(prefCount);
			lastCount = prefCount;
		}
	}

	private int lastCount;

	@Override
	public void onTabSelected(Tab tab, FragmentTransaction ft) {

		selectTab = tab;

		int index = tab.getPosition();
		//		readPref(counts[index]);
		mCounter.setCount(counts[index]);

		if(et != null)
			et.setText(tab.getText().toString());


		//Drawより先に呼ばれるのでフラグ
		if(mView.isCreate){

			mView.setCount();
		}
	}

	@Override
	public void onTabUnselected(Tab tab, FragmentTransaction ft) {

		/*
		//カウンタが変動していたら
		if(lastCount != mCounter.getCount()){
			int index = tab.getPosition();
			writePref(index);
			if(countMemorys.size() > 0)
				writeCountMemory(index);
		}
		//メモリのクリア
		countMemorys.clear();
		 */

		int index = tab.getPosition();
		counts[index] = mCounter.getCount();
	}

	@Override
	public void onTabReselected(Tab tab, FragmentTransaction ft) {

	}
}
