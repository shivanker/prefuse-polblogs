package assg1;

import java.util.Iterator;

import prefuse.data.Graph;
import prefuse.data.Node;

public class connectedComponents {

	public static void DFS(Node n, int src) {
		n.setBoolean("explored", true);
		Iterator<Node> neighbors = n.neighbors();
		while (neighbors.hasNext()) {
			Node temp = neighbors.next();
			if (!temp.getBoolean("explored"))
				DFS(temp, src);
		}
	}

	public static Graph[] components(Graph g)	{
		g.addColumn("explored", boolean.class, false);
		return null;
	}
	public static void main(String[] args) {
		// TODO Auto-generated method stub

	}

}
