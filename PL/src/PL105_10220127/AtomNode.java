package PL105_10220127;

public class AtomNode extends ConsNode {
  private int mDataType;
  private Token matom;
  private boolean mNil;
  
  public AtomNode( int line, int column ) {
    super( true );
    this.SetAtom( new Token( "nil", line, column ) );
    this.mDataType = DataType.NIL;
    this.SetNil( true );
  } // AtomNode()
    
  public AtomNode( Token atomT, int datatype ) {
    // TODO Auto-generated constructor stub
    super( true );
    this.mDataType = datatype;
    this.SetAtom( atomT );
  } // AtomNode()
    
  public void SetNil( boolean NilT ) {
    mNil = NilT;
  } // SetNil()
    
  public boolean IsNil() {
    return mNil;
  } // IsNil()
    
  public Token GetAtom() {
    return matom;
  } // GetAtom()
    
  public void SetAtom( Token atomT ) {
    matom = new Token( atomT );
  } // SetAtom()
    
  public String ToString() {
    return matom.GetData();
  } // ToString()
  
  public int GetDataType() {
    return this.mDataType;
  } // GetDataType()
} // class AtomNode
