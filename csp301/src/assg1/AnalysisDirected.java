package assg1;

import java.awt.GraphicsEnvironment;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.Stack;

import javax.swing.JFrame;

import prefuse.data.Edge;
import prefuse.data.Graph;
import prefuse.data.Node;
import prefuse.data.Table;
import prefuse.data.io.DataIOException;
import prefuse.data.io.GraphMLReader;
import prefuse.util.ui.UILib;

public class AnalysisDirected {
	static Stack<Node> s = new Stack<Node>();

	public static Table nodalAnalysis(Graph g) {
		Iterator<Node> nodes = g.nodes();
		Table tb = new Table();
		tb.addColumn("Conservative", int.class, 0);
		tb.addColumn("Liberal", int.class, 0);
		tb.addRows(g.getNodeCount());
		while (nodes.hasNext()) {
			Node temp = nodes.next();
			Iterator<Node> neighbor = temp.neighbors();
			int c = 0, l = 0;
			while (neighbor.hasNext()) {
				Node t = neighbor.next();
				if (t.get("value") == "c")
					c++;
				else
					l++;
			}
			tb.setInt((int) temp.get("id"), "Conservative", c);
			tb.setInt((int) temp.get("id"), "Liberal", l);
		}
		return tb;
	}

	public static Graph setSCC(Graph g) {
		g.addColumn("explored", boolean.class, false);
		g.addColumn("exploredRev", boolean.class, false);
		g.addColumn("leader", int.class);
		for (int i = g.getNodeCount() - 1; i >= 0; i--)
			if (!g.getNode(i).getBoolean("exploredRev"))
				DFSRev(g.getNode(i));
		while (!s.isEmpty()) {
			if (!s.peek().getBoolean("explored"))
				DFS(s.peek(), s.pop().getInt("id"));
			else
				s.pop();
		}
		Graph sccG = new Graph(false);
		sccG.addColumn("subGraph", Graph.class);
		sccG.addColumn("l", int.class);
		sccG.addColumn("n", int.class);
		sccG.addColumn("c", int.class);
		sccG.addColumn("size", int.class);
		Graph[] sgs = new Graph[g.getNodeCount()];
		g.addColumn("set", boolean.class, false);

		for (int i = 0; i < g.getNodeCount(); ++i)
			if (g.getNode(i).getInt("leader") == i) {
				Node I = g.getNode(i);

				sgs[i] = new Graph(false);
				sgs[i].addColumn("id", int.class);
				sgs[i].addColumn("idOld", int.class);
				sgs[i].addColumn("label", String.class);
				sgs[i].addColumn("value", int.class);
				sgs[i].addColumn("source", String.class);
				sgs[i].addColumn("leader", int.class);

				Node t = sgs[i].addNode();
				t.set("label", I.getString("label"));
				t.set("value", I.getInt("value"));
				t.set("leader", I.getInt("leader"));
				t.set("source", I.getString("source"));
				t.set("id", 0);
				t.set("idOld", i);
				I.set("set", true);
				int c = 0, loop = 0;
				while (true) {
					try {
						Iterator<Node> neitr = g.getNode(
								sgs[i].getNode(loop).getInt("idOld"))
								.neighbors();

						while (neitr.hasNext()) {
							Node n = neitr.next();
							if (n.getInt("leader") != i
									|| (boolean) n.get("set"))
								continue;
							Node temp = sgs[i].addNode();
							c++;
							temp.set("id", c);
							temp.set("idOld", n.getInt("id"));
							temp.set("label", n.getString("label"));
							temp.set("value", n.getString("value"));
							temp.set("leader", n.getInt("leader"));
							temp.set("source", n.getString("source"));
							n.set("set", true);
						}

						loop++;
					} catch (Exception e) {
						break;
					}
				}
				for (int j = 0; j < sgs[i].getNodeCount(); ++j)
					for (int k = j + 1; k < sgs[i].getNodeCount(); ++k)
						sgs[i].addEdge(j, k);
			}

		for (int i = 0; i < g.getNodeCount(); ++i)
			if (sgs[i] != null) {
				Node sn = sccG.addNode();
				sn.set("subGraph", sgs[i]);
				// System.out.println(i+": "+sgs[i].getNodeCount());
				sn.set("size", sgs[i].getNodeCount());
				sn.set("n", 0);
				sn.set("c", 0);
				sn.set("l", 0);
				for (int j = 0; j < sgs[i].getNodeCount(); ++j)
					if (((Node) sgs[i].getNode(j)).get("value").equals("n"))
						sn.set("n", (int) sn.get("n") + 1);
					else if (((Node) sgs[i].getNode(j)).get("value")
							.equals("c"))
						sn.set("c", (int) sn.get("c") + 1);
					else
						sn.set("l", (int) sn.get("l") + 1);
			}
		return sccG;
	}

