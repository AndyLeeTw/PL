package token;

import java.util.Scanner;
import java.util.ArrayList;
import java.util.Arrays;

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
        String aLine;
        ArrayList<String> token = new ArrayList<String>();
        while(this.scan.hasNext()){
            if(token.isEmpty()){
                aLine = this.scan.nextLine();
                this.line++;
                token.addAll(Arrays.asList(aLine.split(" ")));
            }
        }
        return null;
    }
    public void parser(){
        String beSolved;
        while((beSolved = this.getNext()) != null){
            System.out.println(beSolved + " " + this.line);
        }
    }
}
