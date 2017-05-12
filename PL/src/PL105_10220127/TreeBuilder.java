package PL105_10220127;

import java.util.ArrayList;
import java.util.LinkedHashMap;

public class TreeBuilder {
  private ConsNode mTransTyper;
  private LinkedHashMap<String, ConsNode>mSymbolTable = new LinkedHashMap<String, ConsNode>();
  public TreeBuilder() { }
  
  public ConsNode TreeConStruct( ConsNode head, ArrayList<Token> tokens, GetToken Getter )
  throws SystemMessageException {
    Token aToken = this.ReadSexp( tokens, Getter );
    if ( head == null ) {
      if ( aToken.GetData().matches( "[(]" ) ) {
        tokens.remove( 0 );
        aToken = this.ReadSexp( tokens, Getter );
        if ( aToken.GetData().matches( "[)]" ) ) {
          head =  new AtomNode( aToken.GetLine(), aToken.GetColumn() );
          tokens.remove( 0 );
        } // if
        else {
          head = new ConsNode();
          head.SetLeft( TreeConStruct( DataType.NULL, tokens, Getter ) );
          head.SetRight( TreeConStruct( new ConsNode(), tokens, Getter ) );
        } // else
      } // if
      else {
        if ( aToken.GetData().matches( "'" ) ) {
          head = new ConsNode();
          aToken.SetData( "quote" );
          this.mTransTyper = new AtomNode( aToken, DataType.QUOTE );
          tokens.remove( 0 );
          head.SetLeft( this.mTransTyper );
          this.mTransTyper = new ConsNode();
          head.SetRight( this.mTransTyper );
          this.mTransTyper.SetLeft( TreeConStruct( DataType.NULL, tokens, Getter ) );
          ConsNode tTr = new AtomNode( 0, 0 );
          this.mTransTyper.SetRight( tTr );
        } // if
        else if ( aToken.GetData().matches( "[\\.)]" ) )
          throw new SystemMessageException( "UTL", aToken.GetData(), aToken.GetLine(),
                                            aToken.GetColumn() );
        else {
          if ( aToken.GetData().matches( "quote" ) )
            head = new AtomNode( aToken, DataType.QUOTE );
          else if ( aToken.GetData().matches( "nil" ) || aToken.GetData().matches( "#f" ) )
            head = new AtomNode( aToken.GetLine(), aToken.GetColumn() );
          else if ( aToken.GetData().matches( "^[+-]?\\d+$" ) ) {
            aToken.SetData( String.format( "%.0f", Float.valueOf( aToken.GetData() ) ) );
            head = new AtomNode( aToken, DataType.INT );
          } // else if
          else if ( aToken.GetData().matches( "^[+-]?(((0-9)*\\.[0-9]+)|([0-9]+\\.[0-9]*))$" ) ) {
            aToken.SetData( String.format( "%.3f", Float.valueOf( aToken.GetData() ) ) );
            head = new AtomNode( aToken, DataType.FLOAT );
          } // else if
          else if ( aToken.GetData().matches( "t" ) || aToken.GetData().matches( "#t" ) ) {
            aToken.SetData( "#t" );
            head = new AtomNode( aToken, DataType.T );
          } // else if
          else if ( aToken.GetData().startsWith( "\"" ) )
            head = new AtomNode( aToken, DataType.STRING );
          else
            head = new AtomNode( aToken, DataType.SYMBOL );
          tokens.remove( 0 );
        } // else
      } // else
    } // if
    else {
      if ( aToken.GetData().matches( "[\\.]" ) ) {
        tokens.remove( 0 );
        head = TreeConStruct( DataType.NULL, tokens, Getter );
        aToken = this.ReadSexp( tokens, Getter );
        if ( !aToken.GetData().matches( "[)]" ) )
          throw new SystemMessageException( "UTR", aToken.GetData(), aToken.GetLine(),
                                            aToken.GetColumn() );
        else
          tokens.remove( 0 );
      } // if
      else if ( aToken.GetData().matches( "[)]" ) ) {
        head =  new AtomNode( aToken.GetLine(), aToken.GetColumn() );
        tokens.remove( 0 );
      } // else if
      else {
        head.SetLeft( TreeConStruct( DataType.NULL, tokens, Getter ) );
        head.SetRight( TreeConStruct( new ConsNode(), tokens, Getter ) );
      } // else
    } // else
    
    return head;
  } // TreeConStruct()
  
  public Token ReadSexp( ArrayList<Token> tokens, GetToken Getter )
  throws SystemMessageException {
    if ( tokens.isEmpty() )
      tokens.add( Getter.CutToken() );
    return tokens.get( 0 );
  } // ReadSexp()
  
