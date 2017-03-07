package PL_10220127;

import java.util.ArrayList;
import java.util.Scanner;

import datatype.Token;
import tokenProcess.gettoken;

public class Main {
    public static void main(String []args){
        int LineNum = 1;
        ArrayList<Token> Tokens = new ArrayList<Token>();
        Scanner Scan = new Scanner(System.in);
        gettoken G = new gettoken();
        System.out.println("Welcome to OurScheme!");
        System.out.print("\n> ");
        while(Scan.hasNext()){
            Tokens = G.cutToken(Scan.nextLine());
            for(Token S:Tokens){
                if(S.getData().compareTo("(exit)") == 0){
                    System.out.println("\nThanks for using OurScheme!");
                    System.exit(0);
                }
                System.out.println(S.getData());
            }
            LineNum++;
            System.out.print("> ");
        }
        System.out.println("\nThanks for using OurScheme!");
    }
}
