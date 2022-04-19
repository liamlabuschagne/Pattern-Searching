import java.io.FileReader;
import java.io.BufferedReader;
import java.util.ArrayList;
import java.io.IOException;
import java.io.InputStreamReader;

public class REsearch{
	public static void main(String[] args){
		try{
			//regular expression outputed by REcompile
			BufferedReader exp = new BufferedReader(new InputStreamReader(System.in));
			//text file to check
			BufferedReader text = new BufferedReader(new FileReader(args[0]));
			String s=text.readLine();
			
			//3 arrays to simulate a FSM
			char[] t;
			int[] n1;
			int[] n2;
			//creates a new deque
			Deque deque = new Deque();
			String[] words;
			
			words = s.split("");
			while(s!=null){
				
				
			}
			
			
		}
		catch(Exception e){
			System.err.println("Error");
		}
	
	}


}

class Deque{
	public class Node{
		String value;
		
		Node next;
	}
	public Node head = null;
	public Node tail = null;
	
	public void push(String s){
		Node node = new Node();
		
		node.value = s;
		node.next = head;
		if(head == null)
		  tail = node;
		else
		  head = node;
		
		
	}
	public void pop(){
		head = head.next;
		head.prev = null;
	}
}
