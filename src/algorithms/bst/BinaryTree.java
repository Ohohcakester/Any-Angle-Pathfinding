package algorithms.bst;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.Queue;
import java.util.Stack;

/**
 * Base class for a binary tree that stores type E objects.
 * Node is a public class.
 */
public class BinaryTree<E> implements Iterable<E>{

    // Data Field
    /** The root of the binary tree */
    protected Node<E> root;
    
    private class TreeIterator<E> implements Iterator<E> {
        
        private BinaryTree<E> tree;
        private Node<E> current;
        private Stack<Node<E>> nodeStack;
        
        protected TreeIterator (BinaryTree<E> tree) {
            this.tree = tree;
            current = null;
            nodeStack = new Stack<>();
        }

        @Override
        public boolean hasNext() {
            if (current == null)
                return root != null;
            
            if (current.right == null)
                return !nodeStack.isEmpty();
            
            return true;
        }

        @Override
        public E next() {
            if (current == null) {
                if (root != null)
                    current = tree.root;
                else
                    throw new IndexOutOfBoundsException();
                
                // From start, go to far left.
                goToFarLeft();
                return current.data;
            }
            else {
                if (current.right != null) {
                    // Has Right Child: go right, then go to far left, pushing lefts into stack.
                    current = current.right;
                    goToFarLeft();
                    return current.data;
                }
                else {
                    // Has no Right Child: pop from stack.
                    current = nodeStack.pop();
                    return current.data;
                }   
            }
        }
        
        private void goToFarLeft() {
            while(current.left != null) {
                nodeStack.push(current);
                current = current.left;
            }
        }

        @Override
        public void remove() {
            throw new UnsupportedOperationException();
        }
        
    }
    
    public TreeIterator<E> iterator() {
        return new TreeIterator<E>(this);
    }

    /** Construct an empty BinaryTree */
    public BinaryTree() {
        root = null;
    }

    /**
     * Construct a BinaryTree with a specified root.
     * Should only be used by subclasses.
     * @param root The node that is the root of the tree.
     */
    protected BinaryTree(Node<E> root) {
        this.root = root;
    }

    /**
     * Constructs a new binary tree with data in its root,leftTree
     * as its left subtree and rightTree as its right subtree.
     */
    public BinaryTree(E data, BinaryTree<E> leftTree,
            BinaryTree<E> rightTree) {
        root = new Node<E>(data);
        if (leftTree != null) {
            root.left = leftTree.root;
        } else {
            root.left = null;
        }
        if (rightTree != null) {
            root.right = rightTree.root;
        } else {
            root.right = null;
        }
    }

    /**
     * Return the left subtree.
     * @return The left subtree or null if either the root or
     * the left subtree is null
     */
    public BinaryTree<E> getLeftSubtree() {
        if (root != null && root.left != null) {
            return new BinaryTree<E>(root.left);
        } else {
            return null;
        }
    }

    /**
     * Return the right sub-tree
     * @return the right sub-tree or
     *         null if either the root or the
     *         right subtree is null.
     */
    public BinaryTree<E> getRightSubtree() {
        if (root != null && root.right != null) {
            return new BinaryTree<E>(root.right);
        } else {
            return null;
        }
    }

    /**
     * Return the data field of the root
     * @return the data field of the root
     *         or null if the root is null
     */
    public E getData() {
        if (root != null) {
            return root.data;
        } else {
            return null;
        }
    }

    /**
     * Determine whether this tree is a leaf.
     * @return true if the root has no children
     */
    public boolean isLeaf() {
        return (root == null || (root.left == null && root.right == null));
    }
  
    public int height() {
      return height(root);
    }
    
    public int height(Node<E> r) {
      if (r == null) return 0;
      return 1 + Math.max(height(r.left), height(r.right));
    }

    public int size() {
      return size(root);
    }
    
    public int size(Node<E> r) {
      if (r == null) return 0;
      return 1 + size(r.left) + size(r.right);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        preOrderTraverse(root, 1, sb);
        return sb.toString();
    }
     
