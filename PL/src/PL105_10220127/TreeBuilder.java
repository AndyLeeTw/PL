package PL105_10220127;

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
      "equal?", "begin", "if", "cond", "list?", "atom?" };
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
        ConsNode nodeNow;
        for ( nodeNow = sexp ; !nodeNow.IsAtomNode() ;
              nodeNow = nodeNow.GetRight() ) // count argument
          argumentCount++;
        if ( ! ( ( AtomNode ) nodeNow ).IsNil() )
          throw new SystemMessageException( "NL", "", head );
        if ( functionName.matches( "#<procedure (exit|clean-environment|define)>" ) ) {
          if ( !isTop )
            throw new SystemMessageException( "EL", this.TakeRealFunction( functionName ) );
          
          if ( functionName.matches( "#<procedure (exit|clean-environment)>" ) ) {
            this.CheckParameterAmount( argumentCount, 0, functionName, false );
            if ( functionName.matches( "#<procedure exit>" ) )
              throw new SystemMessageException( "EOFT" );
            else {
              this.mSymbolTable.clear();
              this.AddPermitiveSymbol();
              throw new SystemMessageException( "EC" );
            } // else
          } // if
          else { // #<procedure define>
            try {
              this.CheckParameterAmount( argumentCount, 2, functionName, false );
            } // try
            catch ( SystemMessageException e ) {
              throw new SystemMessageException( "EF", this.TakeRealFunction( functionName ), head );
            } // catch
            if ( sexp.GetLeft().IsAtomNode() ) {
              String key = ( ( AtomNode ) sexp.GetLeft() ).GetAtom().GetData();
              if ( ( ( AtomNode ) sexp.GetLeft() ).GetDataType() == DataType.SYMBOL ) {
                if ( !this.IsPermitiveSymbol( key ) ) {
                  this.mSymbolTable.put( key, this.Eval( sexp.GetRight().GetLeft(), false ) );
                  throw new SystemMessageException( "DEFINE", key );
                } // if
                else
                  throw new SystemMessageException( "EF", this.TakeRealFunction( functionName ), head );
              } // if
              else
                throw new SystemMessageException( "EF", this.TakeRealFunction( functionName ), head );
            } // if
            else
              throw new SystemMessageException( "EF", this.TakeRealFunction( functionName ), head );
          } // else
        } // if
        else if ( functionName.matches( "#<procedure (cons|eqv[?]|equal[?])>" ) ) {
          this.CheckParameterAmount( argumentCount, 2, functionName, false );     
          if ( functionName.matches( "#<procedure eqv[?]>" ) )
            return this.CompareVeecor( sexp );
          else if ( functionName.matches( "#<procedure equal[?]>" ) )
            return this.Equal( sexp );
          else {
            sexp.SetLeft( this.Eval( sexp.GetLeft(), false ) );
            sexp.SetRight( this.Eval( sexp.GetRight().GetLeft(), false ) );
            return sexp;
          } // else
        } // else if
        else if ( functionName.matches( "#<procedure list>" ) ) {
          ConsNode sexpNow = sexp;
          while ( !sexpNow.IsAtomNode() ) {
            sexpNow.SetLeft( this.Eval( sexpNow.GetLeft(), false ) );
            sexpNow = sexpNow.GetRight();
          } // while
          
          return sexp;
        } // else if
        else if ( functionName.matches( "#<procedure c[ad]r>" ) ) {
          this.CheckParameterAmount( argumentCount, 1, functionName, false );
          sexp.SetLeft( this.Eval( sexp.GetLeft(), false ) );
          functionName = this.TakeRealFunction( functionName );
          if ( !sexp.GetLeft().IsAtomNode() )
            if ( functionName.matches( "car" ) )
              return sexp.GetLeft().GetLeft();
            else
              return sexp.GetLeft().GetRight();
          else
            throw new SystemMessageException( "IAT", functionName, sexp.GetLeft() );
        } // else if
        else if ( functionName.matches( "#<procedure (pair|list)[?]>" ) ) {
          this.CheckParameterAmount( argumentCount, 1, functionName, false );
          sexp.SetLeft( this.Eval( sexp.GetLeft(), false ) );
          if ( !sexp.GetLeft().IsAtomNode() )
            return this.T();
          else if ( functionName.matches( "#<procedure list[?]>" ) &&
                    ( ( AtomNode ) sexp.GetLeft() ).IsNil() )
            return this.T();
          else
            return this.NIL();
        } // else if
        else if ( functionName.matches( "#<procedure atom[?]>" ) ) {
          this.CheckParameterAmount( argumentCount, 1, functionName, false );
          sexp.SetLeft( this.Eval( sexp.GetLeft(), false ) );
          if ( sexp.GetLeft().IsAtomNode() )
            return this.T();
          else
            return this.NIL();
        } // else if
        else if ( functionName.matches( "#<procedure null[?]>" ) )
          return this.DecideTorNil( sexp.GetLeft(), argumentCount, functionName, DataType.NIL );
        else if ( functionName.matches( "#<procedure integer[?]>" ) )
          return this.DecideTorNil( sexp.GetLeft(), argumentCount, functionName, DataType.INT );
        else if ( functionName.matches( "#<procedure (real|number)[?]>" ) )
          return this.DecideTorNil( sexp.GetLeft(), argumentCount, functionName,
                                    DataType.INT, DataType.FLOAT );
        else if ( functionName.matches( "#<procedure string[?]>" ) )
          return this.DecideTorNil( sexp.GetLeft(), argumentCount, functionName, DataType.STRING );
        else if ( functionName.matches( "#<procedure boolean[?]>" ) )
          return this.DecideTorNil( sexp.GetLeft(), argumentCount, functionName, DataType.T, DataType.NIL );
        else if ( functionName.matches( "#<procedure symbol[?]>" ) )
          return this.DecideTorNil( sexp.GetLeft(), argumentCount, functionName, DataType.SYMBOL );
        else if ( functionName.matches( "#<procedure not>" ) ) {
          this.CheckParameterAmount( argumentCount, 1, functionName, false );
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
        else if ( functionName.matches( "#<procedure [+]>" ) )
          return this.Arithmetic( sexp, argumentCount, functionName, PLUS );
        else if ( functionName.matches( "#<procedure [-]>" ) )
          return this.Arithmetic( sexp, argumentCount, functionName, SUBTRACT );
        else if ( functionName.matches( "#<procedure [*]>" ) )
          return this.Arithmetic( sexp, argumentCount, functionName, MULTIPLY );
        else if ( functionName.matches( "#<procedure [/]>" ) )
          return this.Arithmetic( sexp, argumentCount, functionName, DIVIDE );
        else if ( functionName.matches( "#<procedure >>" ) )
          return this.Compare( sexp, argumentCount, functionName, MORE );
        else if ( functionName.matches( "#<procedure <>" ) )
          return this.Compare( sexp, argumentCount, functionName, LESS );
        else if ( functionName.matches( "#<procedure =>" ) )
          return this.Compare( sexp, argumentCount, functionName, EQUAL );
        else if ( functionName.matches( "#<procedure >=>" ) )
          return this.Compare( sexp, argumentCount, functionName, MOREEQUAL );
        else if ( functionName.matches( "#<procedure <=>" ) )
          return this.Compare( sexp, argumentCount, functionName, LESSEQUAL );
        else if ( functionName.matches( "#<procedure string>[?]>" ) )
          return this.CompareString( sexp, argumentCount, functionName, MORE );
        else if ( functionName.matches( "#<procedure string<[?]>" ) )
          return this.CompareString( sexp, argumentCount, functionName, LESS );
        else if ( functionName.matches( "#<procedure string=[?]>" ) )
          return this.CompareString( sexp, argumentCount, functionName, EQUAL );
        else if ( functionName.matches( "#<procedure string-append>" ) )
          return this.ComcatString( sexp, argumentCount, functionName );
        else if ( functionName.matches( "#<procedure if>" ) ) {
          try {
            return this.DoIf( sexp, argumentCount, functionName );
          } // try
          catch ( SystemMessageException e ) {
            if ( e.GetSystemCode().matches( "NRV" ) )
              throw new SystemMessageException( "NRV", "", head );
            else
              throw e;
          } // catch
        } // else if
        else if ( functionName.matches( "#<procedure cond>" ) ) {
          try {
            return this.DoCond( sexp, argumentCount, functionName );
          } catch ( SystemMessageException e ) {
            if ( e.GetSystemCode().matches( "EF" ) )
              throw new SystemMessageException( "EF", this.TakeRealFunction( functionName ), head );
            else if ( e.GetSystemCode().matches( "NRV" ) )
              throw new SystemMessageException( "NRV", "", head );
            else
              throw e;
          } // catch
        } // else if
        else if ( functionName.matches( "#<procedure begin>" ) ) {
          ConsNode parameter;
          this.CheckParameterAmount( argumentCount, 1, functionName, true );
          do {
            sexp.SetLeft( this.Eval( sexp.GetLeft(), false ) );
            parameter = sexp.GetLeft();
            sexp = sexp.GetRight();  
          } while ( !sexp.IsAtomNode() );
          return parameter;
        } // else if
        else if ( functionName.matches( "#<procedure and>" ) ) {
          this.CheckParameterAmount( argumentCount, 2, functionName, true );
          sexp.SetLeft( this.Eval( sexp.GetLeft(), false ) );
          ConsNode parameter = sexp.GetLeft();
          if ( parameter.IsAtomNode() && ( ( AtomNode ) parameter ).GetDataType() == DataType.NIL )
            return parameter;
          else {
            while ( !sexp.GetRight().IsAtomNode() )
              sexp = sexp.GetRight();
            return this.Eval( sexp.GetLeft(), false );
          } // else
        } // else if
        else if ( functionName.matches( "#<procedure or>" ) ) {
          this.CheckParameterAmount( argumentCount, 2, functionName, true );
          sexp.SetLeft( this.Eval( sexp.GetLeft(), false ) );
          ConsNode parameter = sexp.GetLeft();
          if ( parameter.IsAtomNode() && ( ( AtomNode ) parameter ).GetDataType() == DataType.NIL ) {
            while ( !sexp.GetRight().IsAtomNode() )
              sexp = sexp.GetRight();
            return this.Eval( sexp.GetLeft(), false );
          } // if
          else
            return parameter;
        } // else if
        else
          throw new SystemMessageException( "AtANF", functionName );
      } // else if
      else if ( function.GetDataType() == DataType.QUOTE )
        return sexp.GetLeft();
      else
        throw new SystemMessageException( "AtANF", functionName );
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
    for ( int i = 0; i < PERMITIVESYMBOLS.length ; i++ ) {
      permitiveSymbol = new AtomNode( new Token( "#<procedure " + PERMITIVESYMBOLS[i] + ">", 0, 0 ),
                                                 DataType.SYMBOL );
      this.mSymbolTable.put( PERMITIVESYMBOLS[i], permitiveSymbol );
    } // for
  } // AddPermitiveSymbol()
  
  private boolean IsPermitiveSymbol( String symbol ) {
    for ( int i = 0; i < PERMITIVESYMBOLS.length ; i++ )
      if ( symbol.compareTo( PERMITIVESYMBOLS[i] ) == 0 )
        return true;
    return false;
  } // IsPermitiveSymbol()
  
  public void PrintErrorFormat( ConsNode head ) {
    AtomNode function = ( AtomNode ) head.GetLeft();
    function.GetAtom().SetData( this.TakeRealFunction( function.GetAtom().GetData() ) );
    this.TreeTravel( head, 0, true, false );
    function.GetAtom().SetData( "#<procedure "+ function.GetAtom().GetData() + ">" );
  } // PrintErrorFormat()
  
  private void CheckParameterAmount( int argumentCount, int argumentAmount,
                                     String functionName, boolean moreThan ) throws SystemMessageException {
    if ( moreThan && argumentCount < argumentAmount )
      throw new SystemMessageException( "INoA", this.TakeRealFunction( functionName ) );
    else if ( !moreThan && argumentCount != argumentAmount )
      throw new SystemMessageException( "INoA", this.TakeRealFunction( functionName ) );
  } // CheckParameterAmount()
  
  private String TakeRealFunction( String function ) {
    function = function.replace( "#<procedure ", "" );
    function = function.replaceAll( ">$", "" );
    return function;
  } // TakeRealFunction()
  
  private boolean IsReal( ConsNode atom ) {
    if ( atom.IsAtomNode() )
      if ( ( ( AtomNode ) atom ).GetDataType() == DataType.INT ||
           ( ( AtomNode ) atom ).GetDataType() == DataType.FLOAT )
        return true;
      else
        return false;
    else
      return false;
  } // IsReal()
  
  private ConsNode Arithmetic( ConsNode sexp, int argumentCount,
                               String functionName, int operator ) throws SystemMessageException {
    float count = 0;
    boolean isIntDivide = false;
    this.CheckParameterAmount( argumentCount, 2, functionName, true );
    sexp.SetLeft( this.Eval( sexp.GetLeft(), false ) );
    if ( this.IsReal( sexp.GetLeft() ) ) {
      AtomNode parameter = ( AtomNode ) sexp.GetLeft();
      isIntDivide = this.IsIntDivide( parameter );
      count = Float.parseFloat(  parameter.GetAtom().GetData() );
      sexp = sexp.GetRight();
      while ( !sexp.IsAtomNode() ) {
        sexp.SetLeft( this.Eval( sexp.GetLeft(), false ) );
        if ( this.IsReal( sexp.GetLeft() ) ) {
          parameter = ( AtomNode ) sexp.GetLeft();
          if ( isIntDivide ) // if one parameter is float, the divide is float divide
            isIntDivide = this.IsIntDivide( parameter );
          
          if ( operator == PLUS )
            count += Float.parseFloat(  parameter.GetAtom().GetData() );
          else if ( operator == SUBTRACT )
            count -= Float.parseFloat(  parameter.GetAtom().GetData() );
          else if ( operator == MULTIPLY )
            count *= Float.parseFloat(  parameter.GetAtom().GetData() );
          else if ( operator == DIVIDE ) {
            if ( Float.parseFloat(  parameter.GetAtom().GetData() ) != 0 ) {
              count /= Float.parseFloat(  parameter.GetAtom().GetData() );
              if ( isIntDivide ) {
                Float integer = new Float( count );
                count = integer.intValue();
              } // if
            } // if
            else
              throw new SystemMessageException( "DbZ" );
          } // else if
        } // if
        else
          throw new SystemMessageException( "IAT", this.TakeRealFunction( functionName ), sexp.GetLeft() );
        sexp = sexp.GetRight();
      } // while
    } // if
    else
      throw new SystemMessageException( "IAT", this.TakeRealFunction( functionName ), sexp.GetLeft() );
    
    if ( isIntDivide )
      return new AtomNode( new Token( String.format( "%.0f", count ), 0, 0 ), DataType.INT );
    else
      return new AtomNode( new Token( String.format( "%.3f", count ), 0, 0 ), DataType.FLOAT );
  } // Arithmetic()
  
  private ConsNode Compare( ConsNode sexp, int argumentCount, String functionName, int operator )
  throws SystemMessageException {
    float comparer = 0;
    float compared = 0;
    boolean inOrder = true; 
    this.CheckParameterAmount( argumentCount, 2, functionName, true );
    sexp.SetLeft( this.Eval( sexp.GetLeft(), false ) );
    if ( this.IsReal( sexp.GetLeft() ) ) {
      AtomNode parameter = ( AtomNode ) sexp.GetLeft();
      comparer = Float.parseFloat(  parameter.GetAtom().GetData() );
      sexp = sexp.GetRight();
      while ( !sexp.IsAtomNode() ) {
        sexp.SetLeft( this.Eval( sexp.GetLeft(), false ) );
        if ( this.IsReal( sexp.GetLeft() ) ) {
          parameter = ( AtomNode ) sexp.GetLeft();
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
        else
          throw new SystemMessageException( "IAT", this.TakeRealFunction( functionName ), sexp.GetLeft() );
        sexp = sexp.GetRight();              
      } // while
    } // if
    else
      throw new SystemMessageException( "IAT", this.TakeRealFunction( functionName ), sexp.GetLeft() );
    
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
  
  private ConsNode CompareString( ConsNode sexp, int argumentCount, String functionName, int operator )
  throws SystemMessageException {
    String compareString;
    String nextString;
    boolean inOrder = true;
    this.CheckParameterAmount( argumentCount, 2, functionName, true );
    sexp.SetLeft( this.Eval( sexp.GetLeft(), false ) );
    if ( sexp.GetLeft().IsAtomNode() && ( ( AtomNode ) sexp.GetLeft() ).GetDataType() == DataType.STRING ) {
      AtomNode parameter = ( AtomNode ) sexp.GetLeft();
      compareString = parameter.GetAtom().GetData();
      sexp = sexp.GetRight();
      while ( !sexp.IsAtomNode() ) {
        sexp.SetLeft( this.Eval( sexp.GetLeft(), false ) );
        if ( sexp.GetLeft().IsAtomNode() &&
             ( ( AtomNode ) sexp.GetLeft() ).GetDataType() == DataType.STRING ) {
          parameter = ( AtomNode ) sexp.GetLeft();
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
        else
          throw new SystemMessageException( "IAT", this.TakeRealFunction( functionName ), sexp.GetLeft() );
        
        sexp = sexp.GetRight();              
      } // while
    } // if
    else
      throw new SystemMessageException( "IAT", this.TakeRealFunction( functionName ), sexp.GetLeft() );
    
    if ( inOrder )
      return this.T();
    else
      return this.NIL();
  } // CompareString()
  
  private AtomNode CompareVeecor( ConsNode sexp ) throws SystemMessageException {
    ConsNode leftParameter, rightParameter;
    sexp.SetLeft( this.Eval( sexp.GetLeft(), false ) );
    leftParameter = sexp.GetLeft();
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
  } // CompareVeecor()
  
  private AtomNode Equal( ConsNode sexp ) throws SystemMessageException {
    ConsNode leftParameter, rightParameter;
    sexp.SetLeft( this.Eval( sexp.GetLeft(), false ) );
    leftParameter = sexp.GetLeft();
    sexp = sexp.GetRight();
    sexp.SetLeft( this.Eval( sexp.GetLeft(), false ) );
    rightParameter = sexp.GetLeft();
    return this.TreeCompare( leftParameter, rightParameter );
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
  
  private ConsNode ComcatString( ConsNode sexp, int argumentCount, String functionName )
  throws SystemMessageException {
    String allString = "\"";
    String comcatingString;
    AtomNode parameter;
    this.CheckParameterAmount( argumentCount, 2, functionName, true );
    while ( !sexp.IsAtomNode() ) {
      sexp.SetLeft( this.Eval( sexp.GetLeft(), false ) );
      if ( sexp.GetLeft().IsAtomNode() &&
           ( ( AtomNode ) sexp.GetLeft() ).GetDataType() == DataType.STRING ) {
        parameter = ( AtomNode ) sexp.GetLeft();
        comcatingString = parameter.GetAtom().GetData();
        allString += comcatingString.substring( 1, comcatingString.length() - 1 );
      } // if
      else
        throw new SystemMessageException( "IAT", this.TakeRealFunction( functionName ), sexp.GetLeft() );
      sexp = sexp.GetRight();
    } // while
    
    return new AtomNode( new Token( allString + "\"", 0, 0 ), DataType.STRING );
  } // ComcatString()
  
  private ConsNode DecideTorNil( ConsNode parameter, int argumentCount, String functionName, int dataType )
  throws SystemMessageException {
    this.CheckParameterAmount( argumentCount, 1, functionName, false );
    parameter = this.Eval( parameter, false );
    if ( parameter.IsAtomNode() )
      if ( ( ( AtomNode ) parameter ).GetDataType() == dataType )
        if ( dataType == DataType.SYMBOL && !this.IsPermitiveSymbol( parameter.ToString() ) &&
             this.IsPermitiveSymbol( this.TakeRealFunction( parameter.ToString() ) ) )
          return this.NIL();
        else
          return this.T();
      else
        return this.NIL();
    else
      return this.NIL();
  } // DecideTorNil()
  
  private ConsNode DecideTorNil( ConsNode sexp, int argumentCount, String functionName,
                                 int dataType1, int dataType2 ) throws SystemMessageException {
    if ( ! ( ( AtomNode ) this.DecideTorNil( sexp, argumentCount, functionName, dataType1 ) ).IsNil() ||
         ! ( ( AtomNode ) this.DecideTorNil( sexp, argumentCount, functionName, dataType2 ) ).IsNil() )
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
          throw new SystemMessageException( "NRV" );
      else if ( ( ( AtomNode ) condition ).GetDataType() != DataType.NIL )
        return this.Eval( Tpart, false );
      else
        throw new SystemMessageException( "NRV" );
    else
      return this.Eval( Tpart, false );
  } // MakeDecision()
  
  private ConsNode DoIf( ConsNode sexp, int argumentCount, String functionName )
  throws SystemMessageException {
    try {
      this.CheckParameterAmount( argumentCount, 2, functionName, false );
    } // try
    catch ( SystemMessageException e ) {
      this.CheckParameterAmount( argumentCount, 3, functionName, false );
    } // catch
    sexp.SetLeft( this.Eval( sexp.GetLeft(), false ) );
    return this.MakeDecision( sexp.GetLeft(), sexp.GetRight().GetLeft(),
                              sexp.GetRight().GetRight().GetLeft(), false );
  } // DoIf()
  
  private ConsNode DoCond( ConsNode sexp, int argumentCount, String functionName )
  throws SystemMessageException {
    AtomNode condition;
    ConsNode checkDecisionType;
    try {
      this.CheckParameterAmount( argumentCount, 1, functionName, true );
    } // try
    catch ( SystemMessageException e ) {
      throw new SystemMessageException( "EF" );
    } // catch
    if ( !sexp.GetLeft().IsAtomNode() ) {
      checkDecisionType = null;
      while ( checkDecisionType == null && !sexp.GetRight().IsAtomNode() ) {       
        checkDecisionType = this.ParseCond( sexp.GetLeft() );
        if ( checkDecisionType == null )
          sexp = sexp.GetRight();
      } // while
      
      if ( checkDecisionType == null )
        if ( !sexp.GetLeft().IsAtomNode() ) {
          if ( sexp.GetLeft().GetLeft().IsAtomNode() ) {
            condition = ( AtomNode ) sexp.GetLeft().GetLeft();
            if ( condition.GetAtom().GetData().matches( "else" ) && argumentCount != 0 )
              condition.SetDataType( DataType.T );
          } // if
          
          checkDecisionType = this.ParseCond( sexp.GetLeft() );
        } // if
      
      return checkDecisionType;
    } // if
    else
      throw new SystemMessageException( "EF" );
  } // DoCond()
  
  private ConsNode ParseCond( ConsNode sexp ) throws SystemMessageException {
    int count = 0;
    ConsNode condition = sexp.GetLeft();
    sexp.SetLeft( this.Eval( sexp.GetLeft(), false ) );
    while ( !sexp.GetRight().IsAtomNode() ) {
      sexp = sexp.GetRight();
      count++;
    } // while
    
    if ( count == 0 )
      throw new SystemMessageException( "EF" );  
    return this.MakeDecision( condition, sexp.GetLeft(), DataType.NULL, true );
  } // ParseCond()
  
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
