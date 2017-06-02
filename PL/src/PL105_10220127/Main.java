package PL105_10220127;

import java.util.ArrayList;
import java.util.Scanner;
import java.util.LinkedHashMap;

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
        head = tb.Eval( head, true );
        getter.ResetColumn();
        if ( getter.IsEmpty() )
          getter.SetLine( 0 );
        else
          getter.SetLine( 1 );
        
        System.out.print( "\n> " );
        tb.TreeTravel( head, 0, true, false );
      } // try    
      catch ( SystemMessageException e ) {        
        System.out.print( "\n> " );
        if ( e.GetSystemCode().startsWith( "EOF" ) ) {
          if ( e.GetSystemCode().matches( "EOF" ) ) {
            System.out.print( "ERROR (no more input) : END-OF-FILE encountered" );
          } // if
          else if ( e.GetSystemCode().matches( "EOFT" ) ) ;
          
          isend = true;
        } // if
        else if ( e.GetSystemCode().matches( "EOL|UT[LR]" ) ) {
          if ( e.GetSystemCode().matches( "EOL" ) )
            System.out.println( "ERROR (no closing quote) : END-OF-LINE encountered at Line " + 
                                e.GetLine() + " Column " + e.GetColumn() );
          else if ( e.GetSystemCode().matches( "UTL" ) )
            System.out.println( "ERROR (unexpected token) : atom or '(' expected when token at Line " +
                                e.GetLine() + " Column " + e.GetColumn() + " is >>" + e.GetAtom() + "<<" );
          else if ( e.GetSystemCode().matches( "UTR" ) )
            System.out.println( "ERROR (unexpected token) : ')' expected when token at Line " +
                                e.GetLine() + " Column " + e.GetColumn() + " is >>" + e.GetAtom() + "<<" );
          tokens.clear();
          getter.Clear();
          getter.SetLine( 0 );
        } // else if
        else {
          if ( e.GetSystemCode().matches( "EC" ) )
            System.out.println( "environment cleaned" );
          else if ( e.GetSystemCode().matches( "DEFINE" ) )
            System.out.println( e.GetAtom() + " defined" );
          else if ( e.GetSystemCode().matches( "US" ) )
            System.out.println( "ERROR (unbound symbol) : " + e.GetAtom() );
          else if ( e.GetSystemCode().matches( "AtANF" ) )
            System.out.println( "ERROR (attempt to apply non-function) : " + e.GetAtom() );
          else if ( e.GetSystemCode().matches( "INoA" ) )
            System.out.println( "ERROR (incorrect number of arguments) : " + e.GetAtom() );
          else if ( e.GetSystemCode().matches( "IAT" ) ) {
            System.out.print( "ERROR (" + e.GetAtom() + " with incorrect argument type) : " );
            tb.TreeTravel( e.GetHead(), 0, true, false );
          } // else if
          else if ( e.GetSystemCode().matches( "DbZ" ) )
            System.out.println( "ERROR (division by zero) : /" );
          else if ( e.GetSystemCode().matches( "EL" ) )
            System.out.println( "ERROR (level of " + e.GetAtom().toUpperCase() + ")" );
          else if ( e.GetSystemCode().matches( "EDF" ) ) {
            System.out.print( "ERROR (DEFINE format) : " );
            tb.PrintErrorFormat( e.GetHead() );
          } // else if
          else if ( e.GetSystemCode().matches( "NL" ) ) {
            System.out.print( "ERROR (non-list) : " );
            tb.PrintErrorFormat( e.GetHead() );
          } // else if
        } // else
      } // catch
    } // while
    
    System.out.println( "\nThanks for using OurScheme!" );
  } // main()
} // class Main
