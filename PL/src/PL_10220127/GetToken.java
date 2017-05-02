package PL_10220127;

import java.util.ArrayList;

public class GetToken {
  private int mcolumn;
  private ArrayList<Token> mALToken;
  public GetToken() {
    this.mcolumn = 0;
    this.mALToken = new ArrayList<Token>();
  } // GetToken()
  
  public ArrayList<Token> CutToken( String aLineT ) {
    String aLine = aLineT;
    boolean isString = false;
    this.mALToken.clear();
    // \".*[^\\\\\\\\]+(\\\\.)*\"|\".*[^\\\\\\\\]+(\\\\.)*\\\\\\\\+[^\\\\\\\\]+\"|\"(\\\\.)*\"
    String [] ss = aLine.split( " " );
    String realToken = "";
    int concatPosion = -1;
    for ( int i = 0; i < ss.length ; i++ ) {
      if ( ss[i].startsWith( "\"" ) && !isString ) {
        isString = !isString;
        concatPosion = i;
        realToken = "";
      } // if
      
      if ( isString ) {
        realToken = realToken.concat( ss[i] );
        if ( i != concatPosion && ss[i].endsWith( "\"" ) && !ss[i].endsWith( "\\\"" ) ) {
          isString = !isString;
          ss[concatPosion] = realToken;
        } // if
        else
          realToken += " ";
        if ( i != concatPosion )
          ss[i] = "";
      } // if
    } // for
    
    for ( int i = 0; i < ss.length && !ss[i].startsWith(";") ; i++ ) {
      this.mcolumn++;
      if ( ss[i].length() == 0) ;
      else {
        if ( ss[i].matches( "^[+-]?\\d+$" ) ) {
          mALToken.add( new Token( String.format( "%.0f", Float.valueOf( ss[i] ) ), this.mcolumn ) );
        } // if
        else if ( ss[i].matches( "^[+-]?(((0-9)*\\.[0-9]+)|([0-9]+\\.[0-9]*))$" ) ) {
          mALToken.add( new Token( String.format( "%.3f", Float.valueOf( ss[i] ) ), this.mcolumn ) );
        } // else if
        else
          mALToken.add( new Token( ss[i], this.mcolumn ) );
        this.mcolumn += ss[i].length();
      } // else
    } // for
    this.mcolumn = 0;
    return this.mALToken;
  } // CutToken()
} // class GetToken
