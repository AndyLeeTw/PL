package PL_10220127;

public class ConsNode {
  private ConsNode mRightNode;
  private ConsNode mLeftNode;
  private boolean mIsAtomNode;
  public ConsNode( boolean mIsAtomNodeT ) {
    mRightNode = null;
    mLeftNode = null;
    mIsAtomNode = mIsAtomNodeT;
  } // ConsNode()
  
  public ConsNode() {
    mRightNode = null;
    mLeftNode = null;
    mIsAtomNode = false;
  } // ConsNode()
  
  public void SetRight( ConsNode Right ) {
    mRightNode = Right;
  } // SetRight()
  
  public void SetLeft( ConsNode Left ) {
    mLeftNode = Left;
  } // SetLeft()
  
  public ConsNode GetLeft() {
    return mLeftNode;
  } // GetLeft()
  
  public ConsNode GetRight() {
    return mRightNode;
  } // GetRight()
  
  public Boolean IsAtomNode() {
    return mIsAtomNode;
  } // IsAtomNode()
  
  public String ToString() {
    return "";
  } // ToString()
} // class ConsNode
