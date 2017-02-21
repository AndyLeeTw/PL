package token;

import java.util.Scanner;

public class gettoken {
	private int line;
	private int column;
	private Scanner scan;
	public gettoken(){
		this.line = 0;
		this.column = 0;
		this.scan = new Scanner(System.in);
	}
	private String getNext(){
		String S;
		while(this.scan.hasNext()){
			S = this.scan.next();
			if(S.compareTo(";") == 0){
				if(this.scan.hasNext()){
					this.scan.nextLine();
				} else {
					return null;
				}
			} else {
				return S;
			}
		}
		return null;
	}
	public void parser(){
		String beSolved;
		while((beSolved = this.getNext()) != null){
			System.out.println(beSolved);
		}
	}
}
