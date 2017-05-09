package PL105_10220127;

import java.util.Scanner;

public class GetToken {
  private int mcolumn;
  private int mline;
  private String maLine;
  private Scanner mscan;
  
  public GetToken( Scanner scan ) {
    this.mcolumn = 0;
    this.mline = 1;
    this.maLine = "";
    this.mscan = scan;
  } // GetToken()
  
  public Token CutToken() throws ErrorMessageException {
    Token aToken = null;
    while ( this.maLine.startsWith( ";" ) || this.maLine.length() == 0 ) {
      if ( this.mscan.hasNextLine() ) {
        this.maLine = this.mscan.nextLine();
        this.mcolumn = 0;
        while ( this.maLine.startsWith( " " ) ) {
          this.maLine = this.maLine.substring( 1 );
          this.mcolumn++;
        } // while
      } // if
      else
        throw new ErrorMessageException( "EOF" );
    } // if
    
    while ( this.maLine.startsWith( " " ) ) {
      this.maLine = this.maLine.substring( 1 );
      this.mcolumn++;
    } // while
    
    if ( this.maLine.startsWith( "(" ) || this.maLine.startsWith( ")" ) || this.maLine.startsWith( "'" ) ) {
      this.mcolumn++;
      aToken =  new Token( this.maLine.substring( 0, 1 ), this.mline, this.mcolumn );
      this.maLine = this.maLine.substring( 1 );
    } // else if
    else if ( this.maLine.startsWith( "\"" ) ) {
      int fromindex;
      if ( this.maLine.contains( "\\\"" ) &&
           this.maLine.indexOf( "\"", 1 ) > this.maLine.indexOf( "\\\"" ) ) {
        fromindex = this.maLine.indexOf( "\\\"" ) + 2;
      } // if
      else
        fromindex = 1;
      if ( this.maLine.indexOf( "\"", fromindex )  == -1 ) {
        throw new ErrorMessageException( "EOL", this.mline, this.mcolumn + this.maLine.length() + 1 );
      } // if
      else {
        String realToken = this.maLine.substring( 0, this.maLine.indexOf( "\"", fromindex ) + 1 );
        this.mcolumn += realToken.length();
        realToken = realToken.replaceAll( "(?!')\\\\n(?!')", "\n" );
        realToken = realToken.replaceAll( "(?!')\\\\t(?!')", "\t" );
        realToken = realToken.replaceAll( "\\\\\"", "\"" );
        realToken = realToken.replaceAll( "\\\\\\\\", "\\\\" );
        aToken =  new Token( realToken, this.mline, this.mcolumn );
        this.maLine = this.maLine.substring( this.maLine.indexOf( "\"", fromindex ) + 1 );
      } // else
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
    
    this.mline++;
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
    return this.maLine.isEmpty();
  } // IsEmpty()
} // class GetToken
