package cuttoken;

import java.util.Scanner;
import java.util.ArrayList;
import java.util.Arrays;
import datatype.Token;

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
        ArrayList<String> cuttenData = new ArrayList<String>();
        ArrayList<Token> ALToken = new ArrayList<Token>();
        while(this.scan.hasNext()){
            if(cuttenData.isEmpty()){
                aLine = this.scan.nextLine();
                this.line++;
                cuttenData.addAll(Arrays.asList(aLine.split(" ")));
            }
            for(String S: cuttenData){
                this.column++;
                if(S.compareTo(";") == 0){
                    commitIndex = cuttenData.indexOf(S);
                    cuttenData.removeAll(cuttenData.subList(commitIndex, cuttenData.size() - 1));
                    break;
                } else if(S.length() != 0){
                    ALToken.add(new Token(S, this.column));
                    this.column += S.length();
                }
            }
            cuttenData.clear();
            this.column = 0;
        }
    }
}
