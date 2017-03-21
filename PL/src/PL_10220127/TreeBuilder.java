package PL_10220127;

import java.util.ArrayList;

public class TreeBuilder {
  public TreeBuilder() { }
  
  public ConsNode TreeConStruct( ConsNode head, ArrayList<Token> TokensT ) {
    ArrayList<Token> tokens = TokensT;
    ConsNode transTyper;
    if ( tokens.get( 0 ).GetData().substring( 0, 1 ).compareTo( "(" ) == 0 ) {
      if ( tokens.size() > 1 && tokens.get( 1 ).GetData().compareTo( ")" ) == 0 )
        return new AtomNode( tokens.get( 0 ).GetColumn() );
      else {
        if ( head == null )
          head = new ConsNode();
        tokens.get( 0 ).SetData( tokens.get( 0 ).GetData().replaceFirst( "[(]", "" ) );
        if ( tokens.get( 0 ).GetData().length() == 0 )
            tokens.remove(0);
        ConsNode c = null;
        head.SetLeft( TreeConStruct( c, tokens ) );
        head.SetRight( TreeConStruct( new ConsNode(), tokens ) );
      } // else
    } // if
    else if ( tokens.get( 0 ).GetData().compareTo( "." ) == 0 ) {
      tokens.remove( 0 );
      head = new AtomNode( tokens.get( 0 ) );
    } // else if
    else if ( tokens.get( 0 ).GetData().compareTo( ")" ) == 0 ) {
      transTyper = new AtomNode( tokens.get( 0 ).GetColumn() );
      head.SetRight( transTyper );
      tokens.remove( 0 );
    } // else if
    else {
      if ( head == null ) {
        head = new AtomNode( tokens.get( 0 ) );
        tokens.remove( 0 );
      } // if
      else {
        transTyper = new AtomNode( tokens.get( 0 ) );
        head.SetLeft( transTyper );
        tokens.remove( 0 );
        head.SetRight( TreeConStruct( new ConsNode(), tokens ) );
      } // else 
    } // else
    
    return head;
  } // TreeConStruct()
  
  public void TreeTravel( ConsNode head ) {
    if ( head == null ) { }
    else if ( head.IsAtomNode() ) {
      System.out.println( head.ToString() );
    } // else if
    else {
      TreeTravel( head.GetLeft() );
      TreeTravel( head.GetRight() );
    } // else
  } // TreeTravel()
} // class TreeBuilder
