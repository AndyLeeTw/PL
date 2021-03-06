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
      "equal?", "begin", "if", "cond", "list?", "atom?", "let", "lambda" };
  private ConsNode mTransTyper;
  private LinkedHashMap<String, ConsNode>mSymbolTable = new LinkedHashMap<String, ConsNode>();
  private LinkedHashMap<String, ConsNode>mFunctionTable;
  private ValueStack mLocalVar;
  
  public TreeBuilder() {
    this.AddPermitiveSymbol();
    this.mLocalVar = new ValueStack();
    this.mFunctionTable = new LinkedHashMap<String, ConsNode>();
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
          head.GetRight().SetLeft( TreeConStruct( DataType.NULL, tokens, Getter ) );
          ConsNode tTr = new AtomNode( 0, 0 );
          head.GetRight().SetRight( tTr );
        } // if
        else if ( aToken.GetData().matches( "[\\.)]" ) )
          throw new SystemMessageException( "UTL", aToken.GetData(), aToken.GetLine(),
                                            aToken.GetColumn() );
        else {
          if ( aToken.GetData().matches( "quote" ) )
            head = new AtomNode( aToken, DataType.QUOTE );
          else if ( aToken.GetData().matches( "nil" ) || aToken.GetData().matches( "#f" ) )
            head = new AtomNode( aToken.GetLine(), aToken.GetColumn() );
          else if ( aToken.GetData().matches( "^[+-]?\\d+$" ) )
            head = new AtomNode( aToken, DataType.INT );
          else if ( aToken.GetData().matches( "^[+-]?(((0-9)*\\.[0-9]+)|([0-9]+\\.[0-9]*))$" ) )
            head = new AtomNode( aToken, DataType.FLOAT );
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
  
  public ConsNode Eval( ConsNode head, boolean isTop, boolean isInScope ) throws SystemMessageException {
    if ( head.IsAtomNode() ) {
      if ( ( ( AtomNode ) head ).GetDataType() != DataType.SYMBOL )
        return head;
      else {
        String key = ( ( AtomNode ) head ).GetAtom().GetData();
        if ( isInScope ) {
          ConsNode varValue;
          varValue = this.mLocalVar.GetLocalValue( key );
          if ( varValue != null )
            return varValue;
        } // if
        
        if ( this.mSymbolTable.containsKey( key ) )
          return this.mSymbolTable.get( key );
        else
          throw new SystemMessageException( "US", key );
      } // else
    } // if
    else {
      ConsNode functionNode =  this.Eval( head.GetLeft(), isTop, isInScope );
      if ( functionNode.IsAtomNode() &&
           ( ( AtomNode ) functionNode ).GetDataType() == DataType.SYMBOL ) {
        AtomNode function = ( AtomNode ) functionNode;
        ConsNode sexp = head.GetRight();
        String functionName = function.GetAtom().GetData();
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
              this.mFunctionTable.clear();
              this.AddPermitiveSymbol();
              throw new SystemMessageException( "EC" );
            } // else
          } // if
          else { // #<procedure define>
            try {
              this.Define( sexp, argumentCount );
            } catch( SystemMessageException e ) {
              if ( e.GetSystemCode().matches( "EF" ) && e.GetHead() == null )
                throw new SystemMessageException( "EF", "DEFINE", head );
              else
                throw e;
            } // catch
            
            return null; // impossible reach here
          } // else
        } // if
        else if ( functionName.matches( "#<procedure (cons|eqv[?]|equal[?])>" ) ) {
          this.CheckParameterAmount( argumentCount, 2, functionName, false );     
          if ( functionName.matches( "#<procedure eqv[?]>" ) )
            return this.CompareVeecor( sexp, isInScope );
          else if ( functionName.matches( "#<procedure equal[?]>" ) )
            return this.Equal( sexp, isInScope );
          else {
            sexp.SetLeft( this.Eval( sexp.GetLeft(), false, isInScope ) );
            sexp.SetRight( this.Eval( sexp.GetRight().GetLeft(), false, isInScope ) );
            return sexp;
          } // else
        } // else if
        else if ( functionName.matches( "#<procedure list>" ) ) {
          ConsNode sexpNow = sexp;
          while ( !sexpNow.IsAtomNode() ) {
            sexpNow.SetLeft( this.Eval( sexpNow.GetLeft(), false, isInScope ) );
            sexpNow = sexpNow.GetRight();
          } // while
          
          return sexp;
        } // else if
        else if ( functionName.matches( "#<procedure c[ad]r>" ) ) {
          this.CheckParameterAmount( argumentCount, 1, functionName, false );
          sexp.SetLeft( this.Eval( sexp.GetLeft(), false, isInScope ) );
          if ( !sexp.GetLeft().IsAtomNode() )
            if ( functionName.matches( "#<procedure car>" ) )
              return sexp.GetLeft().GetLeft();
            else
              return sexp.GetLeft().GetRight();
          else
            throw new SystemMessageException( "IAT", this.TakeRealFunction( functionName ), sexp.GetLeft() );
        } // else if
        else if ( functionName.matches( "#<procedure (pair|list)[?]>" ) ) {
          this.CheckParameterAmount( argumentCount, 1, functionName, false );
          sexp.SetLeft( this.Eval( sexp.GetLeft(), false, isInScope ) );
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
          sexp.SetLeft( this.Eval( sexp.GetLeft(), false, isInScope ) );
          if ( sexp.GetLeft().IsAtomNode() )
            return this.T();
          else
            return this.NIL();
        } // else if
        else if ( functionName.matches( "#<procedure null[?]>" ) )
          return this.DecideTorNil( sexp.GetLeft(), argumentCount, functionName, DataType.NIL, isInScope );
        else if ( functionName.matches( "#<procedure integer[?]>" ) )
          return this.DecideTorNil( sexp.GetLeft(), argumentCount, functionName, DataType.INT, isInScope );
        else if ( functionName.matches( "#<procedure (real|number)[?]>" ) )
          return this.DecideTorNil( sexp.GetLeft(), argumentCount, functionName,
                                    DataType.INT, DataType.FLOAT, isInScope );
        else if ( functionName.matches( "#<procedure string[?]>" ) )
          return this.DecideTorNil( sexp.GetLeft(), argumentCount, functionName,
                                    DataType.STRING, isInScope );
        else if ( functionName.matches( "#<procedure boolean[?]>" ) )
          return this.DecideTorNil( sexp.GetLeft(), argumentCount, functionName, DataType.T,
                                    DataType.NIL, isInScope );
        else if ( functionName.matches( "#<procedure symbol[?]>" ) )
          return this.DecideTorNil( sexp.GetLeft(), argumentCount, functionName,
                                    DataType.SYMBOL, isInScope );
        else if ( functionName.matches( "#<procedure not>" ) ) {
          this.CheckParameterAmount( argumentCount, 1, functionName, false );
          sexp.SetLeft( this.Eval( sexp.GetLeft(), false, isInScope ) );
          if ( sexp.GetLeft().IsAtomNode() && ( ( AtomNode ) sexp.GetLeft() ).IsNil() )
            return this.T();
          else
            return this.NIL();
        } // else if
        else if ( functionName.matches( "#<procedure [+]>" ) )
          return this.Arithmetic( sexp, argumentCount, functionName, PLUS, isInScope );
        else if ( functionName.matches( "#<procedure [-]>" ) )
          return this.Arithmetic( sexp, argumentCount, functionName, SUBTRACT, isInScope );
        else if ( functionName.matches( "#<procedure [*]>" ) )
          return this.Arithmetic( sexp, argumentCount, functionName, MULTIPLY, isInScope );
        else if ( functionName.matches( "#<procedure [/]>" ) )
          return this.Arithmetic( sexp, argumentCount, functionName, DIVIDE, isInScope );
        else if ( functionName.matches( "#<procedure >>" ) )
          return this.Compare( sexp, argumentCount, functionName, MORE, isInScope );
        else if ( functionName.matches( "#<procedure <>" ) )
          return this.Compare( sexp, argumentCount, functionName, LESS, isInScope );
        else if ( functionName.matches( "#<procedure =>" ) )
          return this.Compare( sexp, argumentCount, functionName, EQUAL, isInScope );
        else if ( functionName.matches( "#<procedure >=>" ) )
          return this.Compare( sexp, argumentCount, functionName, MOREEQUAL, isInScope );
        else if ( functionName.matches( "#<procedure <=>" ) )
          return this.Compare( sexp, argumentCount, functionName, LESSEQUAL, isInScope );
        else if ( functionName.matches( "#<procedure string>[?]>" ) )
          return this.CompareString( sexp, argumentCount, functionName, MORE, isInScope );
        else if ( functionName.matches( "#<procedure string<[?]>" ) )
          return this.CompareString( sexp, argumentCount, functionName, LESS, isInScope );
        else if ( functionName.matches( "#<procedure string=[?]>" ) )
          return this.CompareString( sexp, argumentCount, functionName, EQUAL, isInScope );
        else if ( functionName.matches( "#<procedure string-append>" ) )
          return this.ComcatString( sexp, argumentCount, functionName, isInScope );
        else if ( functionName.matches( "#<procedure if>" ) ) {
          try {
            return this.DoIf( sexp, argumentCount, functionName, isInScope );
          } catch ( SystemMessageException e ) {
            if ( e.GetSystemCode().matches( "NRV" ) )
              throw new SystemMessageException( "NRV", "", head );
            else
              throw e;
          } // catch
        } // else if
        else if ( functionName.matches( "#<procedure cond>" ) ) {
          try {
            this.ParseCond( sexp, argumentCount, functionName, isInScope );
            return this.DoCond( sexp, argumentCount, isInScope );
          } catch ( SystemMessageException e ) {
            if ( e.GetHead() == null )
              if ( e.GetSystemCode().matches( "EF" ) )
                throw new SystemMessageException( "EF", this.TakeRealFunction( functionName ), head );
              else if ( e.GetSystemCode().matches( "NRV" ) )
                throw new SystemMessageException( "NRV", "", head );
              else
                throw e;
            else
              throw e;
          } // catch
        } // else if
        else if ( functionName.matches( "#<procedure begin>" ) ) {
          ConsNode parameter;
          this.CheckParameterAmount( argumentCount, 1, functionName, true );
          do {
            sexp.SetLeft( this.Eval( sexp.GetLeft(), false, isInScope ) );
            parameter = sexp.GetLeft();
            sexp = sexp.GetRight();  
          } while ( !sexp.IsAtomNode() );
          return parameter;
        } // else if
        else if ( functionName.matches( "#<procedure and>" ) ) {
          this.CheckParameterAmount( argumentCount, 2, functionName, true );
          ConsNode parameter;
          while ( !sexp.GetRight().IsAtomNode() ) {
            parameter = this.Eval( sexp.GetLeft(), false, isInScope );
            if ( parameter.IsAtomNode() && ( ( AtomNode ) parameter ).GetDataType() == DataType.NIL )
              return parameter;
            sexp = sexp.GetRight();
          } // while
          
          return this.Eval( sexp.GetLeft(), false, isInScope );
        } // else if
        else if ( functionName.matches( "#<procedure or>" ) ) {
          this.CheckParameterAmount( argumentCount, 2, functionName, true );
          ConsNode parameter;
          while ( !sexp.GetRight().IsAtomNode() ) {
            parameter = this.Eval( sexp.GetLeft(), false, isInScope );
            if ( parameter.IsAtomNode() && ( ( AtomNode ) parameter ).GetDataType() != DataType.NIL )
              return parameter;
            sexp = sexp.GetRight();
          } // while
          
          return this.Eval( sexp.GetLeft(), false, isInScope );
        } // else if
        else if ( functionName.matches( "#<procedure let>" ) ) {
          try {
            return this.Let( sexp, argumentCount, isInScope );
          } catch ( SystemMessageException e ) {
            if ( e.GetSystemCode().matches( "EF" ) && e.GetHead() == null )
              throw new SystemMessageException( "EF", this.TakeRealFunction( functionName ), head );
            else
              throw e;
          } // catch
        } // else if
        else if ( functionName.matches( "#<procedure lambda>" ) ) {
          if ( head.GetLeft().ToString().matches( "lambda" ) ) {
            ConsNode keyNode;
            try {
              this.CheckParameterAmount( argumentCount, 2, functionName, true );
              keyNode = sexp.GetLeft();
              this.CheckFunctionFormat( keyNode );
            } catch ( SystemMessageException e ) {
              throw new SystemMessageException( "EF", "lambda", head );
            } // catch
            
            ConsNode sexpNow = sexp.GetRight();
            while ( !sexpNow.IsAtomNode() ) {
              if ( !sexpNow.GetLeft().IsAtomNode() || !this.IsReal( sexpNow.GetLeft() ) )
              sexpNow = sexpNow.GetRight();
            } // while
            
            this.mFunctionTable.put( function.ToString(), sexp );
            return function;
          } // if
          else {
            ConsNode interFunction;
            if ( head.GetLeft().IsAtomNode() )
              interFunction = this.mFunctionTable.get( head.GetLeft().ToString() );
            else
              interFunction = this.mFunctionTable.get( functionName );
            ConsNode argumentNode = interFunction.GetLeft();
            ConsNode sexpNow = sexp;
            this.CheckCustomFunction( argumentNode, sexpNow, functionName );
            return this.DoCustomFunction( interFunction, argumentNode, sexpNow, argumentCount, isInScope );
          } // else
        } // else if
        else if ( functionName.matches( "#<procedure .*>" ) ) {
          ConsNode interFunction = this.mFunctionTable.get( functionName );
          ConsNode argumentNode = interFunction.GetLeft();
          ConsNode sexpNow = sexp;
          this.CheckCustomFunction( argumentNode, sexpNow, functionName );
          return this.DoCustomFunction( interFunction, argumentNode, sexpNow, argumentCount, isInScope );
        } // else if
        else
          throw new SystemMessageException( "AtANF", "", head.GetLeft() );
      } // else if
      else if ( functionNode.IsAtomNode() &&
                ( ( AtomNode ) functionNode ).GetDataType() == DataType.QUOTE )
        return head.GetRight().GetLeft();
      else
        throw new SystemMessageException( "AtANF", "", head.GetLeft() );
    } // else
  } // Eval()
  
  private AtomNode T() {
    return new AtomNode( new Token( "#t", 0, 0 ), DataType.T );
  } // T()

  private AtomNode NIL() {
    return new AtomNode( 0, 0 );
  } // NIL()
  
  public void LocalValueClear() {
    this.mLocalVar.Clear();
  } // LocalValueClear()
  
  private ConsNode Clone( ConsNode head ) {
    ConsNode aNode;
    if ( head.IsAtomNode() ) {
      AtomNode tTer = ( AtomNode ) head;
      return new AtomNode( tTer.GetAtom(), tTer.GetDataType() );
    } // if
    else {
      aNode = new ConsNode();
      aNode.SetLeft( this.Clone( head.GetLeft() ) );
      aNode.SetRight( this.Clone( head.GetRight() ) );
      return aNode;
    } // else
  } // Clone()
  
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
    function = function.replaceAll( "^#<procedure ", "" );
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
  
  private ConsNode Arithmetic( ConsNode sexp, int argumentCount, String functionName,
                               int operator, boolean isInScope ) throws SystemMessageException {
    float count = 0;
    boolean isIntDivide;
    ConsNode sexpNow = sexp;
    this.CheckParameterAmount( argumentCount, 2, functionName, true );
    while ( !sexpNow.IsAtomNode() ) {
      sexpNow.SetLeft( this.Eval( sexpNow.GetLeft(), false, isInScope ) );
      if ( !this.IsReal( sexpNow.GetLeft() ) )
        throw new SystemMessageException( "IAT", this.TakeRealFunction( functionName ), sexpNow.GetLeft() );
      sexpNow = sexpNow.GetRight();
    } // while
    
    AtomNode parameter = ( AtomNode ) sexp.GetLeft();
    isIntDivide = this.IsIntDivide( parameter );
    count = parameter.GetVaule();
    sexp = sexp.GetRight();
    while ( !sexp.IsAtomNode() ) {
      parameter = ( AtomNode ) sexp.GetLeft();
      if ( isIntDivide ) // if one parameter is float, the divide is float divide
        isIntDivide = this.IsIntDivide( parameter );
      
      if ( operator == PLUS )
        count += parameter.GetVaule();
      else if ( operator == SUBTRACT )
        count -= parameter.GetVaule();
      else if ( operator == MULTIPLY )
        count *= parameter.GetVaule();
      else if ( operator == DIVIDE ) {
        if ( Float.parseFloat(  parameter.GetAtom().GetData() ) != 0 ) {
          count /= parameter.GetVaule();
          if ( isIntDivide ) {
            Float integer = new Float( count );
            count = integer.intValue();
          } // if
        } // if
        else
          throw new SystemMessageException( "DbZ" );
      } // else if
      
      sexp = sexp.GetRight();
    } // while
    
    if ( isIntDivide )
      return new AtomNode( new Token( String.format( "%f", count ), 0, 0 ), DataType.INT );
    else 
      return new AtomNode( new Token( String.format( "%f", count ), 0, 0 ), DataType.FLOAT );
  } // Arithmetic()
  
  private ConsNode Compare( ConsNode sexp, int argumentCount, String functionName,
                            int operator, boolean isInScope ) throws SystemMessageException {
    float comparer = 0;
    float compared = 0;
    boolean inOrder = true; 
    this.CheckParameterAmount( argumentCount, 2, functionName, true );
    sexp.SetLeft( this.Eval( sexp.GetLeft(), false, isInScope ) );
    if ( this.IsReal( sexp.GetLeft() ) ) {
      AtomNode parameter = ( AtomNode ) sexp.GetLeft();
      comparer = parameter.GetVaule();
      sexp = sexp.GetRight();
      while ( !sexp.IsAtomNode() ) {
        sexp.SetLeft( this.Eval( sexp.GetLeft(), false, isInScope ) );
        if ( this.IsReal( sexp.GetLeft() ) ) {
          parameter = ( AtomNode ) sexp.GetLeft();
          compared = parameter.GetVaule();
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
  
  private ConsNode CompareString( ConsNode sexp, int argumentCount, String functionName,
                                  int operator, boolean isInScope ) throws SystemMessageException {
    String compareString;
    String nextString;
    boolean inOrder = true;
    this.CheckParameterAmount( argumentCount, 2, functionName, true );
    sexp.SetLeft( this.Eval( sexp.GetLeft(), false, isInScope ) );
    if ( sexp.GetLeft().IsAtomNode() && ( ( AtomNode ) sexp.GetLeft() ).GetDataType() == DataType.STRING ) {
      AtomNode parameter = ( AtomNode ) sexp.GetLeft();
      compareString = parameter.GetAtom().GetData();
      sexp = sexp.GetRight();
      while ( !sexp.IsAtomNode() ) {
        sexp.SetLeft( this.Eval( sexp.GetLeft(), false, isInScope ) );
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
  
  private AtomNode CompareVeecor( ConsNode sexp, boolean isInScope ) throws SystemMessageException {
    ConsNode leftParameter, rightParameter;
    sexp.SetLeft( this.Eval( sexp.GetLeft(), false, isInScope ) );
    leftParameter = sexp.GetLeft();
    sexp = sexp.GetRight();
    sexp.SetLeft( this.Eval( sexp.GetLeft(), false, isInScope ) );
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
  
  private AtomNode Equal( ConsNode sexp, boolean isInScope ) throws SystemMessageException {
    ConsNode leftParameter, rightParameter;
    sexp.SetLeft( this.Eval( sexp.GetLeft(), false, isInScope ) );
    leftParameter = sexp.GetLeft();
    sexp = sexp.GetRight();
    sexp.SetLeft( this.Eval( sexp.GetLeft(), false, isInScope ) );
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
      AtomNode leftResult = this.TreeCompare( treeA.GetLeft(), treeB.GetLeft() );
      AtomNode rightResult = this.TreeCompare( treeA.GetRight(), treeB.GetRight() );
      if ( leftResult.IsNil() || rightResult.IsNil() )
        return this.NIL();
      else
        return this.T();
    } // else if
    else
      return this.NIL();
  } // TreeCompare()
  
  private ConsNode ComcatString( ConsNode sexp, int argumentCount, String functionName, boolean isInScope )
  throws SystemMessageException {
    String allString = "\"";
    String comcatingString;
    AtomNode parameter;
    this.CheckParameterAmount( argumentCount, 2, functionName, true );
    while ( !sexp.IsAtomNode() ) {
      sexp.SetLeft( this.Eval( sexp.GetLeft(), false, isInScope ) );
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

  private ConsNode DecideTorNil( ConsNode parameter, int argumentCount, String functionName, int dataType,
                                 boolean isInScope ) throws SystemMessageException {
    this.CheckParameterAmount( argumentCount, 1, functionName, false );
    parameter = this.Eval( parameter, false, isInScope );
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
  
  private ConsNode DecideTorNil( ConsNode sexp, int argumentCount, String functionName, int dataType1,
                                 int dataType2, boolean isInScope ) throws SystemMessageException {
    if ( ! ( ( AtomNode ) this.DecideTorNil( sexp, argumentCount, functionName,
                                             dataType1, isInScope ) ).IsNil() ||
         ! ( ( AtomNode ) this.DecideTorNil( sexp, argumentCount, functionName,
                                             dataType2, isInScope ) ).IsNil() )
      return this.T();
    else
      return this.NIL();
  } // DecideTorNil()
  
  private ConsNode MakeDecision( ConsNode condition, ConsNode Tpart, ConsNode NILpart, boolean isInScope )
  throws SystemMessageException {
    if ( condition.IsAtomNode() )
      if ( ( ( AtomNode ) condition ).GetDataType() == DataType.NIL )
        if ( NILpart != null )
          return this.Eval( NILpart, false, isInScope );
        else
          throw new SystemMessageException( "NRV", "", DataType.NULL );
      else
        return this.Eval( Tpart, false, isInScope );
    else
      return this.Eval( Tpart, false, isInScope );
  } // MakeDecision()
  
  private ConsNode DoIf( ConsNode sexp, int argumentCount, String functionName, boolean isInScope )
  throws SystemMessageException {
    try {
      this.CheckParameterAmount( argumentCount, 2, functionName, false );
    } // try
    catch ( SystemMessageException e ) {
      this.CheckParameterAmount( argumentCount, 3, functionName, false );
    } // catch
    ConsNode condition = this.Clone( sexp.GetLeft() );
    return this.MakeDecision( this.Eval( condition, false, isInScope ), 
                              sexp.GetRight().GetLeft(), sexp.GetRight().GetRight().GetLeft(), isInScope );
  } // DoIf()
  
  private void ParseCond( ConsNode sexp, int argumentCount, String functionName, boolean isInScope )
  throws SystemMessageException {
    try {
      ConsNode sexpNow = sexp;
      this.CheckParameterAmount( argumentCount, 1, functionName, true );
      while ( !sexpNow.IsAtomNode() ) {
        if ( sexpNow.GetLeft().IsAtomNode() )
          throw new SystemMessageException( "EF", "", DataType.NULL );
        else {
          ConsNode conditionNow;
          int count = 0;
          conditionNow = sexpNow.GetLeft();
          while ( !conditionNow.IsAtomNode() ) {
            count++;
            conditionNow = conditionNow.GetRight();
          } // while
          
          if ( count < 2 )
            throw new SystemMessageException( "EF", "", DataType.NULL );
        } // else
        
        sexpNow = sexpNow.GetRight();
      } // while
    } // try
    catch ( SystemMessageException e ) {
      throw new SystemMessageException( "EF", "", DataType.NULL );
    } // catch
  } // ParseCond()
  
  private ConsNode DoCond( ConsNode sexp, int argumentCount, boolean isInScope )
  throws SystemMessageException {
    ConsNode checkDecisionType = null;
    AtomNode condition;
    while ( !sexp.GetRight().IsAtomNode() ) {
      if ( checkDecisionType == null )
        checkDecisionType = this.ExecuteCond( sexp.GetLeft(), isInScope );
      sexp = sexp.GetRight();
    } // while
    
    if ( sexp.GetLeft().GetLeft().IsAtomNode() ) {
      condition = ( AtomNode ) sexp.GetLeft().GetLeft();
      if ( condition.GetAtom().GetData().matches( "else" ) && argumentCount != 1 )
        condition.SetDataType( DataType.T );
    } // if
    
    if ( checkDecisionType == null )
      checkDecisionType = this.ExecuteCond( sexp.GetLeft(), isInScope );
    
    if ( checkDecisionType == null )
      throw new SystemMessageException( "NRV", "", DataType.NULL );
    
    return checkDecisionType;
  } // DoCond()
  
  private ConsNode ExecuteCond( ConsNode sexp, boolean isInScope ) throws SystemMessageException {
    ConsNode condition = this.Eval( this.Clone( sexp.GetLeft() ), false, isInScope );
    if ( !condition.IsAtomNode() || ( condition.IsAtomNode() && ! ( ( AtomNode ) condition ).IsNil() ) ) {
      while ( !sexp.GetRight().IsAtomNode() ) {
        sexp.SetLeft( this.Eval( sexp.GetLeft(), false, isInScope ) );
        sexp = sexp.GetRight();
      } // while
      
      return this.Eval( sexp.GetLeft(), false, isInScope );
    } // if
    else
      return null;
  } // ExecuteCond()
  
  private ConsNode Let( ConsNode sexp, int argumentCount, boolean isInScope ) throws SystemMessageException {
    try {
      this.CheckParameterAmount( argumentCount, 2, "#<procedure let>", true );
    } catch ( SystemMessageException e ) {
      throw new SystemMessageException( "EF", "", DataType.NULL );
    } // catch
    
    int count = 0;
    if ( !sexp.GetLeft().IsAtomNode() ) {
      count = 1;
      ConsNode sexpFirstNow = sexp.GetLeft();
      while ( !sexpFirstNow.IsAtomNode() ) {
        if ( !sexpFirstNow.GetLeft().IsAtomNode() ) {
          if ( ( ( AtomNode ) sexpFirstNow.GetLeft().GetLeft() ).GetDataType() != DataType.SYMBOL )
            throw new SystemMessageException( "EF", "", DataType.NULL );
        } // if
        else  
          throw new SystemMessageException( "EF", "", DataType.NULL );
        ConsNode sexpInFirstNow = sexpFirstNow.GetLeft().GetRight();
        while ( !sexpInFirstNow.IsAtomNode() ) {
          count++;
          sexpInFirstNow = sexpInFirstNow.GetRight();
        } // while
        
        if ( ! ( ( AtomNode ) sexpInFirstNow ).IsNil() || count != 2 )
          throw new SystemMessageException( "EF", "", DataType.NULL );
        count = 1;
        sexpFirstNow = sexpFirstNow.GetRight(); 
      } // while
      
      if ( ! ( ( AtomNode ) sexpFirstNow ).IsNil() )
        throw new SystemMessageException( "EF", "", DataType.NULL );    
      sexpFirstNow = sexp.GetLeft();
      count = 0;
      while ( !sexpFirstNow.IsAtomNode() ) {
        String variable = ( ( AtomNode ) sexpFirstNow.GetLeft().GetLeft() ).ToString();
        ConsNode aNode = this.Eval( sexpFirstNow.GetLeft().GetRight().GetLeft(), false, isInScope );
        VarNode vn = new VarNode( variable, aNode );
        this.mLocalVar.Push( vn );
        sexpFirstNow = sexpFirstNow.GetRight(); 
        count++;
      } // while
    } // if
    else if ( ( ( AtomNode ) sexp.GetLeft() ).IsNil() ) ;
    else
      throw new SystemMessageException( "EF", "", DataType.NULL );
    
    sexp = sexp.GetRight();
    ConsNode parameter;
    do {
      parameter = this.Eval( sexp.GetLeft(), false, true );
      sexp = sexp.GetRight();
    } while ( !sexp.IsAtomNode() ) ;
    for ( int i = 0 ; i < count ; i++ )
      this.mLocalVar.Pop();
    return parameter;
  } // Let()
  
  private void Define( ConsNode sexp, int argumentCount ) throws SystemMessageException {
    try {
      this.CheckParameterAmount( argumentCount, 2, "DEFINE", false );
    } catch ( SystemMessageException e ) {
      throw new SystemMessageException( "EF", "", DataType.NULL );
    } // catch
    if ( sexp.GetLeft().IsAtomNode() ) {
      this.DoSymbolDefine( sexp.GetLeft(), sexp.GetRight().GetLeft() );
    } // if
    else {
      ConsNode keyNode = sexp.GetLeft();
      ConsNode valueNode;
      this.CheckFunctionFormat( keyNode );
      valueNode = sexp;
      this.DoFunctionDefine( keyNode, valueNode );
      
      throw new SystemMessageException( "DEFINE" );
    } // else
  } // Define()
  
  private void CheckFunctionFormat( ConsNode keyNode ) throws SystemMessageException {
    while ( !keyNode.IsAtomNode() ) {
      if ( keyNode.GetLeft().IsAtomNode() &&
           ( ( AtomNode ) keyNode.GetLeft() ).GetDataType() == DataType.SYMBOL )
        keyNode = keyNode.GetRight();
      else
        throw new SystemMessageException( "EF", "", DataType.NULL );
    } // while
    
    if ( ! ( ( AtomNode ) keyNode ).IsNil() )
      throw new SystemMessageException( "EF", "", DataType.NULL );
  } // CheckFunctionFormat()
  
  private void DoSymbolDefine( ConsNode keyNode, ConsNode valueNode ) throws SystemMessageException {
    String key = ( ( AtomNode ) keyNode ).GetAtom().GetData();
    if ( ( ( AtomNode ) keyNode ).GetDataType() == DataType.SYMBOL &&
         !this.IsPermitiveSymbol( key ) ) {
      valueNode = this.Eval( valueNode, false, false );
      if ( valueNode.IsAtomNode() && valueNode.ToString().matches( "#<procedure lambda>|#function" ) )
        this.mFunctionTable.put( key, this.mFunctionTable.get( valueNode.ToString() ) );
      this.mSymbolTable.put( key, valueNode );
      throw new SystemMessageException( "DEFINE", key );
    } // if
    else
      throw new SystemMessageException( "EF", "", DataType.NULL );
  } // DoSymbolDefine()
  
  private void DoFunctionDefine( ConsNode keyNode, ConsNode function ) throws SystemMessageException {
    String key = keyNode.GetLeft().ToString();
    this.mSymbolTable.put( key, keyNode.GetLeft() );
    ( ( AtomNode ) keyNode.GetLeft() ).GetAtom().SetData( "#<procedure "  + key + ">" );
    function.SetLeft( function.GetLeft().GetRight() );   
    this.mFunctionTable.put( keyNode.GetLeft().ToString(), function );
    throw new SystemMessageException( "DEFINE", key );
  } // DoFunctionDefine()
  
  private ConsNode DoCustomFunction( ConsNode interFunction, ConsNode argumentNode, ConsNode sexpNow,
                                     int argumentCount, boolean isInScope ) throws SystemMessageException {
    ConsNode returnNode;
    if ( !argumentNode.IsAtomNode() ) {
      while ( !argumentNode.IsAtomNode() ) {
        VarNode aNode = new VarNode( argumentNode.GetLeft().ToString(),
                                     this.Eval( sexpNow.GetLeft(), false, isInScope ) );
        this.mLocalVar.Push( aNode );
        sexpNow = sexpNow.GetRight();
        argumentNode = argumentNode.GetRight();
      } // while
    } // if
    
    interFunction = interFunction.GetRight();
    do {
      returnNode = this.Eval( this.Clone( interFunction.GetLeft() ), false, true );
      interFunction = interFunction.GetRight();
    } while ( !interFunction.IsAtomNode() );
    for ( int i = 0 ; i < argumentCount ; i++ )
      this.mLocalVar.Pop();
    return returnNode;
  } // DoCustomFunction()
  
  private void CheckCustomFunction( ConsNode argumentNode, ConsNode sexpNow,
                                    String functionName ) throws SystemMessageException {
    while ( !argumentNode.IsAtomNode() && !sexpNow.IsAtomNode() ) {
      sexpNow = sexpNow.GetRight();
      argumentNode = argumentNode.GetRight();
    } // while
    
    if ( !argumentNode.IsAtomNode() || !sexpNow.IsAtomNode() )
      throw new SystemMessageException( "INoA", this.TakeRealFunction( functionName ) );
  } // CheckCustomFunction()
  
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
