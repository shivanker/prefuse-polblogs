package assg1;

import java.util.Iterator;
import java.util.HashSet;
import java.util.Set;
import java.util.Stack;

import prefuse.data.Edge;
import prefuse.data.Graph;
import prefuse.data.Node;
import prefuse.data.Table;

public class Analysis
{
	static Stack<Node> s = new Stack<Node>();
	public static Table nodalAnalysis(Graph g)
	{
		Iterator<Node> nodes = g.nodes();
		Table tb = new Table();
		tb.addColumn("Conservative", int.class, 0);
		tb.addColumn("Liberal", int.class, 0);
		tb.addColumn("Neutral", int.class, 0);
		tb.addRows(g.getNodeCount());
		while (nodes.hasNext())
		{
			Node temp = nodes.next();
			Iterator<Node> neighbor = temp.neighbors();
			int c=0, l=0, n=0;
			while (neighbor.hasNext())
			{
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
	public static void setSCC(Graph g)
	{
		g.addColumn("explored", boolean.class, false);
		g.addColumn("exploredRev", boolean.class, false);
		g.addColumn("leader", int.class);
		for (int i = g.getNodeCount()-1; i>=0; i--)
			if (!g.getNode(i).getBoolean("exploredRev"))
				DFSRev(g.getNode(i));
		while (!s.isEmpty())
		{
			if (!s.peek().getBoolean("explored"))
				DFS(s.peek(), s.pop().getInt("id"));
			else
				s.pop();
		}
	}
	public static void DFS(Node n, int src)
	{
		n.setBoolean("explored", true);
		n.setInt("leader", src);
		Iterator<Node> neighbors = n.neighbors();
		while (neighbors.hasNext())
		{
			Node temp =neighbors.next(); 
			if (!temp.getBoolean("explored"))
				DFS(temp, src);
		}
	}
	public static void DFSRev(Node n)
	{
		n.setBoolean("exploredRev", true);
		Iterator<Node> neighbors = n.neighbors();
		while (neighbors.hasNext())
		{
			Node temp =neighbors.next(); 
			if (!temp.getBoolean("exploredRev"))
				DFSRev(temp);
		}
		s.push(n);
	}
	public static int countTriangles(Graph g)
	{
		int c = 0;
		g.addColumn("close", HashSet.class, null);
		Iterator<Node> nodes = g.nodes();
		while (nodes.hasNext())
		{
			Node s = nodes.next();
			Iterator<Node> neighbor = s.neighbors();
			while (neighbor.hasNext())
			{
				Node t = neighbor.next();
				if (t.getInt("id") > s.getInt("id"))
				{
					Set<Node> intersection = new HashSet((HashSet<Node>) s.get("close"));
					intersection.retainAll((HashSet<Node>) t.get("close"));
					c+=intersection.size();
					((HashSet<Node>)t.get("close")).add(s);
				}
			}	
		}
		return c;
	}
	public static int classifyEdges(Graph g)
	{
		int sameEdge = 0;
		Iterator<Edge> i = g.edges();
		while (i.hasNext())
		{
			Edge temp = i.next();
			if (temp.getSourceNode().get("value") == temp.getTargetNode().get("value"))
				sameEdge++;
		}
		return sameEdge;
	}
	public static long nC3 (int n)
	{
		return ((long)n*(n-1)*(n-2))/6;
	}
}