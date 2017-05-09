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
        this.ReadSexp( tokens, Getter, 2 );
        tokens.remove( 0 );
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
            throw new ErrorMessageException( "UT", realToken.GetData(), realToken.GetLine(),
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
        ConsNode theRight = head;
        while ( !theRight.IsAtomNode() ) { // to know last token column
          theRight = theRight.GetRight();
        } // while
        
        AtomNode atom = ( AtomNode ) theRight;
        atom.GetAtom().SetColumn( tokens.get( 0 ).GetColumn() ); // end to know last token column
        tokens.remove( 0 );
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
  
  public void ReadSexp( ArrayList<Token> tokens, GetToken Getter, int tokenssize )
  throws ErrorMessageException {
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
  
  public AtomNode FindLastToken( ConsNode head ) {
    if ( head.IsAtomNode() )
      return ( AtomNode ) head;
    else {
      AtomNode left = FindLastToken( head.GetLeft() );
      AtomNode right = FindLastToken( head.GetRight() );
      if ( left.GetAtom().GetLine() == right.GetAtom().GetLine() )
        if ( left.GetAtom().GetColumn() > right.GetAtom().GetColumn() )
          return left;
        else
          return right;
      else if ( left.GetAtom().GetLine() > right.GetAtom().GetLine() )
        return left;
      else
        return right;
    } // else
  } // FindLastToken()
} // class TreeBuilder
