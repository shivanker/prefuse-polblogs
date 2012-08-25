package assg1;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.Stack;

import prefuse.data.Edge;
import prefuse.data.Graph;
import prefuse.data.Node;
import prefuse.data.Table;
import prefuse.data.io.DataIOException;
import prefuse.data.io.GraphMLReader;

public class AnalysisUndirected {
	static Stack<Node> s = new Stack<Node>();

	public static Table nodalAnalysis(Graph g) {
		Iterator<Node> nodes = g.nodes();
		Table tb = new Table();
		tb.addColumn("Conservative", int.class, 0);
		tb.addColumn("Liberal", int.class, 0);
		tb.addColumn("Neutral", int.class, 0);
		tb.addRows(g.getNodeCount());
		while (nodes.hasNext()) {
			Node temp = nodes.next();
			Iterator<Node> neighbor = temp.neighbors();
			int c = 0, l = 0, n = 0;
			while (neighbor.hasNext()) {
				Node t = neighbor.next();
				if (t.get("value") == "c")
					c++;
				else if (t.get("value") == "l")
					l++;
				else
					n++;
			}
			tb.setInt((int) temp.get("id"), "Conservative", c);
			tb.setInt((int) temp.get("id"), "Liberal", l);
			tb.setInt((int) temp.get("id"), "Neutral", n);
		}
		return tb;
	}
	
	public static tuple countTrianglesAndNetworkClusteringCoefficient(Graph g) {
		tuple c = new tuple(0,0.0);
		g.addColumn("Triangles", int.class, 0);
		g.addColumn("localClusteringCoefficient", double.class, 0);
		g.addColumn("close", HashSet.class, null);
		for(int i=0; i<g.getNodeCount(); ++i)
			g.getNode(i).set("close", new HashSet<Node>());
		Iterator<Node> nodes = g.nodes();
		while (nodes.hasNext()) {
			Node s = nodes.next();
			Iterator<Node> neighbor = s.neighbors();
			while (neighbor.hasNext()) {
				Node t = neighbor.next();
				if (t.getInt("id") > s.getInt("id")) {
					Set<Node> intersection = new HashSet((HashSet<Node>) s.get("close"));
					intersection.retainAll((HashSet<Node>) t.get("close"));
					int n = intersection.size();
					c.Triangles += n;
					s.setInt("Triangles", s.getInt("Triangles")+n);
					t.setInt("Triangles", t.getInt("Triangles")+n);
					Iterator<Node> i = intersection.iterator();
					while (i.hasNext())
					{
						Node temp = i.next();
						temp.setInt("Triangles", temp.getInt("Triangles")+1);
					}
					((HashSet<Node>) t.get("close")).add(s);
				}
			}
		}
		Iterator<Node> node = g.nodes();
		while (node.hasNext())
		{
			Node temp = node.next();
			int n = temp.getDegree();
			temp.setDouble("localClusteringCoefficient", (2*temp.getInt("Triangles"))/(n*(n-1)));
			c.Clustering += temp.getDouble("localClusteringCoefficient");
		}
		c.Clustering /= (double)(g.getNodeCount());
		return c;
	}
	public static int classifyEdges(Graph g) {
		int sameEdge = 0;
		Iterator<Edge> i = g.edges();
		while (i.hasNext()) {
			Edge temp = i.next();
			if (temp.getSourceNode().get("value") == temp.getTargetNode().get(
					"value"))
				sameEdge++;
		}
		return sameEdge;
	}

	public static void main(String... args) throws DataIOException	{
		Graph polbooks = new GraphMLReader().readGraph("polbooks.xml");
		polbooks.addColumn("id", int.class);
		Iterator<Node> n = polbooks.nodes();
		int i = 0;
		while(n.hasNext())	{
			n.next().set("id", i++);
		}

		System.out.println(countTrianglesAndNetworkClusteringCoefficient(polbooks).Triangles);
		System.out.println(countTrianglesAndNetworkClusteringCoefficient(polbooks).Clustering);
	}
}
class tuple
{
	int Triangles;
	double Clustering;
	tuple(int a, double b)
	{
		Triangles = a;
		Clustering = b;
	}
}
