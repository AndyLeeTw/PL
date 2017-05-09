package PL105_10220127;

import java.util.ArrayList;
import java.util.Scanner;

public class Main {
  public static void main( String []args ) {
    boolean isend = false;
    ArrayList<Token> tokens = new ArrayList<Token>();
    TreeBuilder tb = new TreeBuilder();
    ConsNode head = null;
    Scanner scan = new Scanner( System.in );
    GetToken getter = new GetToken( scan );
    scan.nextLine();
    System.out.println( "Welcome to OurScheme!" );
    while ( !isend ) {
      head = null;
      try {
        head = tb.TreeConStruct( head, tokens, getter );
        getter.ResetColumn();
        if ( !tokens.isEmpty() )
          getter.SetLine( 2 );
        else
          getter.SetLine( 1 );
        if ( !head.IsAtomNode() ) {
          if ( head.GetLeft().IsAtomNode() && head.GetRight().IsAtomNode() ) {
            AtomNode transTyperL = ( AtomNode ) head.GetLeft();
            AtomNode transTyperR = ( AtomNode ) head.GetRight();
            if ( transTyperL.GetAtom().GetData().matches( "exit" ) && transTyperR.IsNil() )
              throw new ErrorMessageException( "EOFT" ) ;
          } // if
          
          System.out.print( "\n> ( " );
        } // if
        else
          System.out.print( "\n> " );
        tb.TreeTravel( head, 1, true );
        if ( !head.IsAtomNode() )
          System.out.println( ")" );
      } // try
      catch ( ErrorMessageException e ) {
        System.out.print( "\n> " );
        if ( e.GetErrorCode().startsWith( "EOF" ) ) {
          if ( e.GetErrorCode().matches( "EOF" ) ) {
            System.out.print( "ERROR (no more input) : END-OF-FILE encountered" );
          } // if
          else if ( e.GetErrorCode().matches( "EOFT" ) ) ;
          
          isend = true;
        } // if
        else {
          if ( e.GetErrorCode().matches( "EOL" ) ) {
            System.out.println( "ERROR (no closing quote) : END-OF-LINE encountered at Line " + 
                                e.GetLine() + " Column " + e.GetColumn() );
          } // if
          else if ( e.GetErrorCode().matches( "UT" ) ) {
            System.out.println( "ERROR (unexpected token) : atom or '(' expected when token at Line " +
                                e.GetLine() + " Column " + e.GetColumn() + " is >>" + e.GetAtom() + "<<" );
          } // else if
          
          getter.Clear();
          getter.SetLine( 1 );
          tokens.clear();
        } // else
      } // catch
    } // while
    
    System.out.println( "\nThanks for using OurScheme!" );
  } // main()
} // class Main
