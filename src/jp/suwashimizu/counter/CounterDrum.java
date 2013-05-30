package jp.suwashimizu.counter;

import java.util.ArrayList;
import java.util.List;

import android.util.Log;

public class CounterDrum {

	private static final int LENGTH = 5;
	private int count;
	private int[] counts;
	private StringBuilder digits;
	private String copy;
	private List<Integer>countPool;

	public CounterDrum() {
		count = 0;
		counts = new int[LENGTH];
		digits = new StringBuilder();
		countPool = new ArrayList<Integer>();
		reset();

	}

	public void setCount(int count){
		this.countPool.clear();
		
		
		this.count = count;
		
		
		for(int i =0,z = 10;i<counts.length;i++,z*=10){
			if(i > 0)
				counts[i] = count % z - counts[i-0];
			else
				counts[i] = count % z;
		}

		setDidits();
		setLastDigit();

	}

	public int getCount(){
		return count;
	}

	public int[] copy(){
		return counts.clone();
	}
	
	public void add(){
		count++;
		setDidits();
		countPool.add(1);
	}

	public void sub(){
		if(count > 0){
			count--;
			setDidits();
			countPool.add(-1);
		}
	}

	
	//末尾はPoolから取ってるのでSet時にバグる　Poolに追加する？
	public int getPool(){

		if(countPool.size() > 0){
			int count = countPool.get(0);
			countPool.remove(0);
			return count;
		}else
			return 0;

	}

	private void setDidits(){
		digits.delete(0,LENGTH);
		digits.append(String.format("%0"+LENGTH+"d", count));
		copy = digits.substring(0);
	}
	
	private void setLastDigit(){
		
	}

	public int getLength(){
		return LENGTH;
	}

	public int getDigits(int digit){

		//return counts[digit];
		//return digits.charAt(digit) - '0';
		//copy = digits.substring(0);
//		saiki(digit);
		
		int d = LENGTH - digit-1;
		digit = count;
		
		for(int i=0;i<d;i++){
			digit /= 10;
		}
		digit = digit % 10;
		return digit;
		//return copy.charAt(digit) - '0';
	}
	
	private int saiki(int d){
		
		return 0;
	}

	public void reset(){
		count = 0;
		for(int i=0;i<counts.length;i++)
			counts[i] = 0;

		setDidits();

	}
	
	@Override
	public String toString(){
		return String.valueOf(count);
	}
}
