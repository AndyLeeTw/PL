package tokenProcess;

import java.util.ArrayList;
import java.util.Arrays;
import datatype.Token;


public class gettoken {
    private int column;
    private ArrayList<String> cuttenData;
    private ArrayList<Token> ALToken;
    public gettoken(){
        this.column = 0;
        this.cuttenData = new ArrayList<String>();
        this.ALToken = new ArrayList<Token>();
    }
    public ArrayList<Token> cutToken(String _aLine){
        int commitIndex;
        String aLine = _aLine;
        if(!this.cuttenData.isEmpty()){
            this.cuttenData.clear();
            this.ALToken.clear();
        }
        this.cuttenData.addAll(Arrays.asList(aLine.split(" ")));
            for(String S: cuttenData){
                this.column++;
                if(S.compareTo(";") == 0){
                    commitIndex = cuttenData.indexOf(S);
                    this.cuttenData.removeAll(this.cuttenData.subList(commitIndex, cuttenData.size() - 1));
                    break;
                } else if(S.length() != 0){
                    ALToken.add(new Token(S, this.column));
                    this.column += S.length();
                }
            }
        this.column = 0;
        return new ArrayList<Token>(this.ALToken);
    }
}
