package jp.suwashimizu.counter;


import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.app.SherlockExpandableListActivity;
import com.actionbarsherlock.app.SherlockListActivity;
import com.actionbarsherlock.view.MenuItem;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Color;
import android.media.MediaScannerConnection;
import android.media.MediaScannerConnection.OnScanCompletedListener;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ExpandableListView;
import android.widget.SimpleExpandableListAdapter;
import android.widget.TextView;
import android.widget.Toast;

public class MemoryListActivity extends SherlockExpandableListActivity{


	//Listの実装　出力の実装　終わり？ 折り畳みにしよう

	private String[] labels;

	private static final String GROUP_TITLE = "GROUP_TITLE";
	private static final String CHILD_VALUES = "CHILD_VALUES";
	
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		//setContentView(R.layout.list_activity);
		setTheme(R.style.Theme_Sherlock_Light);
		ActionBar actionBar = getSupportActionBar();
		actionBar.setDisplayHomeAsUpEnabled(true);

		/*
		ArrayList<Integer>[] lists = (ArrayList<Integer>[]) getIntent().getSerializableExtra("counts");
		int total = 3;
		for(int i=0;i<lists.length;i++){
			total += lists[i].size();
		}

		Integer[] countList = new Integer[total];

		int count = 0;
		for(int i=0;i<lists.length;i++){
			countList[count++] = -1;
			ArrayList<Integer> list = lists[i];
			for(int c:list){
				countList[count++] = c;
			}
		}
		 */
		labels = new String[MainActivity.TAB_LIMIT];
		int total = getIntent().getIntExtra(getString(R.string.intent_total), 0);
		Integer[] countList = new Integer[total+3];
		int count = 0;
		
		for(int i=0;i<MainActivity.TAB_LIMIT;i++){

			countList[count++] = -(i+1);
			ArrayList<Integer> list = (ArrayList<Integer>)getIntent().getSerializableExtra(getString(R.string.intent_list)+i);
			for(int c:list){
				countList[count++] = c;
			}
			
			labels[i] = getIntent().getStringExtra(getString(R.string.intent_label)+i);
		}
		
		
		List<Map<String, String>> gropuList = new ArrayList<Map<String,String>>();
		List<List<Map<String, String>>> childList = new ArrayList<List<Map<String,String>>>();
		
		//親の設定
		for(int i=0;i<MainActivity.TAB_LIMIT;i++){
			Map<String, String> groupElement = new HashMap<String, String>();
			groupElement.put(GROUP_TITLE, labels[i]);
			gropuList.add(groupElement);
			
			ArrayList<Integer> list = (ArrayList<Integer>)getIntent().getSerializableExtra(getString(R.string.intent_list)+i);
			
			//子の設定
			List<Map<String,String>> childElements = new ArrayList<Map<String,String>>();
			Map<String, String> child_text = new HashMap<String, String>();
			child_text.put(CHILD_VALUES,"ファイルに出力");
			childElements.add(child_text);
			
			for(int j=0;j<list.size();j++){
				Map<String, String> child = new HashMap<String, String>();
				child.put(CHILD_VALUES, String.valueOf(list.get(j)));
				childElements.add(child);
			}
			childList.add(childElements);
		}
		
		
		SimpleExpandableListAdapter adapter = new SimpleExpandableListAdapter(
				this, gropuList, android.R.layout.simple_expandable_list_item_1, new String[]{GROUP_TITLE}, new int[]{android.R.id.text1}, childList, 
				R.layout.list_item, new String[]{CHILD_VALUES}, new int[]{android.R.id.text1});
		setListAdapter(adapter);
		getExpandableListView().setOnChildClickListener(new ExpandableListView.OnChildClickListener() {
			
			@Override
			public boolean onChildClick(ExpandableListView parent, View v,
					int groupPosition, int childPosition, long id) {
				//groypPosition で　Group取得 childPosition=0でOutPutと判定？
				if(childPosition == 0)
					outputText(groupPosition,parent);
				return false;
			}
		});
	}
	
	@SuppressLint("NewApi")
	private void outputText(int groupPosition,ExpandableListView lv){
		
		String state = Environment.getExternalStorageState();
		if(Environment.MEDIA_MOUNTED.equals(state)){
			File file = new File(Environment.getExternalStorageDirectory().getPath()+"/"+getString(R.string.save_dir));

			if(!file.exists())
				file.mkdirs();
			

			String date = String.valueOf(System.currentTimeMillis());
			String fileName = file.getAbsolutePath() +"/"+ labels[groupPosition]+date+".text";
			try {
				FileOutputStream out = new FileOutputStream(fileName);
				
				OutputStreamWriter osw = new OutputStreamWriter(out, "UTF-8");
	            BufferedWriter bw = new BufferedWriter(osw);
	            String str = labels[groupPosition];
	            bw.write(str);
	            
	            for(int i=2;i<lv.getChildCount();i++){
			    	HashMap<String, String> map = (HashMap<String, String>)lv.getItemAtPosition(i);
			    	bw.write(","+map.get(CHILD_VALUES));
			    }
	            
	            bw.flush();
	            bw.close();
				

	    		String messa = getString(R.string.toast_save_dir,fileName);
	    		
	    		//scanしないとPCで見たときに反映されない なんか見えないこともあったﾜｶﾗﾝ
	    		MediaScannerConnection.scanFile(getApplicationContext(),new String[]{fileName},null, mScanCompletedListener);
	    		
	    		Toast.makeText(this, messa, Toast.LENGTH_SHORT).show();
	    		
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			} catch (UnsupportedEncodingException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
			
			
		}
		
	}


	
	OnScanCompletedListener mScanCompletedListener = new OnScanCompletedListener() {
	    @Override
	    public void onScanCompleted(String path, Uri uri) {
	        Log.d("MediaScannerConnection", "Scanned " + path + ":");
	        Log.d("MediaScannerConnection", "-> uri=" + uri);
	    }
	};
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		if(item.getItemId() == android.R.id.home){
			finish();
			return true;
		}
		return super.onOptionsItemSelected(item);
	}
}
