package PL105_10220127;

import java.util.ArrayList;
import java.util.HashMap;

public class TreeBuilder {
  private ConsNode transTyper;
  private HashMap<String, AtomNode>symbolTable = new HashMap<String, AtomNode>();
  public TreeBuilder() { }
  
  public ConsNode TreeConStruct( ConsNode head, ArrayList<Token> tokens, GetToken Getter )
  throws ErrorMessageException {
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
      } // else if
      else {
        if ( aToken.GetData().matches( "'" ) ) {
          head = new ConsNode();
          aToken.SetData( "quote" );
          this.transTyper = new AtomNode( aToken, DataType.QUOTE );
          tokens.remove( 0 );
          head.SetLeft( this.transTyper );
          this.transTyper = new ConsNode();
          head.SetRight( this.transTyper );
          this.transTyper.SetLeft( TreeConStruct( DataType.NULL, tokens, Getter ) );
          ConsNode tTr = new AtomNode( 0, 0 );
          this.transTyper.SetRight( tTr );
        } // if
        else if ( aToken.GetData().matches( "[\\.]" ) )
          throw new ErrorMessageException( "UTL", aToken.GetData(), aToken.GetLine(),
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
        aToken = this.ReadSexp(tokens, Getter);
        if ( !aToken.GetData().matches( "[)]" ) )
          throw new ErrorMessageException( "UTR", aToken.GetData(), aToken.GetLine(),
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
  throws ErrorMessageException {
    if ( tokens.isEmpty() )
      tokens.add( Getter.CutToken() );
    return tokens.get( 0 );
  } // ReadSexp()
  
  public ConsNode Eval( ConsNode head, boolean isTop ) throws SystemMessageException, ErrorMessageException {
    if ( head.IsAtomNode() ) {
      if ( ( ( AtomNode ) head ).GetDataType() != DataType.SYMBOL )
        return head;
      else {
        if ( this.symbolTable.containsKey( ( ( AtomNode ) head).GetAtom().GetData() ) )
          return this.symbolTable.get( ( ( AtomNode ) head).GetAtom().GetData() );
        else
          throw new ErrorMessageException( "US", ( ( AtomNode ) head ).GetAtom().GetData() );
      }
    }
    else {
      AtomNode function = ( AtomNode ) head.GetLeft();
      ConsNode sexp = head.GetRight();
      if ( function.GetDataType() == DataType.SYMBOL ) {
        if ( function.GetAtom().GetData().matches( "clean-environment" ) ) {
          this.symbolTable.clear();
          throw new SystemMessageException( "environment cleaned" );
        }
        if ( function.GetAtom().GetData().matches( "cons" ) ) {
          sexp.SetLeft( this.Eval( sexp.GetLeft(), false ) );
          sexp = sexp.GetRight();
          sexp.SetLeft( this.Eval( sexp.GetLeft(), false ) );
          return sexp;
        }
        else if ( function.GetAtom().GetData().matches( "list" ) ) {
          ConsNode sexpNow = sexp;
          do {
            sexpNow.SetLeft( this.Eval( sexpNow.GetLeft(), false ) );
            sexpNow = sexpNow.GetRight();
          } while( !sexpNow.IsAtomNode() );
          
          return sexp;
        }
        else if ( function.GetAtom().GetData().matches( "define" ) ) {
          this.symbolTable.put( ( ( AtomNode ) sexp.GetLeft() ).GetAtom().GetData(), 
                                ( ( AtomNode ) sexp.GetRight().GetLeft() ) );
          throw new SystemMessageException( ( ( AtomNode ) sexp.GetLeft() ).GetAtom().GetData() + " defined" );
        }
      }
      else if ( function.GetDataType() == DataType.QUOTE )
        return sexp.GetLeft();
      else
        throw new SystemMessageException( "ERROR (attempt to apply non-function) : " + function.GetAtom().GetData() );
    } // else
    
    return head;
  } // Eval()
  
  public void TreeTravel( ConsNode head, int column, boolean firstLine ) {
    if ( head == null ) ;
    else if ( head.IsAtomNode() ) {
      //if ( !aligned ) {
        for ( int i = 0; i < column ; i++ )
          System.out.print( "  " );
      //} // if*/
      
      System.out.println( head.ToString() );
    } // else if
    else {
      //if ( !aligned ) {
        for ( int i = 0; i < column ; i++ )
          System.out.print( "  " );
      //} // if*/
      System.out.print( "( " );
      if ( !head.GetLeft().IsAtomNode() ) {
        TreeTravel( head.GetLeft(), column + 1, true );
      } // if
      else
        TreeTravel( head.GetLeft(), column + 1, firstLine ); // aligned will be true in initial time
      if ( head.GetLeft() != null && !head.GetLeft().IsAtomNode() ) {
        for ( int i = 0; i < column ; i++ )
          System.out.print( "  " );
        //System.out.println( ")" );
      } // if
      
      if ( head.GetRight().IsAtomNode() ) {
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
      System.out.println( ")" );
    } // else
  } // TreeTravel()
} // class TreeBuilder
