package datatype;

public class ConsNode {
    private ConsNode RightNode;
    private ConsNode LeftNode;
    public ConsNode(ConsNode _RightNode, ConsNode _LeftNode){
        this.RightNode = _RightNode;
        this.LeftNode = _LeftNode;
    }
    public ConsNode(){}
    public void setRight(ConsNode Right){
        this.RightNode = Right;
    }
    public void setLeft(ConsNode Left){
        this.RightNode = Left;
    }
}
