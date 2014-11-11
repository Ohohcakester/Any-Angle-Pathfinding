package grid.bst;

import static org.junit.Assert.*;

import org.junit.Test;

public class AVLTreeTest {

    @Test
    public void test() {
        AVLTree<Integer> avlTree = new AVLTree<>();
        for (int i=0;i<15;i++) {
            avlTree.insert(i);
        }
        System.out.println(avlTree.inorderToString());
        avlTree.delete(5);
        avlTree.delete(7);
        avlTree.delete(9);
        avlTree.delete(8);
        avlTree.insert(7);
        
        Node<Integer> node = avlTree.search(0);
        while (node != null) {
            System.out.print(node.data+" ");
            node = node.getNext();
            System.out.print(node.data+" ");
            node = node.getPrev();
            System.out.print(node.data+" ");
            node = node.getNext();
        }
    }

}
