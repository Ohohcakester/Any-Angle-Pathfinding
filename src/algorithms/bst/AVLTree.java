package algorithms.bst;

import java.util.Stack;

class AVLNode<E> extends Node<E> {
    int balance;
    
    public AVLNode(E data) {
        this.data = data;
        balance = 10; // Indication of new node.
    }
}

public class AVLTree<E extends Comparable<? super E>> extends BinarySearchTree<E> {
    private Node<E> first, last;
    
    public AVLTree() {
        root = null;
    }
    
    private Node<E> rightRightRotate(AVLNode<E> root) {
        // Note: changes balance.
        // Rotates root left.
        
        /* Nodes with balance change: Root, Right child.
         * Old Root: +2, Old Right child: +1
         *
         * New Root(Old RC): 0,
         * New Left child(Old Root): 0
         */
        
        root.balance = 0;
        ((AVLNode<E>)root.right).balance = 0;
        return rotateLeft(root);
    }
    
    private Node<E> leftLeftRotate(AVLNode<E> root) {
        // Note: changes balance.
        // Rotates root rigjt.
        
        /* Nodes with balance change: Root, Left child.
         * Old Root: -2, Old Left child: -1
         *
         * New Root(Old LC): 0,
         * New Right child(Old Root): 0
         */
        
        root.balance = 0;
        ((AVLNode<E>)root.left).balance = 0;
        return rotateRight(root);
    }
    
    
    private Node<E> rightCenterRotate(AVLNode<E> root) {
        // Note: changes balance.
        // Rotates root left.
        
        /* Nodes with balance change: Root, Right child.
         * Old Root: +2, Old Right child: 0
         *
         * New Root(Old RC): -1,
         * New Left child(Old Root): +1
         */
        
        ((AVLNode<E>)root.right).balance = -1;
        root.balance = 1;
        return rotateLeft(root);
    }
    
    
    private Node<E> leftCenterRotate(AVLNode<E> root) {
        // Note: changes balance.
        // Rotates root right.
        
        /* Nodes with balance change: Root, Left child.
         * Old Root: -2, Old Left child: 0
         *
         * New Root(Old RC): +1,
         * New Right child(Old Root): -1
         */
        
        ((AVLNode<E>)root.left).balance = 1;
        root.balance = -1;
        return rotateRight(root);
    }
    
    
    private Node<E> leftRightRotate(AVLNode<E> root) {
        // Note: changes balance.
        // Rotates LC left, then root right.
        
        /* Nodes with balance change: Root, Left child, LR grandchild.
         * Old Root: -2, Old Left child: +1, Old LR grandchild: -1 or +1
         *
         * New Root(Old LR grandchild): 0
         * New Right child(Old root): (Old RL == -1) ? +1 : 0
         * New Left child(Old Left child): (Old RL == +1) ? -1 : 0
         */
        int balance = ((AVLNode<E>)root.left.right).balance;
        ((AVLNode<E>)root.left.right).balance = 0;
        root.balance = (balance == -1) ? 1 : 0;
        ((AVLNode<E>)root.left).balance = (balance == 1) ? -1 : 0;
        
        root.left = rotateLeft(root.left);
        return rotateRight(root);
    }
    
    private Node<E> rightLeftRotate(AVLNode<E> root) {
        // Note: changes balance.
        // Rotates RC right, then root left.
        
        /* Nodes with balance change: Root, Right child, RL grandchild.
         * Old Root: +2, Old Right child: -1, Old RL grandchild: +1 or -1
         *
         * New Root(Old RL grandchild): 0
         * New Left child(Old root): (Old RL == +1) ? -1 : 0
         * New Right child(Old Right child): (Old RL == -1) ? +1 : 0
         */
        int balance = ((AVLNode<E>)root.right.left).balance;
        ((AVLNode<E>)root.right.left).balance = 0;
        root.balance = (balance == 1) ? -1 : 0;
        ((AVLNode<E>)root.right).balance = (balance == -1) ? 1 : 0;
        
        
        root.right = rotateRight(root.right);
        return rotateLeft(root);
    }
    
    
    private Node<E> conditionalRotate(AVLNode<E> current) {
        // 6 Possibilities:
        // Current = +2, current.right = +1 / -1 / 0
        // Current = -2, current.left = +1 / -1 / 0
        // Note: child with 0 balance occurs only for deletion.
        
        // Does rotation and readjusts balance accordingly.
        
        if (current.balance == 2) {
            if (((AVLNode<E>)current.right).balance == 1)
                return rightRightRotate(current);
            else if (((AVLNode<E>)current.right).balance == -1)
                return rightLeftRotate(current);
            else // right.balance == 0
                return rightCenterRotate(current);
        }
        else { // current.balance == -2
            if (((AVLNode<E>)current.left).balance == -1)
                return leftLeftRotate(current);
            else if (((AVLNode<E>)current.left).balance == 1)
                return leftRightRotate(current);
            else // left.balance == 0
                return leftCenterRotate(current);
        }
    }
    
