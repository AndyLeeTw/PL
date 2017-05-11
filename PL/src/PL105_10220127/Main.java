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
        //head = tb.Eval( head, true );
        getter.ResetColumn();
        if ( getter.IsEmpty() )
          getter.SetLine( 0 );
        else
          getter.SetLine( 1 );
        if ( !head.IsAtomNode() ) {
          if ( head.GetLeft().IsAtomNode() && head.GetRight().IsAtomNode() ) {
            AtomNode transTyperL = ( AtomNode ) head.GetLeft();
            AtomNode transTyperR = ( AtomNode ) head.GetRight();
            if ( transTyperL.GetAtom().GetData().matches( "exit" ) && transTyperR.IsNil() )
              throw new ErrorMessageException( "EOFT" ) ;
          } // if
        } // if
        System.out.print( "\n> " );
        tb.TreeTravel( head , 0, false );
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
          else if ( e.GetErrorCode().matches( "UTL" ) ) {
            System.out.println( "ERROR (unexpected token) : atom or '(' expected when token at Line " +
                                e.GetLine() + " Column " + e.GetColumn() + " is >>" + e.GetAtom() + "<<" );
          } // else if
          else if ( e.GetErrorCode().matches( "UTR" ) ) {
            System.out.println( "ERROR (unexpected token) : ')' expected when token at Line " +
                                e.GetLine() + " Column " + e.GetColumn() + " is >>" + e.GetAtom() + "<<" );
          } // else if
          else if ( e.GetErrorCode().matches( "US" ) ) {
            System.out.println( "ERROR (unbound symbol) : " + e.GetAtom() );
          } // if
          getter.Clear();
          getter.SetLine( 0 );
          tokens.clear();
        } // else
      } // catch
      //catch ( SystemMessageException e ) {
        //System.out.println( e.GetSystemMessage() );
      //} // catch
    } // while
    
    System.out.println( "\nThanks for using OurScheme!" );
  } // main()
} // class Main
