package PL_10220127;

public class AtomNode extends ConsNode {
  private Token matom;
  private boolean mNil;
  public AtomNode( int column ) {
    super( true );
    matom = new Token( "nil", column );
    SetNil( true );
  } // AtomNode()
    
  public AtomNode( Token atomT ) {
    // TODO Auto-generated constructor stub
    super( true );
    SetAtom( atomT );
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
} // class AtomNode
