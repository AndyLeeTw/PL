package PL105_10220127;

import java.util.ArrayList;

public class TreeBuilder {
  public TreeBuilder() { }
  
  public ConsNode TreeConStruct( ConsNode head, ArrayList<Token> tokens, GetToken Getter )
  throws ErrorMessageException {
    ConsNode transTyper;
    final ConsNode NULL = null;
    this.ReadSexp( tokens, Getter );
    if ( head == null ) {
      if ( tokens.get( 0 ).GetData().matches( "'" ) ) {
        head = new ConsNode();
        tokens.get( 0 ).SetData( "quote" );
        transTyper = new AtomNode( tokens.get( 0 ) );
        tokens.remove( 0 );
        head.SetLeft( transTyper );
        transTyper = new ConsNode();
        head.SetRight( transTyper );
        transTyper.SetLeft( TreeConStruct( NULL, tokens, Getter ) );
        ConsNode tTr = new AtomNode( 0, 0 );
        transTyper.SetRight( tTr );
      } // if
      else if ( tokens.get( 0 ).GetData().matches( "[(]" ) ) {
        tokens.remove( 0 );
        this.ReadSexp( tokens, Getter );
        if ( tokens.get( 0 ).GetData().matches( "[)]" ) ) {
          head =  new AtomNode( tokens.get( 0 ).GetLine(), tokens.get( 0 ).GetColumn() );
          tokens.remove( 0 );
        } // if
        else {
          head = new ConsNode();
          head.SetLeft( TreeConStruct( NULL, tokens, Getter ) );
          head.SetRight( TreeConStruct( new ConsNode(), tokens, Getter ) );
        } // else
      } // else if
      else {
        Token realToken = tokens.get( 0 );
        if ( head == null ) {
          if ( realToken.GetData().matches( "[\\.()]" ) )
            throw new ErrorMessageException( "UTL", realToken.GetData(), realToken.GetLine(),
                                             realToken.GetColumn() );
          else if ( realToken.GetData().matches( "nil" ) || realToken.GetData().matches( "#f" ) )
            head = new AtomNode( tokens.get( 0 ).GetLine(), tokens.get( 0 ).GetColumn() );
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
      } // else
    } // if
    else {
      if ( tokens.get( 0 ).GetData().matches( "[\\.]" ) ) {
        tokens.remove( 0 );
        head = TreeConStruct( NULL, tokens, Getter );
        this.ReadSexp( tokens, Getter );
        if ( tokens.get( 0 ).GetData().matches( "[)]" ) )
          tokens.remove( 0 );
        else
          throw new ErrorMessageException( "UTR", tokens.get( 0 ).GetData(), tokens.get( 0 ).GetLine(),
                                           tokens.get( 0 ).GetColumn() );
      } // if
      else if ( tokens.get( 0 ).GetData().matches( "[)]" ) ) {
        head =  new AtomNode( tokens.get( 0 ).GetLine(), tokens.get( 0 ).GetColumn() );
        tokens.remove( 0 );
      } // else if
      else {
        head.SetLeft( TreeConStruct( NULL, tokens, Getter ) );
        head.SetRight( TreeConStruct( new ConsNode(), tokens, Getter ) );
      } // else
    } // else
    
    return head;
  } // TreeConStruct()
  
  public void ReadSexp( ArrayList<Token> tokens, GetToken Getter )
  throws ErrorMessageException {
    if ( tokens.isEmpty() ) {
      Token aToken = Getter.CutToken();
      if ( aToken != null )
        tokens.add( aToken );
    } // if
  } // ReadSexp()
  
  public void TreeTravel( ConsNode head, int column, boolean aligned ) {
    if ( head == null ) ;
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
