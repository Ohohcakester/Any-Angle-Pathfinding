package grid.bst;



/**
 * Class for a Node<E>  that stores type E objects.
 * Easier to use as compare to when Node is an inner class of BinaryTree
 **/
public class Node<E> {
  
  // Data Fields
  
  /** The information stored in this node. */
  E data;
  /** Reference to the left child. */
  Node<E> left;
  /** Reference to the right child. */
  Node<E> right;
  
  Node<E> next;
  Node<E> prev;
  
  // Constructors
  /**
   * Construct a node with given data and no children.
   * @param data The data to store in this node
   */
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
  
  // Methods
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

