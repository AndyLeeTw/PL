package tokenProcess;

import java.util.ArrayList;
import java.util.List;
import datatype.ConsNode;
import datatype.Token;

public class treeBuilder {
    public treeBuilder(){
    }
    public ConsNode treeContruct(ConsNode head, List<Token> _Tokens){
        ArrayList<Token> Tokens = (ArrayList<Token>)_Tokens;
        if(Tokens.get(0).getData().compareTo("(") == 0){
            if(head == null)
                head = new ConsNode();
            Tokens.remove(0);
            head.setLeft(treeContruct(null, Tokens));
            head.setRight(treeContruct(null, Tokens));
        }
        return null;
    }
}
