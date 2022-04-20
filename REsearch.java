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
			//lecture test example
			char[] t = {'_','a','b','_','a','a','_','a','b'};
			int[] n1 = {0,3,6,2,6,6,5,8,0};
			int[] n2 = {0,3,6,4,6,6,7,8,0};
			
			boolean[] visted = {true,false,false,false,false,false,false,false,false};
			//creates a new deque
			Deque deque = new Deque();
			String[] words;
			
			words = s.split("");
			int mark=0;
			int point=0;
			for(int i = 1; i<=t.length;i++){
			   while(visted[i] == false){
			   
			   	if(deque.curr == null)
				  deque.push(i);
				if(words[point] != String.valueOf(t[i])){
			          deque.pop();
			          mark++;
			          point++;
			          
			        }
			        else{
			          deque.push(n1[i]);
			          point++;
			        }
			   
			   }
				
			}
			
			
		}
		catch(Exception e){
			System.err.println("Error");
		}
	
	}


}

class Deque{
	public class Node{
		int value;
		
		Node link;
	}
	public Node curr = null;
	public Node next = null;
	
	public void push(String s){
		Node node = new Node();
		
		node.value = s;
		node.link = curr;
		if(curr != null)
		  next = node; 
		else
	  	  curr = node;
		
		
	}
	public void pop(){
		curr = curr.link;
		
	}
}
