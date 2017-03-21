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
  } // GetToken()
  
  public ArrayList<Token> CutToken( String aLineT ) {
    String aLine = aLineT;
    boolean isString = false;
    if ( !this.mcuttenData.isEmpty() ) {
      this.mcuttenData.clear();
      this.mALToken.clear();
    } // if
    
    String [] ss = aLine.split( "[ &&^[\".\"]]" );
    String s = null;
    String realToken = "";
    for ( int i = 0; i < ss.length ; i++ ) {
      s = ss[i];
      if ( s.contains( "\"" ) ) {
        isString = !isString;
        System.out.println(isString);
        if( isString )
          realToken = "";
      } // if
      if ( isString || s.contains( "\"" ) ){
        realToken = realToken.concat( s );
        System.out.println(realToken);
      }
      else if ( !isString )
        mcuttenData.add( s );
      else
        mcuttenData.add( realToken );
    } // for
    
    for ( int i = 0; i < mcuttenData.size() ; i++ ) {
      s = mcuttenData.get( i );
      this.mcolumn++;
      if ( s.compareTo( ";" ) == 0 ) ;
      else if ( s.length() != 0 ) {
        mALToken.add( new Token( s, this.mcolumn ) );
        this.mcolumn += s.length();
      } // else if
    } // for
    
    this.mcolumn = 0;
    return this.mALToken;
  } // CutToken()
} // class GetToken
