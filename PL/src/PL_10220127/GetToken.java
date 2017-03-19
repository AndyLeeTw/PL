package PL_10220127;

import java.util.ArrayList;
import java.util.Arrays;


public class GetToken {
    private int mcolumn;
    private ArrayList<String> mcuttenData;
    private ArrayList<Token> mALToken;
    public GetToken() {
        this.mcolumn = 0;
        this.mcuttenData = new ArrayList<String>();
        this.mALToken = new ArrayList<Token>();
    }
    public ArrayList<Token> CutToken( String aLineT ) {
        int commitIndex;
        String aLine = aLineT;
        if(!this.mcuttenData.isEmpty()) {
            this.mcuttenData.clear();
            this.mALToken.clear();
        }
        for(String T: aLine.split(" "))
            this.mcuttenData.add(T);
        for(String S: mcuttenData) {
            this.mcolumn++;
            if(S.compareTo(";") == 0) {
                commitIndex = mcuttenData.indexOf(S);
                this.mcuttenData.removeAll(this.mcuttenData.subList(commitIndex, mcuttenData.size() - 1));
                break;
            } else if(S.length() != 0) {
                mALToken.add(new Token(S, this.mcolumn));
                this.mcolumn += S.length();
            }
        }
        this.mcolumn = 0;
        return new ArrayList<Token>(this.mALToken);
    }
}