  public ConsNode Eval( ConsNode head, boolean isTop ) throws SystemMessageException {
    if ( head.IsAtomNode() ) {
      if ( ( ( AtomNode ) head ).GetDataType() != DataType.SYMBOL )
        return head;
      else {
        String key = ( ( AtomNode ) head ).GetAtom().GetData();
        if ( this.mSymbolTable.containsKey( key ) )
          return this.mSymbolTable.get( key );
        else
          throw new SystemMessageException( "US", key );
      } // else
    } // if
    else {
      AtomNode function = ( AtomNode ) head.GetLeft();
      ConsNode sexp = head.GetRight();
      if ( function.GetDataType() == DataType.SYMBOL ) {
        if ( function.GetAtom().GetData().matches( "clean-environment" ) ) {
          this.mSymbolTable.clear();
          throw new SystemMessageException( "EC" );
        } // if
        else if ( function.GetAtom().GetData().matches( "define" ) ) {
          String key = ( ( AtomNode ) sexp.GetLeft() ).GetAtom().GetData();
          this.mSymbolTable.put( key, this.Eval( sexp.GetRight().GetLeft(), false ) );
          throw new SystemMessageException( "DEFINE", key );
        } // else if
        else if ( function.GetAtom().GetData().matches( "cons" ) ) {
          sexp.SetLeft( this.Eval( sexp.GetLeft(), false ) );
          sexp.SetRight( this.Eval( sexp.GetRight().GetLeft(), false ) );
          return sexp;
        } // else if
        else if ( function.GetAtom().GetData().matches( "list" ) ) {
          ConsNode sexpNow = sexp;
          do {
            sexpNow.SetLeft( this.Eval( sexpNow.GetLeft(), false ) );
            sexpNow = sexpNow.GetRight();
          } while ( !sexpNow.IsAtomNode() );
          
          return sexp;
        } // else if
        else if ( function.GetAtom().GetData().matches( "car" ) )
          return this.Eval( sexp.GetLeft(), false ).GetLeft();
        else if ( function.GetAtom().GetData().matches( "cdr" ) )
          return this.Eval( sexp.GetLeft(), false ).GetRight();
        else if ( function.GetAtom().GetData().matches( "pair[?]" ) ) {
          if ( sexp.GetLeft().IsAtomNode() )
            return new AtomNode( 0, 0 );
          else
            return new AtomNode( new Token( "#t", 0, 0 ), DataType.T );
        } // else if
        else if ( function.GetAtom().GetData().matches( "null[?]" ) )
          return this.DecideTorNil( sexp, DataType.NIL );
        else if ( function.GetAtom().GetData().matches( "integer[?]" ) )
          return this.DecideTorNil( sexp, DataType.INT );
        else if ( function.GetAtom().GetData().matches( "real[?]|number[?]" ) )
          return this.DecicideTorNil( sexp, DataType.INT, DataType.FLOAT );
        else if ( function.GetAtom().GetData().matches( "string[?]" ) )
          return this.DecideTorNil( sexp, DataType.STRING );
        else if ( function.GetAtom().GetData().matches( "boolean[?]" ) )
          return this.DecicideTorNil( sexp, DataType.T, DataType.NIL );
        else if ( function.GetAtom().GetData().matches( "symbol[?]" ) )
          return this.DecideTorNil( sexp, DataType.SYMBOL );
      } // if
      else if ( function.GetDataType() == DataType.QUOTE )
        return sexp.GetLeft();
      else
        throw new SystemMessageException( "AtANF", function.GetAtom().GetData() );
    } // else
    
    return head;
  } // Eval()
  
  private ConsNode DecideTorNil( ConsNode sexp, int dataType ) {
    if ( sexp.GetLeft().IsAtomNode() && ( ( AtomNode ) sexp.GetLeft() ).GetDataType() == dataType )
      return new AtomNode( new Token( "#t", 0, 0 ), DataType.T );
    else
      return new AtomNode( 0, 0 );
  } // DecideTorNil()
  
  private ConsNode DecicideTorNil( ConsNode sexp, int dataType1, int dataType2 ) {
    if ( sexp.GetLeft().IsAtomNode() && ( ( AtomNode ) sexp.GetLeft() ).GetDataType() == dataType1 )
      return new AtomNode( new Token( "#t", 0, 0 ), DataType.T );
    else
      return this.DecideTorNil( sexp, dataType2 );
  } // DecideTorNil()
  
  public void TreeTravel( ConsNode head, int column, boolean isTop, boolean needSpace ) {
    if ( head == null ) ;
    else if ( head.IsAtomNode() )
      System.out.println( head.ToString() );
    else {
      if ( isTop )
        System.out.print( "( " );
      if ( !head.GetLeft().IsAtomNode() ) {
        if ( needSpace )
          for ( int i = 0; i < column + 1 ; i++ )
            System.out.print( "  " );
        System.out.print( "( " );
        this.TreeTravel( head.GetLeft(), column + 1, false, false ); // ( ( ( ( ( (
      } // if
      else {
        if ( needSpace ) 
          for ( int i = 0; i < column + 1 ; i++ )
            System.out.print( "  " );
        this.TreeTravel( head.GetLeft(), column + 1, false, true );
      } // else
      
      if ( head.GetRight().IsAtomNode() ) {
        AtomNode atom = ( AtomNode ) head.GetRight();
        if ( !atom.IsNil() ) {
          for ( int i = 0; i < column + 1 ; i++ )
            System.out.print( "  " );
          System.out.println( "." );
          for ( int i = 0; i < column + 1 ; i++ )
            System.out.print( "  " );
          this.TreeTravel( head.GetRight(), column, false, true );
        } // if
        
        for ( int i = 0; i < column ; i++ ) // RP need align with LP
          System.out.print( "  " );
        System.out.println( ")" );
      } // if
      else
        this.TreeTravel( head.GetRight(), column, false, true );
    } // else
  } // TreeTravel()
} // class TreeBuilder
