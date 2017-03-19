package PL_10220127;

public class AtomNode extends ConsNode {
    private Token matom;
    private boolean mNil;
    public AtomNode( int column ) {
        matom = new Token("", column);
        SetNil(true);
    }
    public AtomNode( Token atomT ) {
        // TODO Auto-generated constructor stub
        super(null, null);
        SetAtom(atomT);
    }
    public void SetNil( boolean NilT ) {
        mNil = NilT;
    }
    public boolean IsNil() {
        return mNil;
    }
    public Token GetAtom() {
        return matom;
    }
    public void SetAtom( Token atomT ) {
        matom = new Token(atomT);
    }
    public String ToString() {
        return matom.GetData();
    }
}
