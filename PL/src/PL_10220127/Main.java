package PL_10220127;

import java.util.ArrayList;
import java.util.Scanner;

public class Main {
  public static void main( String []args ) {
    int lineNum = 1;
    ArrayList<Token> tokens = new ArrayList<Token>();
    TreeBuilder tb = new TreeBuilder();
    ConsNode head = null;
    Scanner scan = new Scanner( System.in );
    GetToken g = new GetToken();
    scan.nextLine();
    System.out.println( "Welcome to OurScheme!" );
    System.out.print( "\n> " );
    while ( scan.hasNext() ) {
      tokens = g.CutToken( scan.nextLine() );
      
      for ( int i = 0; i < tokens.size() ; i++ ) {
        if ( tokens.get( i ).GetData().compareTo( "(exit)" ) == 0 ) {
          System.out.println( "\nThanks for using OurScheme!" );
          System.exit( 0 );
        } // if
      } // for
      
      if ( !tokens.isEmpty() ) {
        head = tb.TreeConStruct( head, tokens );
        tb.TreeTravel( head );
        System.out.print( "\n> " );
      } // if
      
      lineNum++;
      head = null;
    } // while
    
    System.out.println( "\nThanks for using OurScheme!" );
  } // main()
} // class Main
