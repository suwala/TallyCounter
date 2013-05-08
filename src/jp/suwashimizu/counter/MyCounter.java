package jp.suwashimizu.counter;

public class MyCounter {

	private int count;
	
	public MyCounter() {
		count = 0;
	}
	
	public void add(){
		count++;
	}
	
	public void sub(){
		if(count > 0)
			count--;
	}
	
	public int getCount(){
		return count;
	}
	
	public void setCount(int _count){
		count = _count;
	}
}
