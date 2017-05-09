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
        System.out.print( "\n> " );
        if ( !head.IsAtomNode() ) {
          if ( head.GetLeft().IsAtomNode() && head.GetRight().IsAtomNode() ) {
            AtomNode transTyperL = ( AtomNode ) head.GetLeft();
            AtomNode transTyperR = ( AtomNode ) head.GetRight();
            if ( transTyperL.GetAtom().GetData().matches( "exit" ) && transTyperR.IsNil() )
              throw new ErrorMessageException( "exit" ) ;
          } // if
          
          System.out.print( "( " );
        } // if
        
        tb.TreeTravel( head, 1, true );
        if ( !head.IsAtomNode() )
          System.out.println( ")" );
      } // try
      catch ( ErrorMessageException e ) {
        if ( e.GetErrorCode().matches( "EOF" ) )
          System.out.print( "\n> ERROR (no more input) : END-OF-FILE encountered" );
        else if ( e.GetErrorCode().matches( "exit" ) ) ;
        isend = true;
      } // catch
    } // while
    
    System.out.println( "\nThanks for using OurScheme!" );
  } // main()
} // class Main
