package PL105_10220127;

import java.util.Iterator;
import java.util.ArrayList;

public class ValueStack {
  private ArrayList<VarNode>mStack;

  public ValueStack() {
    this.mStack = new ArrayList<VarNode>();
  } // ValueStack()
  
  public void Push( VarNode aNode ) {
    this.mStack.add( aNode );
  } // Push()
  
  public void Pop() {
    this.mStack.remove( this.mStack.size() - 1 );
  } // Pop()
  
  public void Clear() {
    while ( !this.mStack.isEmpty() )
      this.Pop();
  } // Clear()
  
  public ConsNode GetLocalValue( String var ) {
    for ( int i = 0 ; i < this.mStack.size() ; i++ ) {
      VarNode localValue = this.mStack.get( i );
      if ( localValue.GetVariable().compareTo( var ) == 0 )
        return localValue.GetmNode();
    } // for
    
    return null;
  } // GetLocalValue()
} // class ValueStack