    private void log(Node<E> node) {
        System.out.println(node.data + " : " + ((AVLNode<E>)node).balance);
    }
    
    public void insert(E data) {
        Node<E> newRoot = insert(root,data,null, false);
        if (newRoot == null)
            return;
        root = newRoot;
        if (((AVLNode<E>)root).balance == 10)
            ((AVLNode<E>)root).balance = 0;
    }
    
    /*private void printCurrent(Node<E> current) { // DEBUGGING TOOL
        if (current == null)return;
        System.out.println(current +": >> " + current.left + " " + current.right + " | " + current.prev + " " + current.next);
    }*/
    
    private Node<E> insert(Node<E> current, E data, Node<E> parent, boolean wentRight) {
        if (current == null) {
            AVLNode<E> newNode = new AVLNode<E>(data);
            if (parent != null) {
                if (wentRight) {
                    // parent is left of newNode.
                    newNode.next = parent.next;
                    newNode.prev = parent;
                    if (parent.next != null) {
                        parent.next.prev = newNode;
                    } else {
                        last = newNode;
                    }
                    parent.next = newNode;
                } else {
                    // parent is right of newNode.
                    newNode.prev = parent.prev;
                    newNode.next = parent;
                    if (parent.prev != null) {
                        parent.prev.next = newNode;
                    } else {
                        first = newNode;
                    }
                    parent.prev = newNode;
                }
            } else {
                // Root
                first = newNode;
                last = newNode;
            }
            return newNode;
        }

        int compare = data.compareTo(current.data);
        
        if (compare == 0)
            return null; // don't insert
        
        if (compare < 0) {
            Node<E> newLeft = insert(current.left, data, current, false);
            if (newLeft == null)
                return null;
            current.left = newLeft;
            
            // Modify balance / Rebalancing
            if (((AVLNode<E>)current.left).balance == 0)
                return null; // child has been balanced to 0. End recursion.
            else {
                if (((AVLNode<E>)current.left).balance == 10)
                    ((AVLNode<E>)current.left).balance = 0;
                    
                // child has been unbalanced away from 0. Change balance of current.
                AVLNode<E> avlCurrent = (AVLNode<E>)current;
                avlCurrent.balance--;
                if (avlCurrent.balance == -2) {
                    // Unbalanced to -2. need rotation.
                    return conditionalRotate(avlCurrent);
                }
                return current;
            }
        }
        else {
            Node<E> newRight = insert(current.right, data, current, true);
            if (newRight == null)
                return null;
            current.right = newRight;
            
            // Modify balance / Rebalancing
            if (((AVLNode<E>)current.right).balance == 0)
                return null; // child has been balanced to 0. End recursion.
            else {
                if (((AVLNode<E>)current.right).balance == 10)
                    ((AVLNode<E>)current.right).balance = 0;
                
                // child has been unbalanced away from 0. Change balance of current.
                AVLNode<E> avlCurrent = (AVLNode<E>)current;
                avlCurrent.balance++;
                if (avlCurrent.balance == 2) {
                    // Unbalanced to 2. need rotation.
                    return conditionalRotate(avlCurrent);
                }
                return current;
            }
        }
    }
    
    public void logAll() {
        logAll(root);
    }
    
