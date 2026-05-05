package org.mangorage.game.client.layer;

import java.util.*;

public class LayerSystem {

    private static class Node {
        Layer layer;
        Node prev, next;

        Node(Layer layer) {
            this.layer = layer;
        }
    }

    private final Map<LayerId, Node> nodes = new HashMap<>();

    private Node head, tail;
    private int counter = 0;

    /* ---------------- API ---------------- */

    public LayerId add(LayerId id, Layer layer) {
        Node n = new Node(layer);
        nodes.put(id, n);

        if (head == null) {
            head = tail = n;
            return id;
        }

        linkAfter(tail, n);
        return id;
    }

    public LayerId insertAfter(LayerId target, LayerId id, Layer layer) {
        Node t = get(target);
        Node n = new Node(layer);

        nodes.put(id, n);
        linkAfter(t, n);

        return id;
    }

    public LayerId insertBetween(LayerId a, LayerId b, LayerId id, Layer layer) {
        Node na = get(a);
        Node nb = get(b);

        if (na.next != nb) {
            throw new IllegalStateException("Layers must be adjacent");
        }

        Node n = new Node(layer);

        nodes.put(id, n);
        linkBetween(na, nb, n);

        return id;
    }

    /* ---------------- internals ---------------- */

    private String genId() {
        return "layer_" + (counter++);
    }

    private Node get(LayerId id) {
        Node n = nodes.get(id);
        if (n == null) throw new IllegalArgumentException("LayerId not found: " + id);
        return n;
    }

    private void linkAfter(Node base, Node n) {
        n.prev = base;
        n.next = base.next;

        if (base.next != null) {
            base.next.prev = n;
        } else {
            tail = n;
        }

        base.next = n;
    }

    private void linkBetween(Node a, Node b, Node n) {
        a.next = n;
        n.prev = a;

        n.next = b;
        b.prev = n;
    }

    /* ---------------- view ---------------- */

    public List<Layer> asList() {
        List<Layer> out = new ArrayList<>();

        Node c = head;
        while (c != null) {
            out.add(c.layer);
            c = c.next;
        }

        return Collections.unmodifiableList(out);
    }
}