    /**
     * Perform a preorder traversal.
     * @param node The local root
     * @param depth The depth
     * @param sb The string buffer to save the output
     */
    private void preOrderTraverse(Node<E> node, int depth,
            StringBuilder sb) {
        for (int i = 1; i < depth; i++) {
            sb.append("  ");
        }
        if (node == null) {
            sb.append("null\n");
        } else {
            sb.append(node.toString());
            sb.append("\n");
            preOrderTraverse(node.left, depth + 1, sb);
            preOrderTraverse(node.right, depth + 1, sb);
        }
    }
     
        
     /**
     * Method to return the preorder traversal of the binary tree
     * as a sequence of strings each separated by a space.
     * @return A preorder traversal as a string
     */
    public String preorderToString() {
        StringBuilder stb = new StringBuilder();
        preorderToString(stb, root);
        return stb.toString();
    }

    private void preorderToString(StringBuilder stb, Node<E> root) {
        stb.append(root);
        if (root.left != null) {
            stb.append(" ");
            preorderToString(stb, root.left);
        }
        if (root.right != null) {
            stb.append(" ");
            preorderToString(stb, root.right);
        }
    }

    /**
     * Method to return the postorder traversal of the binary tree
     * as a sequence of strings each separated by a space.
     * @return A postorder traversal as a string
     */
    public String postorderToString() {
        StringBuilder stb = new StringBuilder();
        postorderToString(stb, root);
        return stb.toString();
    }

    private void postorderToString(StringBuilder stb, Node<E> root) {
        if (root.left != null) {
            postorderToString(stb, root.left);
            stb.append(" ");
        }
        if (root.right != null) {
            postorderToString(stb, root.right);
            stb.append(" ");
        }
        stb.append(root);
    }

    /** 
     * A method to display the inorder traversal of a binary tree 
     * placeing a left parenthesis before each subtree and a right 
     * parenthesis after each subtree. For example the expression 
     * tree shown in Figure 6.12 would be represented as
     * (((x) + (y)) * ((a) / (b))).
     * @return An inorder string representation of the tree
     */
    public String inorderToString() {
        if (root == null) return "null";
        
        StringBuilder stb = new StringBuilder();
        inorderToString(stb, root);
        return stb.toString();
    }

    private void inorderToString(StringBuilder stb, Node<E> root) {
        if (root.left != null) {
            stb.append("(");
            inorderToString(stb, root.left);
            stb.append(") ");
        }
        stb.append(root);
        if (root.right != null) {
            stb.append(" (");
            inorderToString(stb, root.right);
            stb.append(")");
        }
    }

    public String levelorderToString() {
        StringBuilder stb = new StringBuilder();
        levelorderToString(stb, root);
        return stb.toString();
    }
        
    private void levelorderToString(StringBuilder stb, Node<E> root) {
      if (root == null) {
        stb.append("An empty tree");
        return;
      }
      Queue<Node<E>> q = new LinkedList<Node<E>>();
      
      q.offer(root);
      while (!q.isEmpty()) {
        Node<E>  curr = q.poll();
        stb.append(curr);
        stb.append(" ");
        
        if (curr.left != null) {
            q.offer(curr.left);
        }
        if (curr.right != null) {
            q.offer(curr.right);
        }
      }
    }
    
    private void levelorderToStringPositions(StringBuilder stb, Node<E> root) {
      if (root == null) {
        stb.append("An empty tree");
        return;
      }
      Queue<Node<E>> q = new LinkedList<Node<E>>();
      Queue<String> strQueue = new LinkedList<>();
      
      q.offer(root);
      strQueue.offer("");
      
      while (!q.isEmpty()) {
        Node<E>  curr = q.poll();
        String currPath = strQueue.poll();
        stb.append("(");
        stb.append(curr);
        stb.append(",");
        stb.append(currPath);
        stb.append(") ");
        
        if (curr.left != null) {
            q.offer(curr.left);
            strQueue.offer(currPath + "L");
        }
        if (curr.right != null) {
            q.offer(curr.right);
            strQueue.offer(currPath + "R");
        }
      }
    }
    
    public boolean isEmpty() {
        return root == null;
    }
}