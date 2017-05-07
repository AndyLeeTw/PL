package PL_10220127;

import java.util.ArrayList;
import java.util.Scanner;

public class Main {
  public static void main( String []args ) {
    ArrayList<Token> tokens = new ArrayList<Token>();
    TreeBuilder tb = new TreeBuilder();
    ConsNode head = null;
    Scanner scan = new Scanner( System.in );
    GetToken getter = new GetToken();
    scan.nextLine();
    System.out.println( "Welcome to OurScheme!" );
    //System.out.print( "\n> " );
    do {
      getter.CutToken();
        /*for ( int i = 0; i < tokens.size() ; i++ ) {
          if ( tokens.get( i ).GetData().compareTo( "(exit)" ) == 0 ) {
            System.out.println( "\nThanks for using OurScheme!" );
            System.exit( 0 );
          } // if
        } // for
        head = tb.TreeConStruct( head, tokens, getter );
        int column = 0;
        System.out.print( "\n> " );
        if( !head.IsAtomNode() ) {
          System.out.print( "( " );
          column++;
        }
        tb.TreeTravel( head , column, true );
        if( !head.IsAtomNode() )
          System.out.println( ")" );
      head = null;*/
      
    } while ( !getter.isEmpty() || !tokens.isEmpty() || true ); // while
    
    System.out.println( "\nThanks for using OurScheme!" );
  } // main()
} // class Main
