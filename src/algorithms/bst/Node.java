package algorithms.bst;


public class Node<E> {
    /*private static int counter = 0; // DEBUGGING TOOL
    private int index;
    private void setIndex() {
        index = counter; counter++;
    }*/
  
  E data;
  Node<E> left;
  Node<E> right;
  
  Node<E> next;
  Node<E> prev;
  
  public Node() {
      
  }
  
  public Node(E data) {
    this.data = data;
    left = null;
    right = null;
  }
  
  public Node(E data, Node<E> left, Node<E> right) {
    this.data = data;
    this.left = left;
    this.right = right;
  }
  
  /**
   * Returns a string representation of the node.
   * @return A string representation of the data fields
   */
  @Override
  public String toString() {
    if (data == null)
      return null;
      
    String s = data.toString();
    return s;
  }
  
  public E getData() {
    return data;
  }
  
  public void setData(E newData) {
    data = newData;
  }
  
  public Node<E> getLeft() {
    return left;
  }
  
  public Node<E> getRight() {
    return right;
  }
  
  public Node<E> getNext() {
    return next;
  }
  
  public Node<E> getPrev() {
    return prev;
  }
  
  public void setLeft(Node<E> left) {
    this.left = left;
  }
  
  public void setRight(Node<E> right) {
    this.right = right;
  }
  
}

