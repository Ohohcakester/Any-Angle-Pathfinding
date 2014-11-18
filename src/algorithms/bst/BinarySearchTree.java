package algorithms.bst;

/**
 * Class for a binary tree that stores type E objects.
 * Node is a public class.
 **/
public class BinarySearchTree<E extends Comparable<? super E>> extends BinaryTree<E> {

    /** Construct an empty BinaryTree */
    public BinarySearchTree() {
        root = null;
    }
    
    
    protected Node<E> rotateLeft(Node<E> pivot) {
        Node<E> temp = pivot.right;
        pivot.right = pivot.right.left;
        temp.left = pivot;
        return temp;
    }
    
    protected Node<E> rotateRight(Node<E> pivot) {
        Node<E> temp = pivot.left;
        pivot.left = pivot.left.right;
        temp.right = pivot;
        return temp;
    }

    /**
     * Construct a BinaryTree with a specified root.
     * Should only be used by subclasses.
     * @param root The node that is the root of the tree.
     */
    protected BinarySearchTree(Node<E> root) {
        this.root = root;
    }

    /**
     * Constructs a new binary tree with data in its root,leftTree
     * as its left subtree and rightTree as its right subtree.
     */
    public BinarySearchTree(E data, BinaryTree<E> leftTree,
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

    public boolean insertBST(E value) {
        // Returns true iff insertion is successful.
        
        Node<E> newRoot = insertBST(root,value);
        if (newRoot == null)
            return false;
        
        // newRoot != null.
        root = newRoot;
        return true;
    }
    
    private Node<E> insertBST(Node<E> current, E value) {
        if (current == null)
            return new Node<>(value);
        
        int result = value.compareTo(current.data);
        if (result < 0) {
            Node<E> newCurrent = insertBST(current.left,value);
            if (newCurrent == null)
                return null;
            current.left = newCurrent;
            return current;
        }
        if (result > 0) {
            Node<E> newCurrent = insertBST(current.right,value);
            if (newCurrent == null)
                return null;
            current.right = newCurrent;
            return current;
        }
        else
            return null;
    }
    
    public boolean contains(E data) {
        return contains(root, data);
    }
    
    private boolean contains(Node<E> current, E data) {
        if (current == null) return false;
        
        int compare = current.data.compareTo(data);
        if (compare == 0)
            return true;
        if (compare < 0)
            return contains(current.right, data);
        else
            return contains(current.left, data);
    }
    
    public Node<E> search(E data) {
        Node<E> current = root;
        while(current != null) {
            int result = data.compareTo(current.data);
            if (result < 0) {
                current = current.left;
            } else if (result > 0) {
                current = current.right;
            } else {
                //equal
                return current;
            }
        }
        return null;
    }
    
    public Node<E> ceiling(E data) {
        Node<E> current = root;
        Node<E> currentCeiling = null;
        while(current != null) {
            int result = data.compareTo(current.data);
            if (result < 0) {
                currentCeiling = current;
                current = current.left;
            } else if (result > 0) {
                current = current.right;
            } else {
                // equal
                return current;
            }
        }
        return currentCeiling;
    }
}