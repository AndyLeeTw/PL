package PL_10220127;

public class ConsNode {
    private ConsNode mRightNode;
    private ConsNode mLeftNode;
    public ConsNode( ConsNode RightNodeT, ConsNode LeftNodeT ) {
        mRightNode = RightNodeT;
        mLeftNode = LeftNodeT;
    }
    public ConsNode() {}
    public void SetRight( ConsNode Right ) {
        mRightNode = Right;
    }
    public void SetLeft( ConsNode Left ) {
        mLeftNode = Left;
    }
    public ConsNode GetLeft() {
        return mLeftNode;
    }
    public ConsNode GetRight() {
        return mRightNode;
    }
}
