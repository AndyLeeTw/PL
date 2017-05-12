package PL105_10220127;

import java.util.Scanner;

public class GetToken {
  private int mcolumn;
  private int mline;
  private String maLine;
  private Scanner mscan;
  
  public GetToken( Scanner scan ) {
    this.mcolumn = 0;
    this.mline = 0;
    this.maLine = "";
    this.mscan = scan;
  } // GetToken()
  
  public Token CutToken() throws SystemMessageException {
    Token aToken = null;
    if ( this.maLine.startsWith( ";" ) || this.maLine.length() == 0 ) {
      if ( this.mscan.hasNextLine() ) {
        this.maLine = this.mscan.nextLine();
        this.mcolumn = 0;
        this.mline++;
      } // if
      else
        throw new SystemMessageException( "EOF" );
    } // if
    
    if ( this.maLine.startsWith( " " ) ) {
      this.maLine = this.maLine.substring( 1 );
      this.mcolumn++;
      aToken = this.CutToken();
    } // if
    else if ( this.maLine.startsWith( ";" ) || this.maLine.length() == 0 ) {
      aToken = this.CutToken();
    } // else if
    else if ( this.maLine.startsWith( "(" ) || this.maLine.startsWith( ")" ) ||
              this.maLine.startsWith( "'" ) ) {
      this.mcolumn++;
      aToken =  new Token( this.maLine.substring( 0, 1 ), this.mline, this.mcolumn );
      this.maLine = this.maLine.substring( 1 );
    } // if
    else if ( this.maLine.startsWith( "\"" ) ) {
      int toIndex = 0;
      for ( int i = 1 ; i < this.maLine.length() ; i++ )
        if ( this.maLine.substring( i ).startsWith( "\\\"" ) )
          i++;
        else if ( this.maLine.substring( i ).startsWith( "\"" ) && toIndex == 0 )
          toIndex = i;
      String realToken = this.maLine.substring( 0, toIndex + 1 );
      if ( toIndex == 0 )
        throw new SystemMessageException( "EOL", this.mline, this.mcolumn + this.maLine.length() + 1 );
      this.mcolumn += realToken.length();
      realToken = realToken.replaceAll( "(?!')\\\\n(?!')", "\n" );
      realToken = realToken.replaceAll( "(?!')\\\\t(?!')", "\t" );
      realToken = realToken.replaceAll( "\\\\\"", "\"" );
      realToken = realToken.replaceAll( "\\\\\\\\", "\\\\" );
      aToken =  new Token( realToken, this.mline, this.mcolumn );
      this.maLine = this.maLine.substring( toIndex + 1 );
    } // else if
    else {
      int tokenToIndex = Integer.MAX_VALUE;
      int tryToIndex;
      String cuttenToken;
      String [] separator = { " ", "(", ")", "\"", ";" , "'" };
      for ( int i = 0; i < separator.length ; i++ ) {
        tryToIndex = this.maLine.indexOf( separator[i] );
        if ( tryToIndex > 0 && tokenToIndex > tryToIndex )
          tokenToIndex = tryToIndex;
      } // for
      
      if ( tokenToIndex == Integer.MAX_VALUE ) {
        cuttenToken = this.maLine.substring( 0 );
        this.maLine = this.maLine.substring( cuttenToken.length() );
      } // if
      else {
        cuttenToken = this.maLine.substring( 0, tokenToIndex );
        this.maLine = this.maLine.substring( tokenToIndex );
      } // else
      
      this.mcolumn += cuttenToken.length();
      aToken =  new Token( cuttenToken, this.mline, this.mcolumn );
    } // else
    
    return aToken;
  } // CutToken()
  
  public void Clear() {
    this.maLine = "";
  } // Clear()
  
  public void SetLine( int line ) {
    this.mline = line;
  } // SetLine()
  
  public void ResetColumn() {
    this.mcolumn = 0;
  } // ResetColumn()
  
  public boolean IsEmpty() {
    return this.maLine.isEmpty() || this.maLine.matches( " *| *;.*" );
  } // IsEmpty()
} // class GetToken
