package PL_10220127;

import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class Main {
    public static void main( String []args ) {
    int lineNum = 1;
    ArrayList<Token> tokens = new ArrayList<Token>();
    TreeBuilder tb = new TreeBuilder();
    ConsNode head = null;
    Scanner scan = new Scanner( System.in );
    GetToken g = new GetToken();
    System.out.println( "Welcome to OurScheme!" );
    System.out.print( "\n> " );
    while( scan.hasNext() ) {
        tokens = g.CutToken( scan.nextLine() );
        /*
        for( Token S:Tokens ){
            if( S.getData(  ).compareTo( "( exit )" ) == 0 ){
                System.out.println( "\nThanks for using OurScheme!" );
                System.exit( 0 );
            }
            System.out.println( S.getData(  ) );
        }
        */
        head = tb.TreeContruct( head, tokens );
        tb.TreeTravel( head );
        lineNum++;
        System.out.print( "> " );
        head = null;
    }
    System.out.println( "\nThanks for using OurScheme!" );
    }
}