    private void logAll(Node<E> current) {
        if (current == null) return;
        logAll(current.left);
        log(current);
        logAll(current.right);
    }
    
    public void delete(E data) {
        Stack<Node<E>> nodeStack = new Stack<Node<E>>();
        delete(data, nodeStack);
    }
    
    private void delete(E data, Stack<Node<E>> nodeStack) {
        Node<E> current = searchAndPush(data, nodeStack);
        
        if (current == null)
            throw new NullPointerException("Item not found!");
        
        if (current.left == null) {
            linkedListRemove(current);
            linkFromParent(current, current.right, nodeStack); // set parent to point to right.
            current = current.right;
        }
        else if (current.right == null) {
            linkedListRemove(current);
            linkFromParent(current, current.left, nodeStack); // set parent to point to right.
            current = current.left;
        }
        else {// two children.
            nodeStack.push(current);
            Node<E> newCurrent;
            if (current.right.left == null) {
                newCurrent = current.right;
                current.right = newCurrent.right;
            }
            else {
                // Note: newCurrent has no left child.
                newCurrent = findSmallestChildAndPush(current.right, nodeStack); // newTarget is inorder successor.
                nodeStack.peek().left = newCurrent.right;
            }
            current.data = newCurrent.data; // replace target data, search for new target.
            linkedListRemove(newCurrent);
            current = newCurrent.right;
        }
        
        // Now to rebalance while clearing the stack.
        while (nodeStack != null && !nodeStack.isEmpty()) {
            AVLNode<E> parent = (AVLNode<E>)nodeStack.peek();
            if (parent.left == current) {
                // Current is a left child
                if (parent.right == current) // basically current and right is null
                    parent.balance = 0;
                else
                    parent.balance++;
            }
            else {
                // Current is a right child.
                parent.balance--;
            }
            
            // new current. = previous parent. for next loop.
            current = nodeStack.pop(); // = parent
            
            // Note: continue loop iff parent balance != -1 and != 1.
            // End loop if parent balance == -1 or balance == 1;
            if (parent.balance == 2 || parent.balance == -2) {
                
                // Rotate parent accordingly.
                if (nodeStack.isEmpty()) {
                    root = conditionalRotate(parent);
                }
                else {
                    Node<E> grandParent = nodeStack.peek();
                    
                    if (grandParent.left == parent) {
                        grandParent.left = conditionalRotate(parent);
                        current = grandParent.left;
                    }
                    else if (grandParent.right == parent) {
                        grandParent.right = conditionalRotate(parent);
                        current = grandParent.right;
                    }
                }
            }
            
            // Now current is the new parent.
            if (((AVLNode<E>)current).balance != 0) { // balance == 1 or == -1
                // End loop
                nodeStack = null;
            }
            // else continue looping.
        }
    }
    
    private void linkFromParent(Node<E> current, Node<E> next, Stack<Node<E>> nodeStack) {
        if (nodeStack.isEmpty())
            root = next;
        else if (nodeStack.peek().left == current)
            nodeStack.peek().left = next;
        else
            nodeStack.peek().right = next;
    }
    
    private Node<E> searchAndPush(E data, Stack<Node<E>> nodeStack) {
        Node<E> current = root;
        
        while(true) {
            if (current == null)
                return null;
            
            nodeStack.push(current);
            
            int compare = data.compareTo(current.data);
            
            if (compare < 0)
                current = current.left;
            else if (compare > 0)
                current = current.right;
            else // compare == 0.
                return nodeStack.pop();
        }
        
    }
    
    private Node<E> findSmallestChildAndPush(Node<E> current, Stack<Node<E>> nodeStack) {
        // assumpe current != null.
        
        while (current.left != null) {
            nodeStack.push(current);
            current = current.left;
        }
        return current;
    }
    

    private void linkedListRemove(Node<E> current) {
        if (current.next != null) {
            current.next.prev = current.prev;
        } else {
            last = current.prev;
        }
        if (current.prev != null) {
            current.prev.next = current.next;
        } else {
            first = current.next;
        }
    }

    public Node<E> getLast() {
        return last;
    }

    public Node<E> getFirst() {
        return first;
    }
}