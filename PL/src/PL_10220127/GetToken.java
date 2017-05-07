package PL_10220127;

import java.util.ArrayList;
import java.util.Scanner;

public class GetToken {
  private int mcolumn;
  private int mline;
  private Scanner mscan;
  private ArrayList<Token> mALToken;
  public GetToken() {
    this.mcolumn = 0;
    this.mline = 0;
    this.mALToken = new ArrayList<Token>();
    this.mscan = new Scanner( System.in );
  } // GetToken()
  
  public void CutToken() {
    String aLine;
    if ( this.mscan.hasNextLine() ) {
     aLine = this.mscan.nextLine();
    //boolean isString = false;
    // \".*[^\\\\\\\\]+(\\\\.)*\"|\".*[^\\\\\\\\]+(\\\\.)*\\\\\\\\+[^\\\\\\\\]+\"|\"(\\\\.)*\"
    /*String [] ss = aLine.split( " " );
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
    
    for ( int i = 0; i < ss.length && !ss[i].startsWith( ";" ) ; i++ ) {
      this.mcolumn++;
      if ( ss[i].length() == 0 ) ;
      else {
        if ( ss[i].matches( "^[+-]?\\d+$" ) ) {
          mALToken.add( new Token( String.format( "%.0f", Float.valueOf( ss[i] ) ), this.mcolumn ) );
        } // if
        else if ( ss[i].matches( "^[+-]?(((0-9)*\\.[0-9]+)|([0-9]+\\.[0-9]*))$" ) ) {
          mALToken.add( new Token( String.format( "%.3f", Float.valueOf( ss[i] ) ), this.mcolumn ) );
        } // else if
        else {
          ss[i] = ss[i].replaceAll( "(?!')\\\\n(?!')", "\n" );
          ss[i] = ss[i].replaceAll( "(?!')\\\\\"(?!')", "\"" );
          ss[i] = ss[i].replaceAll( "(?!')\\\\\\\\(?!')", "\\\\" );
          mALToken.add( new Token( ss[i], this.mcolumn ) );
        } // else
        
        this.mcolumn += ss[i].length();
      } // else
    } // for
    */
     
     this.mcolumn = 0;
    }
    else
      this.mALToken.clear();
  } // CutToken()
  
  public boolean isEmpty() {
    return this.mALToken.isEmpty();
  } // isEmpty()
  
  public ArrayList<Token> getToken() {
    ArrayList<Token> taken = new ArrayList<Token>();
    taken.addAll( this.mALToken );
    this.mALToken.clear();
    return taken;
  } // getToken()
} // class GetToken
