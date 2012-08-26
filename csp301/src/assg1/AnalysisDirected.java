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
		
		Graph sccG = new Graph(true);
		sccG.addColumn("subGraph", Graph.class);
		sccG.addColumn("0", int.class);
		sccG.addColumn("1", int.class);
		sccG.addColumn("size", int.class);
		sccG.addColumn("label", String.class);
		sccG.addColumn("id", int.class);
		
		Graph[] sgs = new Graph[g.getNodeCount()];
		String[] sgsLabels = new String[g.getNodeCount()];
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
				t.set("value", I.getInt("value"));
				t.set("leader", I.getInt("leader"));
				t.set("label", I.getString("label"));
				t.set("source", I.getString("source"));
				t.set("id", 0);
				t.set("idOld", i);
				sgsLabels[i] = new String(I.getString("label"));
				I.set("set", true);
				int c = 0, loop = 0;
				while (true) {
					try {
						Iterator<Node> neitr = g.getNode(sgs[i].getNode(loop).getInt("idOld")).neighbors();

						while (neitr.hasNext()) {
							Node n = neitr.next();
							if (n.getInt("leader") != i
									|| (boolean) n.get("set"))
								continue;
							Node temp = sgs[i].addNode();
							c++;
							temp.set("id", c);
							temp.set("idOld", n.getInt("id"));
							temp.set("value", n.getString("value"));
							temp.set("leader", n.getInt("leader"));
							temp.set("label", n.getString("label"));
							temp.set("source", n.getString("source"));
							sgsLabels[i] = new String(sgsLabels[i] + n.getString("label"));
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

		int leaderMap[] = new int[g.getNodeCount()], l = 0;
		for (int i = 0; i < g.getNodeCount(); ++i)
			if (sgs[i] != null) {
				leaderMap[i] = l;
				Node sn = sccG.addNode();
				sn.set("subGraph", sgs[i]);
				sn.set("id", l++);
				// System.out.println(i+": "+sgs[i].getNodeCount());
				sn.set("size", sgs[i].getNodeCount());
				sn.set("0", 0);
				sn.set("1", 0);
				sn.set("label", sgsLabels[i]);
				for (int j = 0; j < sgs[i].getNodeCount(); ++j)
					if (((Node) sgs[i].getNode(j)).getInt("value") == 0)
						sn.set("0", (int) sn.get("0") + 1);
					else
						sn.set("1", (int) sn.get("1") + 1);
			}
		

		HashSet<Integer> leaders = new HashSet<Integer>();
		for(int i=0; i<g.getNodeCount(); ++i)
			leaders.add(g.getNode(i).getInt("leader"));
		System.out.println(leaders+"\n"+g.getEdgeCount() + "\n");
		
		Iterator<Edge> ie = g.edges();
		while(ie.hasNext())	{
			Edge t = ie.next();
			int a = g.getNode(t.getInt("source")).getInt("leader"), b = g.getNode(t.getInt("target")).getInt("leader");
			if(a != b)
				sccG.addEdge(leaderMap[a], leaderMap[b]);
		}
		
		Graph sg2 = new Graph(true);
		sg2.addColumn("subGraph", Graph.class);
		sg2.addColumn("0", int.class);
		sg2.addColumn("1", int.class);
		sg2.addColumn("size", int.class);
		sg2.addColumn("label", String.class);
		sg2.addColumn("id", int.class);
		Iterator<Node> in = sccG.nodes();
		int i = 0;
		while(in.hasNext())	{
			Node t = in.next();
			
			if(!t.edges().hasNext())
				sccG.removeNode(t);
			else	{
				Node u = sg2.addNode();
				u.set("subGraph", t.get("subGraph"));
				u.set("0", t.get("0"));
				u.set("1", t.get("1"));
				u.set("size", t.get("size"));
				u.set("label", t.get("label"));
				u.set("id", i);
				t.set("id",i++);
			}
		}
		
		Iterator<Edge> ed = sccG.edges();
		while(ed.hasNext())	{
			Edge a = ed.next();
			sg2.addEdge(a.getSourceNode().getInt("id"), a.getTargetNode().getInt("id"));
		}
		
		
		
		return sg2;
	}

	public static void DFS(Node n, int src) {
		n.setBoolean("explored", true);
		n.setInt("leader", src);
		Iterator<Node> neighbors = n.outNeighbors();
		while (neighbors.hasNext()) {
			Node temp = neighbors.next();
			if (!temp.getBoolean("explored"))
				DFS(temp, src);
		}
	}

	public static void DFSRev(Node n) {
		n.setBoolean("exploredRev", true);
		Iterator<Node> neighbors = n.inNeighbors();
		while (neighbors.hasNext()) {
			Node temp = neighbors.next();
			if (!temp.getBoolean("exploredRev"))
				DFSRev(temp);
		}
		s.push(n);
	}

	public static int countTriangles(Graph g) {
		int c = 0;
		g.addColumn("close", HashSet.class, null);
		for (int i = 0; i < g.getNodeCount(); ++i)
			g.getNode(i).set("close", new HashSet<Node>());
		Iterator<Node> nodes = g.nodes();
		while (nodes.hasNext()) {
			Node s = nodes.next();
			Iterator<Node> neighbor = s.neighbors();
			while (neighbor.hasNext()) {
				Node t = neighbor.next();
				if (t.getInt("id") > s.getInt("id")) {
					Set<Node> intersection = new HashSet(
							(HashSet<Node>) s.get("close"));
					intersection.retainAll((HashSet<Node>) t.get("close"));
					c += intersection.size();
					((HashSet<Node>) t.get("close")).add(s);
				}
			}
		}
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
				System.out.println(((Graph)g.getNode(i).get("subGraph")).getNodeCount());
			}
		System.out.println(g.getNodeCount() + " " + c + " " + triangleBrute(g));

		UILib.setPlatformLookAndFeel();
		GraphicsEnvironment e = GraphicsEnvironment
				.getLocalGraphicsEnvironment();
		JFrame frame = GraphView.demo(g,"id");

		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
	}
}
