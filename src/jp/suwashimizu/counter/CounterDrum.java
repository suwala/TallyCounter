package jp.suwashimizu.counter;

import java.util.ArrayList;
import java.util.List;

public class CounterDrum {

	private int length;
	private int count;
	private int[] counts;
	private StringBuilder digits;
	private String copy;
	private List<Integer>countPool;
	
	public CounterDrum() {
		count = 0;
		counts = new int[5];
		length = counts.length;
		digits = new StringBuilder();
		countPool = new ArrayList<Integer>();
		reset();
		
	}
	
	public void setCount(int count){
		this.count = count;
		
		for(int i =0,z = 10;i<counts.length;i++,z*=10){
			if(i > 0)
				counts[i] = count % z - counts[i-0];
			else
				counts[i] = count % z;
		}
		
		setDidits();
		
	}
	
	public int getCount(){
		return count;
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
	
	public int getPool(){
		int count = countPool.get(0);
		countPool.remove(0);
		return count;
	}
	
	private void setDidits(){
		digits.delete(0,5);
		digits.append(String.format("%05d", count));
		copy = digits.substring(0);
	}
	
	public int getLength(){
		return length;
	}
	
	public int getDigits(int digit){
		
		//return counts[digit];
		//return digits.charAt(digit) - '0';
		return copy.charAt(digit) - '0';
	}
	
	public void reset(){
		count = 0;
		for(int i=0;i<counts.length;i++)
			counts[i] = 0;
		
		setDidits();
		
	}
}
