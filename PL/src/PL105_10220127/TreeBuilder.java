package PL105_10220127;

import java.util.ArrayList;

public class TreeBuilder {
  public TreeBuilder() { }
  
  public ConsNode TreeConStruct( ConsNode head, ArrayList<Token> tokens, GetToken Getter ) throws ErrorMessageException {
    ConsNode transTyper;
    this.ReadSexp( tokens, Getter );
    if ( tokens.get( 0 ).GetData().matches( "[(]" ) ) {
      this.ReadSexp( tokens, Getter, 2 );
      if ( tokens.get( 1 ).GetData().matches( "[)]" ) ) {
        if ( head == null ) {
          head =  new AtomNode( tokens.get( 0 ).GetColumn() );
          tokens.remove( 0 );
          tokens.remove( 0 );
        } // if
        else {
          transTyper = new AtomNode( tokens.get( 0 ).GetColumn() );
          head.SetLeft( transTyper );
          tokens.remove( 0 );
          tokens.remove( 0 );
          head.SetRight( TreeConStruct( new ConsNode(), tokens, Getter ) );
        } // else
      } // if
      else {
        tokens.remove( 0 );
        if ( head == null ) {
          head = new ConsNode();
          ConsNode c = null;
          head.SetLeft( TreeConStruct( c, tokens, Getter ) );
        } // if
        else
          head.SetLeft( TreeConStruct( new ConsNode(), tokens, Getter ) );
        head.SetRight( TreeConStruct( new ConsNode(), tokens, Getter ) );
      } // else
    } // if
    else if ( tokens.get( 0 ).GetData().matches( "[)]" ) ) {
      // transTyper = new AtomNode( tokens.get( 0 ).GetColumn() );
      // head.SetRight( transTyper );
      head =  new AtomNode( tokens.get( 0 ).GetColumn() );
      tokens.remove( 0 );
    } // else if
    else {
      Token realToken = tokens.get( 0 );
      if ( head == null ) {
        if ( realToken.GetData().matches( "nil" ) || realToken.GetData().matches( "#f" ) )
          head = new AtomNode( tokens.get( 0 ).GetColumn() );
        else {
          if ( realToken.GetData().matches( "^[+-]?\\d+$" ) )
            realToken.SetData( String.format( "%.0f", Float.valueOf( realToken.GetData() ) ) );
          else if ( realToken.GetData().matches( "^[+-]?(((0-9)*\\.[0-9]+)|([0-9]+\\.[0-9]*))$" ) )
            realToken.SetData( String.format( "%.3f", Float.valueOf( realToken.GetData() ) ) );
          else if ( realToken.GetData().matches( "t" ) || realToken.GetData().matches( "#t" ) )
            realToken.SetData( "#t" );
          head = new AtomNode( realToken );
        } // else
        tokens.remove( 0 );
      } // if
      else {
        ConsNode c = null;
        if ( tokens.get( 0 ).GetData().matches( "[\\.]" ) ) {
          tokens.remove( 0 );
          head = TreeConStruct( c, tokens, Getter );
          this.ReadSexp( tokens, Getter );
          tokens.remove( 0 );
        } // if
        else {
          head.SetLeft( TreeConStruct( c, tokens, Getter ) );
          head.SetRight( TreeConStruct( new ConsNode(), tokens, Getter ) );
        } // else
      } // else 
    } // else
    
    return head;
  } // TreeConStruct()
  
  public void ReadSexp( ArrayList<Token> tokens, GetToken Getter ) throws ErrorMessageException {
    if ( tokens.isEmpty() ) {
      Getter.CutToken();
      if ( !Getter.IsEmpty() ) {
        ArrayList<Token> a = Getter.GetList();
        for ( int i = 0; i < a.size() ; i++ )
          tokens.add( a.get( i ) );
      } // if
      else
        throw new ErrorMessageException( "EOF" );
    } // if
  } // ReadSexp()
  
  public void ReadSexp( ArrayList<Token> tokens, GetToken Getter, int tokenssize ) throws ErrorMessageException {
    if ( tokens.size() < tokenssize ) {
      Getter.CutToken();
      if ( !Getter.IsEmpty() ) {
        ArrayList<Token> a = Getter.GetList();
        for ( int i = 0; i < a.size() ; i++ )
          tokens.add( a.get( i ) );
      } // if
      else
        throw new ErrorMessageException( "EOF" );
    } // if
  } // ReadSexp()
  
  public void TreeTravel( ConsNode head, int column, boolean aligned ) {
    if ( head == null ) { }
    else if ( head.IsAtomNode() ) {
      if ( !aligned ) {
        for ( int i = 0; i < column ; i++ )
          System.out.print( "  " );
      } // if
      
      System.out.println( head.ToString() );
    } // else if
    else {
      if ( head.GetLeft() != null && !head.GetLeft().IsAtomNode() ) {
        if ( !aligned ) {
          for ( int i = 0; i < column ; i++ )
            System.out.print( "  " );
        } // if
        
        System.out.print( "( " );
        TreeTravel( head.GetLeft(), column + 1, true );
      } // if
      else
        TreeTravel( head.GetLeft(), column, aligned ); // aligned will be true in initial time
      if ( head.GetLeft() != null && !head.GetLeft().IsAtomNode() ) {
        for ( int i = 0; i < column ; i++ )
          System.out.print( "  " );
        System.out.println( ")" );
      } // if
      
      if ( head.GetRight() != null && head.GetRight().IsAtomNode() ) {
        AtomNode atom = ( AtomNode ) head.GetRight();
        if ( !atom.IsNil() ) {
          for ( int i = 0; i < column ; i++ )
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
