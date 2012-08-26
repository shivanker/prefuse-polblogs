package assg1;

import java.util.Arrays;
import java.util.Iterator;
import java.util.LinkedList;

import prefuse.data.Graph;
import prefuse.data.Node;
import prefuse.data.io.DataIOException;
import prefuse.data.io.GraphMLReader;

public class connectedComponents {

	static int size;
	public static void DFS(Node n) {
		n.setBoolean("explored", true);
		size++;
		Iterator<Node> neighbors = n.neighbors();
		while (neighbors.hasNext()) {
			Node temp = neighbors.next();
			if (!temp.getBoolean("explored"))
				DFS(temp);
		}
	}

	public static LinkedList<Integer> components(Graph g)	{		
		g.addColumn("explored", boolean.class, false);
		
		LinkedList<Integer> result = new LinkedList<Integer>();
		for(int i=0; i<g.getNodeCount(); ++i)
			if(!g.getNode(i).getBoolean("explored"))	{
				size = 0;
				DFS(g.getNode(i));
				result.add(size);
			}
		return result;
	}
	
	public static void main(String[] args) throws DataIOException {
		Graph g = new GraphMLReader().readGraph("polblogs.xml");
		Object[] comps = components(g).toArray();
		Arrays.sort(comps);
		int n = comps.length;
		System.out.println("Total number of disconnected components: " + comps[n-1]);
		
		/** It was found that polbooks is a connected graph,
		 * while polblogs contains a total of 269 disconnected components,
		 * of which,  one is of size 1221, one is a pair, while all others are singlets.*/ 
	}

}
