package PL105_10220127;

import java.util.ArrayList;
import java.util.Scanner;

public class GetToken {
  private int mcolumn;
  private int mline;
  private Scanner mscan;
  private ArrayList<Token> mALToken;
  public GetToken( Scanner scan ) {
    this.mcolumn = 0;
    this.mline = 1;
    this.mALToken = new ArrayList<Token>();
    this.mscan = scan;
  } // GetToken()
  
  public void CutToken() throws ErrorMessageException {
    String aLine = null;
    while ( this.IsEmpty() && this.mscan.hasNextLine() ) {
      aLine = this.mscan.nextLine();
      this.mcolumn = 0;
      while ( aLine.length() > 0 && !aLine.startsWith( ";" ) ) {
        if ( aLine.startsWith( " " ) ) {
          aLine = aLine.substring( 1 );
          this.mcolumn++;
        } // if
        else if ( aLine.startsWith( "(" ) || aLine.startsWith( ")" ) || aLine.startsWith( "'" ) ) {
          this.mcolumn++;
          this.mALToken.add( new Token( aLine.substring( 0, 1 ), this.mline, this.mcolumn ) );
          aLine = aLine.substring( 1 );
        } // else if
        else if ( aLine.startsWith( "\"" ) ) {
          int fromindex;
          if ( aLine.contains( "\\\"" ) && aLine.indexOf( "\"", 1 ) > aLine.indexOf( "\\\"" ) ) {
            fromindex = aLine.indexOf( "\\\"" ) + 2;
          } // if
          else
            fromindex = 1;
          if ( aLine.indexOf( "\"", fromindex )  == -1 )
            throw new ErrorMessageException( "EOL", this.mline, this.mcolumn + aLine.length() + 1 );
          else {
            String realToken = aLine.substring( 0, aLine.indexOf( "\"", fromindex ) + 1 );
            this.mcolumn += realToken.length();
            realToken = realToken.replaceAll( "(?!')\\\\n(?!')", "\n" );
            realToken = realToken.replaceAll( "(?!')\\\\t(?!')", "\t" );
            realToken = realToken.replaceAll( "\\\\\"", "\"" );
            realToken = realToken.replaceAll( "\\\\\\\\", "\\\\" );
            this.mALToken.add( new Token( realToken, this.mline, this.mcolumn ) );
            aLine = aLine.substring( aLine.indexOf( "\"", fromindex ) + 1 );
          } // else
        } // else if
        else {
          int tokenToIndex = Integer.MAX_VALUE;
          int tryToIndex;
          String cuttenToken;
          String [] separator = { " ", "(", ")", "\"", ";" , "'" };
          for ( int i = 0; i < separator.length ; i++ ) {
            tryToIndex = aLine.indexOf( separator[i] );
            if ( tryToIndex > 0 && tokenToIndex > tryToIndex )
              tokenToIndex = tryToIndex;
          } // for
          
          if ( tokenToIndex == Integer.MAX_VALUE ) {
            cuttenToken = aLine.substring( 0 );
            aLine = aLine.substring( cuttenToken.length() );
          } // if
          else {
            cuttenToken = aLine.substring( 0, tokenToIndex );
            aLine = aLine.substring( tokenToIndex );
          } // else
          
          this.mcolumn += cuttenToken.length();
          this.mALToken.add( new Token( cuttenToken, this.mline, this.mcolumn ) );
        } // else
      } // while
      
      this.mline++;
    } // while
  } // CutToken()
  
  public void SetLine( int line ) {
    this.mline = line;
  } // SetLine()
  
  public boolean IsEmpty() {
    return this.mALToken.isEmpty();
  } // IsEmpty()
  
  public ArrayList<Token> GetList() {
    ArrayList<Token> taken = new ArrayList<Token>();
    for ( int i = 0; i < this.mALToken.size() ; i++ )
      taken.add( this.mALToken.get( i ) );
    this.mALToken.clear();
    return taken;
  } // GetList()
} // class GetToken
