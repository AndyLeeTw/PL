package PL_10220127;

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
      if ( tokens.isEmpty() ) {
        getter.CutToken();
        ArrayList<Token> a = getter.GetList();
        for ( int i = 0; i < a.size() ; i++ )
          tokens.add( a.get( i ) );
        if ( tokens.size() > 2 && tokens.get( 0 ).GetData().matches( "[(]" ) &&  
            tokens.get( 1 ).GetData().matches( "exit" ) && tokens.get( 2 ).GetData().matches( "[)]" ) )
         isend = true;
      } // if
      
      if ( !isend ) {
        if ( tokens.isEmpty() ) {
          System.out.print( "\n> ERROR (no more input) : END-OF-FILE encountered" );
          isend = true;
        } // if
        else {
          head = null;
          head = tb.TreeConStruct( head, tokens, getter );
          for(Token T: tokens)
            System.out.println(T.GetData());
          int column = 1;
          System.out.print( "\n> " );
          if ( !head.IsAtomNode() ) {
            System.out.print( "( " );
          } // if
        
          tb.TreeTravel( head, column, true );
          if ( !head.IsAtomNode() )
            System.out.println( ")" );
        } // else
      } // if
    } // while
    System.out.println( "\nThanks for using OurScheme!" );
  } // main()
} // class Main
