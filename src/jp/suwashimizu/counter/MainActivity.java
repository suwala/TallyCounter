package jp.suwashimizu.counter;

import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.app.ActionBar.Tab;
import com.actionbarsherlock.app.ActionBar.TabListener;
import com.actionbarsherlock.app.SherlockActivity;

import android.os.Bundle;
import android.app.Activity;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;
import android.view.Menu;
import android.view.SurfaceView;
import android.view.View;
import android.widget.Button;

public class MainActivity extends SherlockActivity implements TabListener{

	CounterView mView;
	MyCounter counter;
	CounterDrum mCounter;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setTheme(R.style.Theme_Sherlock_Light);
		setContentView(R.layout.activity_main);

		ActionBar actionBar = getSupportActionBar();
		//Tabの表示
		actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);

		mView = (CounterView)findViewById(R.id.counterView1);
		counter = new MyCounter();
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
		
		//Tabの追加
		actionBar.addTab(actionBar.newTab().setText("Tab1").setTabListener(this));
		actionBar.addTab(actionBar.newTab().setText("Tab2").setTabListener(this));		
		actionBar.addTab(actionBar.newTab().setText("Tab2").setTabListener(this));			
	}

	@Override
	public void onTabSelected(Tab tab, FragmentTransaction ft) {
		mCounter.setCount(tab.getPosition()*1000);
		mView.setCount();
	}

	@Override
	public void onTabUnselected(Tab tab, FragmentTransaction ft) {
	}

	@Override
	public void onTabReselected(Tab tab, FragmentTransaction ft) {
	}

}
