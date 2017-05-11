package PL105_10220127;

import java.util.ArrayList;
import java.util.HashMap;

public class TreeBuilder {
  private ConsNode transTyper;
  private HashMap<String, ConsNode>symbolTable = new HashMap<String, ConsNode>();
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
        else if ( aToken.GetData().matches( "[\\.()]" ) )
          throw new ErrorMessageException( "UTL", aToken.GetData(), aToken.GetLine(),
                                           aToken.GetColumn() );
        else if ( aToken.GetData().matches( "nil" ) || aToken.GetData().matches( "#f" ) )
          head = new AtomNode( aToken.GetLine(), aToken.GetColumn() );
        else {
          if ( aToken.GetData().matches( "^[+-]?\\d+$" ) ) {
            aToken.SetData( String.format( "%.0f", Float.valueOf( aToken.GetData() ) ) );
            head = new AtomNode( aToken, DataType.INT );
          }
          else if ( aToken.GetData().matches( "^[+-]?(((0-9)*\\.[0-9]+)|([0-9]+\\.[0-9]*))$" ) ) {
            aToken.SetData( String.format( "%.3f", Float.valueOf( aToken.GetData() ) ) );
            head = new AtomNode( aToken, DataType.FLOAT );
          }
          else if ( aToken.GetData().matches( "t" ) || aToken.GetData().matches( "#t" ) ) {
            aToken.SetData( "#t" );
            head = new AtomNode( aToken, DataType.T );
          }
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
          return this.symbolTable.get( ( ( AtomNode ) head).GetAtom() );
        else
          throw new ErrorMessageException( "US", ( ( AtomNode ) head ).GetAtom().GetData() );
      }
    }
    else {
      if ( head.GetLeft().IsAtomNode() ) {
        AtomNode Left = ( AtomNode ) head.GetLeft();
        if ( Left.GetDataType() == DataType.SYMBOL ) {
          if ( Left.GetAtom().GetData().matches( "clean-environment" ) ) {
            this.symbolTable.clear();
            System.out.println( "environment cleaned" );
            throw new SystemMessageException("");
          }
          if ( Left.GetAtom().GetData().matches( "cons" ) ) {
            return head.GetRight();
          }
          else if ( Left.GetAtom().GetData().matches( "define" ) ) {
            System.out.println("fsdfsdsd");
            this.symbolTable.put( ( ( AtomNode ) head.GetRight().GetLeft() ).GetAtom().GetData(), 
                                  ( ( AtomNode ) head.GetRight().GetRight() ) );
            System.out.println("fsdfsdsd");
            System.out.println( Left.GetAtom().GetData() + " defined" );
            throw new SystemMessageException("");
          }
        }
      }
    }
    return head;
  } // Eval()
  
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
