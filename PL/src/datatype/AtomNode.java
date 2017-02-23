package datatype;

public class AtomNode extends ConsNode{
    private String atom;
    private boolean Nil;
    public AtomNode(){
        this("");
    }
    public AtomNode(String _atom) {
        // TODO Auto-generated constructor stub
        super(null, null);
        this.setAtom(_atom);
    }
    public void setNil(boolean _Nil){
        this.Nil = _Nil;
    }
    public boolean isNil(){
        return this.Nil;
    }
    public String getAtom() {
        return atom;
    }
    public void setAtom(String atom) {
        this.atom = atom;
    }
}
