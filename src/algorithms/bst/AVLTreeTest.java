package algorithms.bst;

import static org.junit.Assert.*;

import org.junit.Test;

public class AVLTreeTest {

    @Test
    public void test2() {
        AVLTree<Integer> avlTree = new AVLTree<>();
        avlTree.insert(10);
        printTreeLinkedList(avlTree);System.out.println();
        avlTree.insert(5);
        printTreeLinkedList(avlTree);System.out.println();
        avlTree.insert(15);
        printTreeLinkedList(avlTree);System.out.println();
        avlTree.insert(3);
        printTreeLinkedList(avlTree);System.out.println();
        avlTree.insert(8);
        System.out.println(avlTree.inorderToString());
        printTreeLinkedList(avlTree);System.out.println();
        
        avlTree.delete(8);
        System.out.println(avlTree.inorderToString());
        printTreeLinkedList(avlTree);System.out.println();

        avlTree.insert(8);
        System.out.println(avlTree.inorderToString());
        printTreeLinkedList(avlTree);System.out.println();
        
        avlTree.insert(7);
        System.out.println(avlTree.inorderToString());
        printTreeLinkedList(avlTree);System.out.println();
        
    }
    
    public void test() {
        AVLTree<Integer> avlTree = new AVLTree<>();
        for (int i=0;i<15;i++) {
            avlTree.insert(i);
        }
        System.out.println(avlTree.inorderToString());

        Node<Integer> node = avlTree.getFirst();
        while (node != null) {
            System.out.print(node.data+" ");
            node = node.getNext();
        }
        System.out.println();

        node = avlTree.getLast();
        while (node != null) {
            System.out.print(node.data+" ");
            node = node.getPrev();
        }
        System.out.println();
        
        avlTree.delete(5); System.out.println("5");
        printTreeLinkedList(avlTree);
        avlTree.delete(0);
        printTreeLinkedList(avlTree);
        avlTree.delete(1);
        printTreeLinkedList(avlTree);
        avlTree.insert(0);
        printTreeLinkedList(avlTree);
        avlTree.insert(-1);
        printTreeLinkedList(avlTree);
        avlTree.delete(0); System.out.println("del 0");
        printTreeLinkedList(avlTree);
        avlTree.insert(1);
        printTreeLinkedList(avlTree);
        avlTree.insert(15);
        printTreeLinkedList(avlTree);
        avlTree.delete(14);
        printTreeLinkedList(avlTree);
        avlTree.delete(15);
        printTreeLinkedList(avlTree);
        avlTree.delete(13);
        printTreeLinkedList(avlTree);
        avlTree.insert(14);
        printTreeLinkedList(avlTree);
        avlTree.insert(13);  System.out.println("ins 13");
        System.out.println(avlTree.inorderToString());
        printTreeLinkedList(avlTree);
        avlTree.delete(7);  System.out.println("del 7");
        System.out.println(avlTree.inorderToString());
        printTreeLinkedList(avlTree);
        avlTree.delete(9);  System.out.println("del 9");
        System.out.println(avlTree.inorderToString());
        printTreeLinkedList(avlTree);
        avlTree.delete(8);  System.out.println("del 8");
        System.out.println(avlTree.inorderToString());
        printTreeLinkedList(avlTree);
        avlTree.insert(7);  System.out.println("ins 7");
        System.out.println(avlTree.inorderToString());
        printTreeLinkedList(avlTree);

        System.out.println(avlTree.inorderToString());
        
        printTreeLinkedList(avlTree);

        node = avlTree.getFirst();
        while (node != null) {
            System.out.print(node.data+" ");
            node = node.getNext();
            if (node != null) {
                System.out.print(node.data+" ");
                node = node.getPrev();
                System.out.print(node.data+" ");
                node = node.getNext();
            }
        }
        System.out.println();

        node = avlTree.getLast();
        while (node != null) {
            System.out.print(node.data+" ");
            node = node.getPrev();
            if (node != null) {
                System.out.print(node.data+" ");
                node = node.getNext();
                System.out.print(node.data+" ");
                node = node.getPrev();
            }
        }
        System.out.println();
    }

    private void printTreeLinkedList(AVLTree<Integer> avlTree) {
        Node<Integer> node = avlTree.getFirst();
        while (node != null) {
            System.out.print(node.data+" ");
            if (node.next != null)
                assertTrue(node.data < node.next.data);
            node = node.getNext();
        }
        System.out.println();

        node = avlTree.getLast();
        while (node != null) {
            System.out.print(node.data+" ");
            if (node.prev != null)
                assertTrue(node.data > node.prev.data);
            node = node.getPrev();
        }
        System.out.println();
        
        node = avlTree.root;
        System.out.println(node + " " + node.next);
    }

}
