package datatype;

public class Token {
    private int column;
    String data;
    public Token(String _data, int _column){
        this.data = _data;
        this.column = _column;
    }
    public String getData(){
        return this.data;
    }
}
