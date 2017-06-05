package PL105_10220127;

public class AtomNode extends ConsNode {
  private int mDataType;
  private Token matom;
  private boolean mNil;
  
  public AtomNode( int line, int column ) {
    super( true );
    this.SetAtom( new Token( "nil", line, column ) );
    this.SetDataType( DataType.NIL );
    this.SetNil( true );
  } // AtomNode()
    
  public AtomNode( Token atomT, int dataType ) {
    // TODO Auto-generated constructor stub
    super( true );
    this.SetDataType( dataType );
    if ( dataType == DataType.NIL )
      this.SetNil( true );
    this.SetAtom( atomT );
  } // AtomNode()
    
  public void SetNil( boolean NilT ) {
    this.mNil = NilT;
  } // SetNil()
    
  public boolean IsNil() {
    return this.mNil;
  } // IsNil()
    
  public Token GetAtom() {
    return this.matom;
  } // GetAtom()
    
  public void SetAtom( Token atomT ) {
    this.matom = new Token( atomT );
  } // SetAtom()
    
  public String ToString() {
    return this.matom.GetData();
  } // ToString()
  
  public void SetDataType( int dataType ) {
    this.mDataType = dataType;
  } // SetDataType()
  
  public int GetDataType() {
    return this.mDataType;
  } // GetDataType()
} // class AtomNode
