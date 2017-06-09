package PL105_10220127;

public class VarNode {
  private ConsNode mNode;
  private String Variable;
  public VarNode( String Var, ConsNode aNode ) {
    this.SetVariable( Var );
    this.SetmNode( aNode );
  } // VarNode()
  
  public ConsNode GetmNode() {
    return mNode;
  } // GetmNode()
  
  public void SetmNode( ConsNode mNode ) {
    this.mNode = mNode;
  } // SetmNode()
  
  public String GetVariable() {
    return Variable;
  } // GetVariable()
  
  public void SetVariable( String variable ) {
    Variable = variable;
  } // SetVariable()
} // class VarNode
