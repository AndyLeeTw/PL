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
    public void parser(){
        int commitIndex;
        String aLine;
        ArrayList<String> token = new ArrayList<String>();
        while(this.scan.hasNext()){
            if(token.isEmpty()){
                aLine = this.scan.nextLine();
                this.line++;
                token.addAll(Arrays.asList(aLine.split(" ")));
            }
            for(String S: token){
                this.column++;
                if(S.compareTo(";") == 0){
                    commitIndex = token.indexOf(S);
                    token.removeAll(token.subList(commitIndex, token.size() - 1));
                    break;
                } else if(S.length() != 0){
                    this.column += S.length();
                }
            }
            token.clear();
            this.column = 0;
        }
    }
}
