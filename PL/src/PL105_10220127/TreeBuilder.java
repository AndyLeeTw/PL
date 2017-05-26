package PL105_10220127;

import java.awt.RenderingHints.Key;
import java.util.ArrayList;
import java.util.LinkedHashMap;

public class TreeBuilder {
  private static final int PLUS = 0;
  private static final int SUBTRACT = 1;
  private static final int MULTIPLY = 2;
  private static final int DIVIDE = 3;
  private static final int MORE = 0;
  private static final int LESS = 1;
  private static final int EQUAL = 2;
  private static final int MOREEQUAL = 3;
  private static final int LESSEQUAL = 4;
  private static final String []PERMITIVESYMBOLS = { "clean-environment", "define", "cons",
      "list", "car", "cdr", "pair?", "null?", "integer?", "real?", "number?", "string?", "not", "and", "or",
      "boolean?", "symbol?", "+", "-", "*", "/", ">", "<", "=", ">=", "exit",
      "<=", "string-append", "string>?", "string<?", "string=?", "eqv?",
      "equal?", "begin", "if", "cond" };
  private ConsNode mTransTyper;
  private LinkedHashMap<String, ConsNode>mSymbolTable = new LinkedHashMap<String, ConsNode>();
  
  public TreeBuilder() {
    this.AddPermitiveSymbol();
  } // TreeBuilder()
  
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
      head.SetLeft( this.Eval( head.GetLeft(), isTop ) );
      AtomNode function = ( AtomNode ) head.GetLeft();
      ConsNode sexp = head.GetRight();
      String functionName = function.GetAtom().GetData();
      if ( function.GetDataType() == DataType.SYMBOL ) {
        int argumentCount = 0;
        for ( ConsNode nodeNow = sexp; !nodeNow.IsAtomNode() ;
              nodeNow = nodeNow.GetRight() ) // count argument
          argumentCount++;
        if ( function.GetAtom().GetData().matches( "#<procedure (exit|clean-environment|define)>" ) ) {
          if ( !isTop )
            throw new SystemMessageException( "EL", this.TakeRealFunction( functionName ) );
          
          if ( function.GetAtom().GetData().matches( "#<procedure (exit|clean-environment)>" ) ) {
            this.CheckParameterAmount( argumentCount, 0, functionName );
            if ( function.GetAtom().GetData().matches( "#<procedure exit>" ) )
              throw new SystemMessageException( "EOFT" );
            else {
              this.mSymbolTable.clear();
              this.AddPermitiveSymbol();
              throw new SystemMessageException( "EC" );
            } // else
          } // if
          else { // #<procedure define>
            try {
              this.CheckParameterAmount( argumentCount, 2, functionName );
              if ( sexp.GetLeft().IsAtomNode() ) {
                String key = ( ( AtomNode ) sexp.GetLeft() ).GetAtom().GetData();
                if ( ( ( AtomNode ) sexp.GetLeft() ).GetDataType() == DataType.SYMBOL ) {
                  if ( key.matches( "exit|clean-environment|define" ) )
                    throw new SystemMessageException( "EL", this.TakeRealFunction( functionName ) );
                  else if ( !this.IsPermitiveSymbol( key ) ) {
                    this.mSymbolTable.put( key, this.Eval( sexp.GetRight().GetLeft(), false ) );
                    throw new SystemMessageException( "DEFINE", key );
                  } // if
                  else
                    throw new SystemMessageException( "EDF" );
                } // if
                else
                  throw new SystemMessageException( "EDF" );
              } // if
              else
                throw new SystemMessageException( "EDF" );
            } // try
            catch ( SystemMessageException e ) {
              if ( e.GetSystemCode().matches( "DEFINE|EL" ) )
                throw e;
              else
                this.PrintErrorDefineFormat( function, head );
            } // catch
            return null;
          } // else
        } // if
        else if ( function.GetAtom().GetData().matches( "#<procedure (cons|eqv[?]|equal[?])>" ) ) {
          this.CheckParameterAmount( argumentCount, 2, functionName );        
          if ( function.GetAtom().GetData().matches( "#<procedure eqv[?]>" ) )
            return this.CompareVeecor( sexp );
          else if ( function.GetAtom().GetData().matches( "#<procedure equal[?]>" ) )
            return this.Equal( sexp );
          else {
            sexp.SetLeft( this.Eval( sexp.GetLeft(), false ) );
            sexp.SetRight( this.Eval( sexp.GetRight().GetLeft(), false ) );
            return sexp;
          } // else
        } // else if
        else if ( function.GetAtom().GetData().matches( "#<procedure list>" ) ) {
          ConsNode sexpNow = sexp;
          while ( !sexpNow.IsAtomNode() ) {
            sexpNow.SetLeft( this.Eval( sexpNow.GetLeft(), false ) );
            sexpNow = sexpNow.GetRight();
          } // while
          
          return sexp;
        } // else if
        else if ( function.GetAtom().GetData().matches( "#<procedure car>" ) )
          return this.Eval( sexp.GetLeft(), false ).GetLeft();
        else if ( function.GetAtom().GetData().matches( "#<procedure cdr>" ) )
          return this.Eval( sexp.GetLeft(), false ).GetRight();
        else if ( function.GetAtom().GetData().matches( "#<procedure pair[?]>" ) ) {
          sexp.SetLeft( this.Eval( sexp.GetLeft(), false ) );
          if ( !sexp.GetLeft().IsAtomNode() )
            return this.T();
          else
            return this.NIL();
        } // else if
        else if ( function.GetAtom().GetData().matches( "#<procedure null[?]>" ) )
          return this.DecideTorNil( sexp.GetLeft(), DataType.NIL );
        else if ( function.GetAtom().GetData().matches( "#<procedure integer[?]>" ) )
          return this.DecideTorNil( sexp.GetLeft(), DataType.INT );
        else if ( function.GetAtom().GetData().matches( "#<procedure (real|number)[?]>" ) )
          return this.DecideTorNil( sexp.GetLeft(), DataType.INT, DataType.FLOAT );
        else if ( function.GetAtom().GetData().matches( "#<procedure string[?]>" ) )
          return this.DecideTorNil( sexp.GetLeft(), DataType.STRING );
        else if ( function.GetAtom().GetData().matches( "#<procedure boolean[?]>" ) )
          return this.DecideTorNil( sexp.GetLeft(), DataType.T, DataType.NIL );
        else if ( function.GetAtom().GetData().matches( "#<procedure symbol[?]>" ) )
          return this.DecideTorNil( sexp.GetLeft(), DataType.SYMBOL );
        else if ( function.GetAtom().GetData().matches( "#<procedure not>" ) ) {
          sexp.SetLeft( this.Eval( sexp.GetLeft(), false ) );
          if ( sexp.GetLeft().IsAtomNode() ) {
            AtomNode parameter = ( AtomNode ) sexp.GetLeft();
            if ( parameter.GetDataType() == DataType.NIL )
              return this.T();
            else
              return this.NIL();
          } // if
          else
            return this.NIL();
        } // else if
        else if ( function.GetAtom().GetData().matches( "#<procedure [+]>" ) )
          return this.Arithmetic( sexp, PLUS );
        else if ( function.GetAtom().GetData().matches( "#<procedure [-]>" ) )
          return this.Arithmetic( sexp, SUBTRACT );
        else if ( function.GetAtom().GetData().matches( "#<procedure [*]>" ) )
          return this.Arithmetic( sexp, MULTIPLY );
        else if ( function.GetAtom().GetData().matches( "#<procedure [/]>" ) )
          return this.Arithmetic( sexp, DIVIDE );
        else if ( function.GetAtom().GetData().matches( "#<procedure >>" ) )
          return this.Compare( sexp, MORE );
        else if ( function.GetAtom().GetData().matches( "#<procedure <>" ) )
          return this.Compare( sexp, LESS );
        else if ( function.GetAtom().GetData().matches( "#<procedure =>" ) )
          return this.Compare( sexp, EQUAL );
        else if ( function.GetAtom().GetData().matches( "#<procedure >=>" ) )
          return this.Compare( sexp, MOREEQUAL );
        else if ( function.GetAtom().GetData().matches( "#<procedure <=>" ) )
          return this.Compare( sexp, LESSEQUAL );
        else if ( function.GetAtom().GetData().matches( "#<procedure string>[?]>" ) )
          return this.CompareString( sexp, MORE );
        else if ( function.GetAtom().GetData().matches( "#<procedure string<[?]>" ) )
          return this.CompareString( sexp, LESS );
        else if ( function.GetAtom().GetData().matches( "#<procedure string=[?]>" ) )
          return this.CompareString( sexp, EQUAL );
        else if ( function.GetAtom().GetData().matches( "#<procedure string-append>" ) )
          return this.ComcatString( sexp );
        else if ( function.GetAtom().GetData().matches( "#<procedure if>" ) )
          return this.DoIf( sexp, false );
        else if ( function.GetAtom().GetData().matches( "#<procedure cond>" ) )
          return this.DoCond( sexp );
        else if ( function.GetAtom().GetData().matches( "#<procedure begin>" ) ) {
          ConsNode parameter;
          do {
            sexp.SetLeft( this.Eval( sexp.GetLeft(), false ) );
            parameter = sexp.GetLeft();
            sexp = sexp.GetRight();  
          } while ( !sexp.IsAtomNode() );
          return parameter;
        } // else if
        else if ( function.GetAtom().GetData().matches( "#<procedure and>" ) ) {
          sexp.SetLeft( this.Eval( sexp.GetLeft(), false ) );
          ConsNode parameter = sexp.GetLeft();
          if ( parameter.IsAtomNode() && ( ( AtomNode ) parameter ).GetDataType() == DataType.NIL )
            return parameter;
          else
            return this.Eval( sexp.GetRight().GetLeft(), false );
        } // else if
        else if ( function.GetAtom().GetData().matches( "#<procedure or>" ) ) {
          sexp.SetLeft( this.Eval( sexp.GetLeft(), false ) );
          ConsNode parameter = sexp.GetLeft();
          if ( parameter.IsAtomNode() && ( ( AtomNode ) parameter ).GetDataType() != DataType.NIL )
            return parameter;
          else
            return this.Eval( sexp.GetRight().GetLeft(), false );
        } // else if
        else
          throw new SystemMessageException( "AtANF", function.GetAtom().GetData() );
      } // else if
      else if ( function.GetDataType() == DataType.QUOTE )
        return sexp.GetLeft();
      else
        throw new SystemMessageException( "AtANF", function.GetAtom().GetData() );
    } // else
  } // Eval()
  
  private AtomNode T() {
    return new AtomNode( new Token( "#t", 0, 0 ), DataType.T );
  } // T()
  
  private AtomNode NIL() {
    return new AtomNode( 0, 0 );
  } // NIL()
  
  private void AddPermitiveSymbol() {
    ConsNode permitiveSymbol;
    for ( int i = 0; i < TreeBuilder.PERMITIVESYMBOLS.length ; i++ ) {
      permitiveSymbol = new AtomNode( new Token( "#<procedure " + TreeBuilder.PERMITIVESYMBOLS[i] + ">", 0, 0 ),
                                                 DataType.SYMBOL );
      this.mSymbolTable.put( TreeBuilder.PERMITIVESYMBOLS[i], permitiveSymbol );
    } // for
  } // AddPermitiveSymbol()
  
  private boolean IsPermitiveSymbol( String symbol ) {
    for ( int i = 0; i < TreeBuilder.PERMITIVESYMBOLS.length ; i++ )
      if ( symbol.compareTo( TreeBuilder.PERMITIVESYMBOLS[i] ) == 0 )
        return true;
    return false;
  } // IsPermitiveSymbol()
  
  private void PrintErrorDefineFormat( AtomNode function, ConsNode head ) throws SystemMessageException {
    function.GetAtom().SetData( "define" );
    System.out.print( "ERROR (DEFINE format) : " );
    this.TreeTravel( head, 0, true, false );
    function.GetAtom().SetData( "#<procedure define>" );
    throw new SystemMessageException( "EDF" );
  } // PrintErrorDefineFormat()
  
  private void CheckParameterAmount( int argumentCount, int argumentAmount, String functionName )
  throws SystemMessageException {
    if ( argumentCount != argumentAmount )
      throw new SystemMessageException( "INoA", this.TakeRealFunction( functionName ) );
  } // CheckParameterAmount()
  
  private String TakeRealFunction( String function ) {
    function = function.replace( "#<procedure ", "" );
    function = function.replace( ">", "" );
    return function;
  } // TakeRealFunction()

  private boolean IsReal( AtomNode atom ) {
    if ( atom.GetDataType() == DataType.INT || atom.GetDataType() == DataType.FLOAT )
      return true;
    else
      return false;
  } // IsReal()
  
  private ConsNode Arithmetic( ConsNode sexp, int operator ) throws SystemMessageException {
    float count = 0;
    boolean isIntDivide = false;
    sexp.SetLeft( this.Eval( sexp.GetLeft(), false ) );
    if ( sexp.GetLeft().IsAtomNode() ) {
      AtomNode parameter = ( AtomNode ) sexp.GetLeft();
      if ( this.IsReal( parameter ) ) {
        if ( operator == DIVIDE )
          isIntDivide = this.IsIntDivide( parameter );
        count = Float.parseFloat(  parameter.GetAtom().GetData() );
        sexp = sexp.GetRight();
        while ( !sexp.IsAtomNode() ) {
          sexp.SetLeft( this.Eval( sexp.GetLeft(), false ) );
          if ( sexp.GetLeft().IsAtomNode() ) {
            parameter = ( AtomNode ) sexp.GetLeft();
            if ( this.IsReal( parameter ) ) {
              if ( operator == PLUS )
                count += Float.parseFloat(  parameter.GetAtom().GetData() );
              else if ( operator == SUBTRACT )
                count -= Float.parseFloat(  parameter.GetAtom().GetData() );
              else if ( operator == MULTIPLY )
                count *= Float.parseFloat(  parameter.GetAtom().GetData() );
              else if ( operator == DIVIDE ) {
                if ( isIntDivide ) // if one parameter is float, the divide is float divide
                  isIntDivide = this.IsIntDivide( parameter );
                if ( Float.parseFloat(  parameter.GetAtom().GetData() ) != 0 ) {
                  count /= Float.parseFloat(  parameter.GetAtom().GetData() );
                  if ( isIntDivide ) {
                    Float integer = new Float( count );
                    count = integer.intValue();
                  } // if
                } // if
              } // else if
            } // if
          } // if
          
          sexp = sexp.GetRight();
        } // while
      } // if
    } // if
    
    if ( operator == DIVIDE )
      if ( isIntDivide )
        return new AtomNode( new Token( String.format( "%.0f", count ), 0, 0 ), DataType.INT );
      else
        return new AtomNode( new Token( String.format( "%.3f", count ), 0, 0 ), DataType.FLOAT );
    else
      return this.DecideValueType( count );
  } // Arithmetic()
  
  private ConsNode Compare( ConsNode sexp, int operator ) throws SystemMessageException {
    float comparer = 0;
    float compared = 0;
    boolean inOrder = true; 
    sexp.SetLeft( this.Eval( sexp.GetLeft(), false ) );
    if ( sexp.GetLeft().IsAtomNode() ) {
      AtomNode parameter = ( AtomNode ) sexp.GetLeft();
      if ( this.IsReal( parameter ) ) {
        comparer = Float.parseFloat(  parameter.GetAtom().GetData() );
        sexp = sexp.GetRight();
        while ( !sexp.IsAtomNode() ) {
          sexp.SetLeft( this.Eval( sexp.GetLeft(), false ) );
          if ( sexp.GetLeft().IsAtomNode() ) {
            parameter = ( AtomNode ) sexp.GetLeft();
            if ( this.IsReal( parameter ) ) {
              compared = Float.parseFloat(  parameter.GetAtom().GetData() );
              if ( inOrder )
                if ( operator == MORE )
                  if ( comparer > compared )
                    inOrder = true;
                  else
                    inOrder = false;
                else if ( operator == LESS )
                  if ( comparer < compared )
                    inOrder = true;
                  else
                    inOrder = false;
                else if ( operator == EQUAL )
                  if ( comparer == compared )
                    inOrder = true;
                  else
                    inOrder = false;
                else if ( operator == MOREEQUAL )
                  if ( comparer >= compared )
                    inOrder = true;
                  else
                    inOrder = false;
                else if ( operator == LESSEQUAL )
                  if ( comparer <= compared )
                    inOrder = true;
                  else
                    inOrder = false;
              comparer = compared;
            } // if
          } // if
          
          sexp = sexp.GetRight();              
        } // while
      } // if
    } // if
    
    if ( inOrder )
      return this.T();
    else
      return this.NIL();
  } // Compare()
  
  private boolean IsIntDivide( AtomNode atom ) {
    if ( atom.GetDataType() == DataType.INT )
      return true;
    else
      return false;
  } // IsIntDivide()
  
  private ConsNode CompareString( ConsNode sexp, int operator ) throws SystemMessageException {
    String compareString;
    String nextString;
    boolean inOrder = true; 
    sexp.SetLeft( this.Eval( sexp.GetLeft(), false ) );
    if ( sexp.GetLeft().IsAtomNode() ) {
      AtomNode parameter = ( AtomNode ) sexp.GetLeft();
      int parameterDataType = parameter.GetDataType();
      if ( parameterDataType == DataType.STRING ) {
        compareString = parameter.GetAtom().GetData();
        sexp = sexp.GetRight();
        while ( !sexp.IsAtomNode() ) {
          sexp.SetLeft( this.Eval( sexp.GetLeft(), false ) );
          if ( sexp.GetLeft().IsAtomNode() ) {
            parameter = ( AtomNode ) sexp.GetLeft();
            parameterDataType = parameter.GetDataType();
            if ( parameterDataType == DataType.STRING ) {
              nextString = parameter.GetAtom().GetData();
              if ( inOrder )
                if ( operator == MORE )
                  if ( compareString.compareTo( nextString ) > 0 )
                    inOrder = true;
                  else
                    inOrder = false;
                else if ( operator == LESS )
                  if ( compareString.compareTo( nextString ) < 0 )
                    inOrder = true;
                  else
                    inOrder = false;
                else if ( operator == EQUAL )
                  if ( compareString.compareTo( nextString ) == 0 )
                    inOrder = true;
                  else
                    inOrder = false;
              compareString = nextString;
            } // if
          } // if
          
          sexp = sexp.GetRight();              
        } // while
      } // if
    } // if
    
    if ( inOrder )
      return this.T();
    else
      return this.NIL();
  } // CompareString()
  
  private AtomNode CompareVeecor( ConsNode sexp ) throws SystemMessageException {
    ConsNode leftParameter, rightParameter;
    sexp.SetLeft( this.Eval( sexp.GetLeft(), false ) );
    leftParameter = sexp.GetLeft();
    if ( !sexp.GetRight().IsAtomNode() ) {
      sexp = sexp.GetRight();
      sexp.SetLeft( this.Eval( sexp.GetLeft(), false ) );
      rightParameter = sexp.GetLeft();
      if ( leftParameter.IsAtomNode() && rightParameter.IsAtomNode() ) {
        AtomNode left = ( AtomNode ) leftParameter;
        AtomNode right = ( AtomNode ) rightParameter;
        if ( left.GetDataType() == DataType.STRING && right.GetDataType() == DataType.STRING )
          if ( leftParameter == rightParameter )
            return this.T();
          else
            return this.NIL();
        else if ( left.GetDataType() != DataType.STRING && right.GetDataType() != DataType.STRING )
          if ( left.GetAtom().GetData().compareTo( right.GetAtom().GetData() ) == 0 )
            return this.T();
          else
            return this.NIL();
        else
          return this.NIL();
      } // if
      else if ( leftParameter == rightParameter )
        return this.T();
      else
        return this.NIL();
    } // if
    
    return null;
  } // CompareVeecor()
  
  private AtomNode Equal( ConsNode sexp ) throws SystemMessageException {
    ConsNode leftParameter, rightParameter;
    sexp.SetLeft( this.Eval( sexp.GetLeft(), false ) );
    leftParameter = sexp.GetLeft();
    if ( !sexp.GetRight().IsAtomNode() ) {
      sexp = sexp.GetRight();
      sexp.SetLeft( this.Eval( sexp.GetLeft(), false ) );
      rightParameter = sexp.GetLeft();
      return this.TreeCompare( leftParameter, rightParameter );
    } // if
    
    return null;
  } // Equal()
  
  private AtomNode TreeCompare( ConsNode treeA, ConsNode treeB ) {
    if ( treeA.IsAtomNode() && treeB.IsAtomNode() ) {
      AtomNode a = ( AtomNode ) treeA;
      AtomNode b = ( AtomNode ) treeB;
      if ( a.GetAtom().GetData().compareTo( b.GetAtom().GetData() ) == 0 )
        return this.T();
      else
        return this.NIL();
    } // if
    else if ( !treeA.IsAtomNode() && !treeB.IsAtomNode() ) {
      AtomNode leftResult, rightResult;
      leftResult = this.TreeCompare( treeA.GetLeft(), treeB.GetLeft() );
      rightResult = this.TreeCompare( treeA.GetRight(), treeB.GetRight() );
      if ( leftResult.IsNil() || rightResult.IsNil() )
        return this.NIL();
      else
        return this.T();
    } // else if
    else
      return this.NIL();
  } // TreeCompare()

  private AtomNode DecideValueType( float count ) {
    Float intValue = new Float( count );
    if ( intValue.intValue() == count )
      return new AtomNode( new Token( String.format( "%.0f", count ), 0, 0 ), DataType.INT );
    else
      return new AtomNode( new Token( String.format( "%.3f", count ), 0, 0 ), DataType.FLOAT );
  } // DecideValueType()
  
  private ConsNode ComcatString( ConsNode sexp ) throws SystemMessageException {
    String allString = "\"";
    String comcatingString;
    AtomNode parameter;
    int parameterDataType;
    while ( !sexp.IsAtomNode() ) {
      sexp.SetLeft( this.Eval( sexp.GetLeft(), false ) );
      if ( sexp.GetLeft().IsAtomNode() ) {
        parameter = ( AtomNode ) sexp.GetLeft();
        parameterDataType = parameter.GetDataType();
        if ( parameterDataType == DataType.STRING ) {
          comcatingString = parameter.GetAtom().GetData();
          allString += comcatingString.substring( 1, comcatingString.length() - 1 );
        } // if
      } // if
      
      sexp = sexp.GetRight();
    } // while
    
    return new AtomNode( new Token( allString + "\"", 0, 0 ), DataType.STRING );
  } // ComcatString()

  private ConsNode DecideTorNil( ConsNode parameter, int dataType ) throws SystemMessageException {
    parameter = this.Eval( parameter, false );
    if ( parameter.IsAtomNode() )
      if ( ( ( AtomNode ) parameter ).GetDataType() == dataType )
        return this.T();
      else
        return this.NIL();
    else
      return this.NIL();
  } // DecideTorNil()
   
  private ConsNode DecideTorNil( ConsNode sexp, int dataType1, int dataType2 )
  throws SystemMessageException {
    if ( ! ( ( AtomNode ) this.DecideTorNil( sexp, dataType1 ) ).IsNil() ||
         ! ( ( AtomNode ) this.DecideTorNil( sexp, dataType2 ) ).IsNil() )
      return new AtomNode( new Token( "#t", 0, 0 ), DataType.T );
    else
      return new AtomNode( 0, 0 );
  } // DecideTorNil()
  
  private ConsNode MakeDecision( ConsNode condition, ConsNode Tpart, ConsNode NILpart, boolean isCond )
  throws SystemMessageException {
    if ( condition.IsAtomNode() )
      if ( ( ( AtomNode ) condition ).GetDataType() == DataType.NIL && !isCond )
        if ( NILpart != null )
          return this.Eval( NILpart, false );
        else
          return null;
      else if ( ( ( AtomNode ) condition ).GetDataType() != DataType.NIL )
        if ( Tpart != null )
          return this.Eval( Tpart, false );
        else
          return null;
      else
        return null;
    else
      return this.Eval( Tpart, false );
  } // MakeDecision()
  
  private ConsNode DoIf( ConsNode sexp, boolean isCond ) throws SystemMessageException {
    ConsNode condition;
    sexp.SetLeft( this.Eval( sexp.GetLeft(), false ) );
    condition = sexp.GetLeft();
    if ( isCond ) {
      while ( !sexp.GetRight().IsAtomNode() )
        sexp = sexp.GetRight();
      return this.MakeDecision( condition, sexp.GetLeft(), DataType.NULL, isCond );
    } // if
    else
      return this.MakeDecision( condition, sexp.GetRight().GetLeft(),
                                sexp.GetRight().GetRight().GetLeft(), isCond );
  } // DoIf()
  
  private ConsNode DoCond( ConsNode sexp ) throws SystemMessageException {
    AtomNode condition;
    ConsNode checkDecisionType;
    int count = 0;
    if ( !sexp.GetLeft().IsAtomNode() ) {
      checkDecisionType = null;
      while ( checkDecisionType == null && !sexp.GetRight().IsAtomNode() ) {
        count++;
        checkDecisionType = this.DoIf( sexp.GetLeft(), true );
        if ( checkDecisionType == null )
          sexp = sexp.GetRight();
      } // while
      
      if ( checkDecisionType == null )
        if ( !sexp.GetLeft().IsAtomNode() ) {
          if ( sexp.GetLeft().GetLeft().IsAtomNode() ) {
            condition = ( AtomNode ) sexp.GetLeft().GetLeft();
            if ( condition.GetAtom().GetData().matches( "else" ) && count != 0 )
              condition.SetDataType( DataType.T );
          } // if
          
          checkDecisionType = this.DoIf( sexp.GetLeft(), true );
        } // if
      
      return checkDecisionType;
    } // if
    
    return null;
  } // DoCond()
  
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
