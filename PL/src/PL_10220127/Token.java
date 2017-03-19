package PL_10220127;

public class Token {
    private int mcolumn;
    String mdata;
    public Token( String dataT, int columnT ) {
        mdata = dataT;
        mcolumn = columnT;
    }
    public Token( Token TokenT ) {
        mdata = TokenT.GetData();
        mcolumn = TokenT.GetColumn();
    }
    public void SetData( String DataT ) {
        mdata = DataT;
    }
    public String GetData() {
        return mdata;
    }
    public int GetColumn() {
        return mcolumn;
    }
}
