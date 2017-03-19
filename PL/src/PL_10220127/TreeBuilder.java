package PL_10220127;

import java.util.ArrayList;
import java.util.List;

public class TreeBuilder {
    public TreeBuilder() {}
    public ConsNode TreeContruct( ConsNode head, ArrayList<Token> TokensT ) {
        ArrayList<Token> Tokens = TokensT;
        if(Tokens.get(0).GetData().substring(0, 1).compareTo("(") == 0){
            if(Tokens.size() > 1 && Tokens.get(1).GetData().compareTo(")") == 0)
                return new AtomNode(Tokens.get(0).GetColumn());
            else {
                if(head == null)
                    head = new ConsNode();
                Tokens.get(0).SetData(Tokens.get(0).GetData().replaceFirst("[(]", ""));
                if(Tokens.get(0).GetData().length() == 0)
                    Tokens.remove(0);
                head.SetLeft(TreeContruct(null, Tokens));
                head.SetRight(TreeContruct(new ConsNode(), Tokens));
            }
        } else if(Tokens.get(0).GetData().compareTo(".") == 0) {
            Tokens.remove(0);
            head = new AtomNode(Tokens.get(0));
        } else if(Tokens.get(0).GetData().compareTo(")") == 0){
            head.SetRight(new AtomNode(Tokens.get(0).GetColumn()));
            Tokens.remove(0);
        } else {
            if(head == null){
                head = new AtomNode(Tokens.get(0));
                Tokens.remove(0);
            } else{
                head.SetLeft(new AtomNode(Tokens.get(0)));
                Tokens.remove(0);
                head.SetRight(TreeContruct(new ConsNode(), Tokens));
            }
        }
        return head;
    }
    public void TreeTravel( ConsNode head ) {
        if(head == null){}
        else if(head.getClass().equals(AtomNode.class)) {
            System.out.println(head);
        } else{
            TreeTravel(head.GetLeft());
            TreeTravel(head.GetRight());
        }
    }
}