	public static void DFS(Node n, int src) {
		n.setBoolean("explored", true);
		n.setInt("leader", src);
		Iterator<Node> neighbors = n.neighbors();
		while (neighbors.hasNext()) {
			Node temp = neighbors.next();
			if (!temp.getBoolean("explored"))
				DFS(temp, src);
		}
	}

	public static void DFSRev(Node n) {
		n.setBoolean("exploredRev", true);
		Iterator<Node> neighbors = n.neighbors();
		while (neighbors.hasNext()) {
			Node temp = neighbors.next();
			if (!temp.getBoolean("exploredRev"))
				DFSRev(temp);
		}
		s.push(n);
	}

	public static tuple countTriangles(Graph g) {
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
			int d = 0;
			Iterator<Node> I = temp.outNeighbors();
			while (I.hasNext())
			{
				Node t = I.next();
				if (g.getEdge(t,temp)!=null)
					d++;
			}
			int denominator = (n*(n-1)) - 2*d;
			temp.setDouble("localClusteringCoefficient", (denominator>0)?(temp.getInt("Triangles"))/(double)denominator:0);
			c.Clustering += temp.getDouble("localClusteringCoefficient");
		}
		c.Clustering /= (double)(g.getNodeCount());
		return c;
	}

	public static float triangleBrute(Graph g) {
		int c = 0;
		Iterator<Node> i = g.nodes(), j = g.nodes(), k = g.nodes();
		Node n1, n2, n3;
		while (i.hasNext()) {
			n1 = i.next();
			while (j.hasNext()) {
				n2 = j.next();
				while (k.hasNext()) {
					n3 = k.next();
					if (g.getEdge(n1, n2) != null && g.getEdge(n2, n3) != null
							&& g.getEdge(n3, n1) != null)
						c++;
				}
			}
		}
		return (float) c / 3.0f;
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

	public static void main(String... args) throws DataIOException {
		Graph polbooks = new GraphMLReader().readGraph("polblogs.xml");
		polbooks.addColumn("id", int.class);
		Iterator<Node> n = polbooks.nodes();
		int i = 0;
		while (n.hasNext()) {
			n.next().set("id", i++);
		}
		Graph g = (Graph) setSCC(polbooks);
		int c = 0;
		for (i = 0; i < g.getNodeCount(); ++i)
			if (g.getNode(i).getInt("size") > 1) {
				// g = (Graph) g.getNode(i).get("subGraph");
				// System.out.println(g.getNode(i).getInt("size"));
				c++;
				if (g.getNode(i).getInt("size") > 1) {
					g = (Graph) g.getNode(i).get("subGraph");
					break;
				}
			}
		System.out.println(g.getNodeCount() + " " + c + " " + triangleBrute(g));

		UILib.setPlatformLookAndFeel();
		GraphicsEnvironment e = GraphicsEnvironment
				.getLocalGraphicsEnvironment();
		JFrame frame = graphAlpha.demo(polbooks, "label");

		frame.setMaximizedBounds(e.getMaximumWindowBounds());
		frame.setExtendedState(frame.getExtendedState() | JFrame.MAXIMIZED_BOTH);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
	}
}
