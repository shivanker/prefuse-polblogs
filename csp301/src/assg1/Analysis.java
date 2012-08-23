package assg1;

import java.util.Iterator;

import prefuse.data.Edge;
import prefuse.data.Graph;
import prefuse.data.Node;
import prefuse.data.Table;

public class Analysis {
	public static Table nodalAnalysis(Graph g)
	{
		Iterator<Node> nodes = g.nodes();
		while (nodes.hasNext())
		{
			Node temp = nodes.next();
			Iterator<Edge> neighbor = temp.edges();
			int c=0, l=0, n=0;
			while (neighbor.hasNext())
			{
				Edge t = neighbor.next();
				switch (t.getTargetNode().)
			}
		}
		return null;
	}
}
