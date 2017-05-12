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
    if ( head == null ) { // left leaf or initial
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
      else {            // right leaf
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
  
  private Token ReadSexp( ArrayList<Token> tokens, GetToken Getter )
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
          return this.DecideTorNil( sexp.GetLeft(), DataType.NIL );
        else if ( function.GetAtom().GetData().matches( "integer[?]" ) )
          return this.DecideTorNil( sexp.GetLeft(), DataType.INT );
        else if ( function.GetAtom().GetData().matches( "real[?]|number[?]" ) )
          return this.DecideTorNil( sexp.GetLeft(), DataType.INT, DataType.FLOAT );
        else if ( function.GetAtom().GetData().matches( "string[?]" ) )
          return this.DecideTorNil( sexp.GetLeft(), DataType.STRING );
        else if ( function.GetAtom().GetData().matches( "boolean[?]" ) )
          return this.DecideTorNil( sexp.GetLeft(), DataType.T, DataType.NIL );
        else if ( function.GetAtom().GetData().matches( "symbol[?]" ) )
          return this.DecideTorNil( sexp.GetLeft(), DataType.SYMBOL );
        else if ( function.GetAtom().GetData().matches( "not" ) ) {
          if ( sexp.GetLeft().IsAtomNode() ) {
            sexp.SetLeft( this.Eval( sexp.GetLeft(), false ) );
            AtomNode parameter = ( AtomNode ) sexp.GetLeft();
            if ( parameter.GetDataType() == DataType.NIL )
              return this.T();
            else
              return this.NIL();
          }
          else
            return new AtomNode( 0, 0 );
        } // else if
        else if ( function.GetAtom().GetData().matches( "[+]" ) )
          return this.Plus( sexp, true );
        else if ( function.GetAtom().GetData().matches( "[-]" ) )
          return this.Plus( sexp, false );
        else if ( function.GetAtom().GetData().matches( "[*]" ) ) {
          ConsNode sexpNow = sexp;
          float count;
          if ( ( ( AtomNode ) this.DecideTorNil( sexpNow.GetLeft(),
                                                 DataType.INT, DataType.FLOAT ) ).GetDataType() == DataType.T ) {
            count = Float.parseFloat( ( ( AtomNode ) sexpNow.GetLeft() ).GetAtom().GetData() );
            while ( !sexpNow.GetRight().IsAtomNode() ) {
              sexpNow = sexpNow.GetRight();
              if ( ( ( AtomNode ) this.DecideTorNil( sexpNow.GetLeft(),
                                                     DataType.INT, DataType.FLOAT ) ).GetDataType() == DataType.T )
                count *= Float.parseFloat( ( ( AtomNode ) sexpNow.GetLeft() ).GetAtom().GetData() );
            } // if
            return this.DecideValueType( count );
          } // else if
        } // else if
        else if ( function.GetAtom().GetData().matches( "[/]" ) ) {
          ConsNode sexpNow = sexp;
          float count;
          if ( ( ( AtomNode ) this.DecideTorNil( sexpNow.GetLeft(),
                                                 DataType.INT, DataType.FLOAT ) ).GetDataType() == DataType.T ) {
            count = Float.parseFloat( ( ( AtomNode ) sexpNow.GetLeft() ).GetAtom().GetData() );
            while ( !sexpNow.GetRight().IsAtomNode() ) {
              sexpNow = sexpNow.GetRight();
              if ( ( ( AtomNode ) this.DecideTorNil( sexpNow.GetLeft(),
                                                     DataType.INT, DataType.FLOAT ) ).GetDataType() == DataType.T )
                count /= Float.parseFloat( ( ( AtomNode ) sexpNow.GetLeft() ).GetAtom().GetData() );
            } // if
            return this.DecideValueType( count );
          } // else if
        } // else if
      } // else if
      else if ( function.GetDataType() == DataType.QUOTE )
        return sexp.GetLeft();
      else
        throw new SystemMessageException( "AtANF", function.GetAtom().GetData() );
    } // else
    
    return head;
  } // Eval()
  
  private AtomNode T() {
    return new AtomNode( new Token( "#t", 0, 0 ), DataType.T );
  } // T()
  
  private AtomNode NIL() {
    return new AtomNode( 0, 0);
  } // NIL()

  private ConsNode Plus( ConsNode sexp, boolean isPlus ) throws SystemMessageException {
    ConsNode sexpNow = sexp;
    float count = 0;
    if ( ( ( AtomNode ) this.DecideTorNil( sexpNow.GetLeft(),
                                           DataType.INT, DataType.FLOAT ) ).GetDataType() == DataType.T ) {
      count += Float.parseFloat( ( ( AtomNode ) sexpNow.GetLeft() ).GetAtom().GetData() );
      while ( !sexpNow.GetRight().IsAtomNode() ) {
        sexpNow = sexpNow.GetRight();
        if ( ( ( AtomNode ) this.DecideTorNil( sexpNow.GetLeft(),
                                               DataType.INT, DataType.FLOAT ) ).GetDataType() == DataType.T )
          if ( isPlus )
            count += Float.parseFloat( ( ( AtomNode ) sexpNow.GetLeft() ).GetAtom().GetData() );
          else
            count -= Float.parseFloat( ( ( AtomNode ) sexpNow.GetLeft() ).GetAtom().GetData() );
      }
    }
    return this.DecideValueType( count );
  }
  
  private AtomNode DecideValueType( float count ) {
    Float intValue = new Float( count );
    if ( intValue.intValue() == count )
      return new AtomNode( new Token( String.format( "%.0f", count ), 0 , 0 ), DataType.INT );
    else
      return new AtomNode( new Token( String.format( "%.3f", count ), 0 , 0 ), DataType.FLOAT );
  }
  
  private ConsNode DecideTorNil( ConsNode sexp, int dataType ) throws SystemMessageException {
    sexp = this.Eval( sexp, false );
    if ( sexp.IsAtomNode() )
      if ( ( ( AtomNode ) sexp ).GetDataType() == dataType )
        return this.T();
      else
        return this.NIL();
    else {
      return null;
    }
  } // DecideTorNil()
  
  private ConsNode DecideTorNil( ConsNode sexp, int dataType1, int dataType2 )
  throws SystemMessageException {
    if ( ! ( ( AtomNode ) this.DecideTorNil( sexp, dataType1 ) ).IsNil() ||
         ! ( ( AtomNode ) this.DecideTorNil( sexp, dataType2 ) ).IsNil() )
      return new AtomNode( new Token( "#t", 0, 0 ), DataType.T );
    else
      return new AtomNode( 0, 0 );
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
