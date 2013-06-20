package jp.suwashimizu.counter;


import java.util.ArrayList;
import java.util.List;

import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.app.SherlockListActivity;
import com.actionbarsherlock.view.MenuItem;

import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

public class MemoryListActivity extends SherlockListActivity{


	//Listの実装　出力の実装　終わり？

	private MyAdapter mAdapter;

	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.list_activity);

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

		ArrayList<Integer> list = (ArrayList<Integer>)getIntent().getSerializableExtra("list1");
		Integer[] countList = new Integer[list.size()+1];
		int count = 0;
		countList[count++] = -1;
		for(int c:list){
			countList[count++] = c;
		}
		
		list = (ArrayList<Integer>)getIntent().getSerializableExtra("list2");
		
		countList[count++] = -1;
		for(int c:list){
			countList[count++] = c;
		}
		
		list = (ArrayList<Integer>)getIntent().getSerializableExtra("list3");
		countList[count++] = -1;
		for(int c:list){
			countList[count++] = c;
		}
		
		mAdapter = new MyAdapter(this, R.layout.list_item, countList);
		setListAdapter(mAdapter);

	}



	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		if(item.getItemId() == android.R.id.home){
			finish();
			return true;
		}
		return super.onOptionsItemSelected(item);
	}

	private class MyAdapter extends ArrayAdapter<Integer>{

		private Integer[] countList;
		private LayoutInflater mInflater;

		public MyAdapter(Context context, int resource,
				Integer[] objects) {

			super(context, resource, objects);
			this.countList = objects;
			mInflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent){

			final ViewHolder holder;
			if(convertView == null){
				convertView = mInflater.inflate(R.layout.list_item, null);

				TextView count = (TextView)convertView.findViewById(android.R.id.text1);
				//TextView time ;

				holder = new ViewHolder();
				holder.count = count;
				convertView.setTag(holder);
			}else
				holder = (ViewHolder)convertView.getTag();

			int count = countList[position];
			if(!isEnabled(position)){
				holder.count.setText(null);
				convertView.setBackgroundColor(Color.LTGRAY);
			}else{
				holder.count.setText(String.valueOf(count));
				convertView.setBackgroundColor(Color.WHITE);
			}
			return convertView;

		}

		@Override
		public boolean isEnabled(int position) {
			int c = getItem(position);
			Log.d("LIST",""+c);
			return getItem(position) == -1? false:true;
		}
	}

	private static class ViewHolder{
		//		TextView time;
		TextView count;
	}
}
