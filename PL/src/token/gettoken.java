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
        String aLine;
        String[] token;
        while(this.scan.hasNext()){
            aLine = this.scan.nextLine();
            token = aLine.split(" ");
            for(String S:token)
                System.out.println(S);
            if(aLine.compareTo(";") == 0){
                if(this.scan.hasNext()){
                    this.scan.nextLine();
                } else {
                    return null;
                }
            } else {
                if(aLine.compareTo("EOF") == 0)
                    System.exit(0);
                return aLine;
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
