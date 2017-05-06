package PL_10220127;

import java.util.ArrayList;

public class TreeBuilder {
  public TreeBuilder() { }
  
  public ConsNode TreeConStruct( ConsNode head, ArrayList<Token> TokensT ) {
    ArrayList<Token> tokens = TokensT;
    ConsNode transTyper;
    if ( !tokens.isEmpty() && tokens.get( 0 ).GetData().matches( "^[(].*" ) ) {
      if ( tokens.get( 0 ).GetData().matches( "[(][)]" ) ) {
        if ( head == null )
          head =  new AtomNode( tokens.get( 0 ).GetColumn() );
        else {
          head.SetLeft( new AtomNode( tokens.get( 0 ).GetColumn() ) );
          tokens.remove(0);
          head.SetRight( TreeConStruct( new ConsNode(), tokens ) );
        }
      } // if
      else {
        tokens.get( 0 ).SetData( tokens.get( 0 ).GetData().replaceFirst( "^[(]", "" ) );
        if ( tokens.get( 0 ).GetData().length() == 0 )
          tokens.remove(0);
        if ( head == null ) {
          head = new ConsNode();
          ConsNode c = null;
          head.SetLeft( TreeConStruct( c, tokens ) );
        } // if
        else
          head.SetLeft( TreeConStruct( new ConsNode(), tokens ) );
        head.SetRight( TreeConStruct( new ConsNode(), tokens ) );
      } // else
    } // if
    else if ( tokens.get( 0 ).GetData().compareTo( ")" ) == 0 ) {
      transTyper = new AtomNode( tokens.get( 0 ).GetColumn() );
      head.SetRight( transTyper );
      tokens.remove( 0 );
    } // else if
    else {
      Token realToken = tokens.get(0);
      /*if ( tokens.get( 0 ).GetData().contains( ")" ) ) {
        realToken = new Token( tokens.get( 0 ).GetData().substring( 0, tokens.get( 0 ).GetData().indexOf( ")" ) ) , tokens.get( 0 ).GetColumn() );
        tokens.get(0).SetData( tokens.get( 0 ).GetData().substring( tokens.get( 0 ).GetData().indexOf( ")" ) ) );
        tokens.get(0).SetColumn( tokens.get( 0 ).GetColumn() + tokens.get( 0 ).GetData().indexOf( ")" ) );
      }
      else {
        realToken = tokens.get(0);
        tokens.remove(0);
      }*/
      if ( head == null ) {
        head = new AtomNode( realToken );
        tokens.remove(0);
      } // if
      else {
        if ( tokens.get( 0 ).GetData().matches("[\\.]") ) {
          tokens.remove( 0 );
            ConsNode c = null;
            head = TreeConStruct( c, tokens );
            tokens.remove( 0 );
        }
        else {
          transTyper = new AtomNode( realToken );
          tokens.remove(0);
          head.SetLeft( transTyper );
          head.SetRight( TreeConStruct( new ConsNode(), tokens ) );
        }
      } // else 
    } // else
    
    return head;
  } // TreeConStruct()
  
  public void TreeTravel( ConsNode head, int column, boolean aligned ) {
    if ( head == null ) { }
    else if ( head.IsAtomNode() ) {
      if ( !aligned ) {
        for ( int i = 0; i < column; i++ )
          System.out.print( "  " );
      } // if
      
      System.out.println( head.ToString() );
    } // else if
    else {
      if ( head.GetLeft() != null && !head.GetLeft().IsAtomNode() ) {
        if ( !aligned ) {
          for ( int i = 0; i < column; i++ )
            System.out.print( "  " );
        } // if
        
        System.out.print("( ");
        TreeTravel( head.GetLeft(), column + 1, true );
      } // if
      else
        TreeTravel( head.GetLeft(), column, aligned ); // aligned will be true in initial time 
      if ( head.GetLeft() != null && !head.GetLeft().IsAtomNode() ) {
        for ( int i = 0; i < column; i++ )
          System.out.print( "  " );
        System.out.println( ")" );
      } // if
      if ( head.GetRight() != null && head.GetRight().IsAtomNode() ) {
        AtomNode atom = (AtomNode)head.GetRight();
        if ( !atom.IsNil() ) {
          for ( int i = 0; i < column; i++ )
            System.out.print( "  " );
          System.out.println( "." );
          TreeTravel( head.GetRight(), column, false );
        } // if
      } // if
      else
        TreeTravel( head.GetRight(), column, false );
    } // else
  } // TreeTravel()
} // class TreeBuilder
