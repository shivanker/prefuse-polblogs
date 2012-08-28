package assg1;

import java.io.BufferedWriter;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.Stack;

import prefuse.data.Edge;
import prefuse.data.Graph;
import prefuse.data.Node;
import prefuse.data.Table;
import prefuse.data.io.CSVTableWriter;
import prefuse.data.io.DataIOException;
import prefuse.data.io.GraphMLReader;

@SuppressWarnings("unchecked")
public class AnalysisDirected {
	static Stack<Node> s = new Stack<Node>();

	public static Table nodalAnalysis(Graph g) {
		g.addColumn("Triangles", int.class, 0);
		g.addColumn("close", HashSet.class, null);
		for(int i=0; i<g.getNodeCount(); ++i)
			g.getNode(i).set("close", new HashSet<Node>());
		Iterator<Node> nodes = g.nodes();
		Table tb = new Table();
		tb.addColumn("Name", String.class);
		tb.addColumn("Affliation", String.class);
		tb.addColumn("Conservative", int.class, 0);
		tb.addColumn("Liberal", int.class, 0);
		tb.addColumn("Total", int.class, 0);
		tb.addColumn("Triangles", int.class, 0);
		tb.addRows(g.getNodeCount());
		while (nodes.hasNext()) {
			Node temp = nodes.next();
			Iterator<Node> neighbor = temp.neighbors();
			int c = 0, l = 0, tot = 0;
			while (neighbor.hasNext()) {
				Node t = neighbor.next();
				if (t.getInt("value") == 1)
					c++;
				else
					l++;
				tot++;
				if (temp.getInt("id") > t.getInt("id")) {
					Set<Node> intersection = new HashSet<Node>((HashSet<Node>) t.get("close"));
					intersection.retainAll((HashSet<Node>) temp.get("close"));
					int n1 = intersection.size();
					t.setInt("Triangles", t.getInt("Triangles")+n1);
					temp.setInt("Triangles", temp.getInt("Triangles")+n1);
					Iterator<Node> i = intersection.iterator();
					while (i.hasNext())
					{
						Node nod = i.next();
						nod.setInt("Triangles", nod.getInt("Triangles")+1);
					}
					((HashSet<Node>) temp.get("close")).add(t);
				}
			}
			tb.setInt((int) temp.get("id"), "Conservative", c);
			tb.setInt((int) temp.get("id"), "Liberal", l);
			tb.setInt(temp.getInt("id"), "Total", tot);
			tb.setInt(temp.getInt("id"), "Triangles", temp.getInt("Triangles"));
			tb.setString((int) temp.get("id"), "Affliation", (temp.getInt("value")==1)?"Conservative":"Liberal");
			tb.setString((int) temp.get("id"), "Name", (String) temp.get("label"));
		}
		return tb;
	}
	
	public static Graph cliques3(Graph g)	{
		g.addColumn("leader", int.class,-1);
		g.addColumn("idNew", int.class);
		g.addColumn("close", HashSet.class, null);
		for(int i=0; i<g.getNodeCount(); ++i)
			g.getNode(i).set("close", new HashSet<Node>());
		Iterator<Node> nodes = g.nodes();
		while(nodes.hasNext())	{
			Node temp = nodes.next();
			Iterator<Node> neighbor = temp.neighbors();
			while (neighbor.hasNext()) {
				Node t = neighbor.next();
				if (temp.getInt("id") < t.getInt("id")) {
					Set<Node> intersection = new HashSet<Node>((HashSet<Node>) t.get("close"));
					intersection.retainAll((HashSet<Node>) temp.get("close"));
					Iterator<Node> i = intersection.iterator();
					while (i.hasNext() && t.getInt("leader") == -1 && temp.getInt("leader") == -1)
					{
						Node c = i.next();
						if(c.getInt("leader") == -1 && c.getInt("value") == t.getInt("value") && t.getInt("value") == temp.getInt("value") &&  
								((g.getEdge(temp,t) != null && g.getEdge(t,c) != null && g.getEdge(c,temp) != null) || 
										(g.getEdge(t,temp) != null && g.getEdge(temp,c) != null && g.getEdge(c,t) != null)))	{
							t.set("leader", temp.getInt("id"));
							temp.set("leader", temp.getInt("id"));
							c.set("leader", temp.getInt("id"));
						}
					}
					((HashSet<Node>) t.get("close")).add(temp);
				}
			}
		}
		
		Graph triG = new Graph(true);
		triG.addColumn("subGraph", Graph.class);
		triG.addColumn("value", int.class);
		triG.addColumn("size", int.class);
		triG.addColumn("label", String.class);
		triG.addColumn("id", int.class);

		// making the scc's as new graphs
		Graph[] tcs = new Graph[g.getNodeCount()];
		String[] tcsLabels = new String[g.getNodeCount()];
		g.addColumn("set", boolean.class, false);

		for (int i = 0; i < g.getNodeCount(); ++i)
			if (g.getNode(i).getInt("leader") == -1 || g.getNode(i).getInt("leader") == i) {
				g.getNode(i).set("leader", i);
				
				Node I = g.getNode(i);

				tcs[i] = new Graph(true);
				tcs[i].addColumn("id", int.class);
				tcs[i].addColumn("idOld", int.class);
				tcs[i].addColumn("label", String.class);
				tcs[i].addColumn("value", int.class);
				tcs[i].addColumn("source", String.class);
				tcs[i].addColumn("leader", int.class);

				Node t = tcs[i].addNode();
				t.set("value", I.getInt("value"));
				t.set("leader", I.getInt("leader"));
				t.set("label", I.getString("label"));
				t.set("source", I.getString("source"));
				t.set("id", 0);
				t.set("idOld", i);
				I.set("idNew", 0);
				
				tcsLabels[i] = new String(I.getString("label"));
				I.set("set", true);
				int c = 0, loop = 0;
				while (true) {
					try {
						Iterator<Node> neitr = g.getNode(
								tcs[i].getNode(loop).getInt("idOld"))
								.neighbors();

						while (neitr.hasNext()) {
							
							Node n = neitr.next();
							if (n.getInt("leader") != i
									|| (boolean) n.get("set"))
								continue;
							Node temp = tcs[i].addNode();
							c++;
							
							temp.set("id", c);
							temp.set("idOld", n.getInt("id"));
							temp.set("value", n.getString("value"));
							temp.set("leader", n.getInt("leader"));
							temp.set("label", n.getString("label"));
							temp.set("source", n.getString("source"));
							n.set("idNew", c);
							
							tcsLabels[i] = new String(tcsLabels[i]
									+ " " + n.getString("label"));
							n.set("set", true);
						}

						loop++;
					} catch (Exception e) {
						break;
					}
				}
				
				// adding the edges
				Iterator<Node> tcsNodes = tcs[i].nodes();
				while(tcsNodes.hasNext())	{
					
					Node tcSrc = tcsNodes.next();
					Node oldSrc = g.getNode(tcSrc.getInt("idOld"));
					
					Iterator<Node> edges = oldSrc.outNeighbors();
					
					while(edges.hasNext())	{
						
						Node oldTrg = edges.next();
						if(oldTrg.getInt("leader") == i)
							tcs[i].addEdge(tcSrc.getInt("id"), oldTrg.getInt("idNew"));
						
					}
				}
			}

		int leaderMap[] = new int[g.getNodeCount()], l = 0;
		for (int i = 0; i < g.getNodeCount(); ++i)
			if (tcs[i] != null) {
				leaderMap[i] = l;
				Node tn = triG.addNode();
				tn.set("subGraph", tcs[i]);
				tn.set("id", l++);
				tn.set("size", tcs[i].getNodeCount());
				tn.set("value", tcs[i].getNode(0).getInt("value"));
				tn.set("label", tcsLabels[i]);
			}

		HashSet<Integer> leaders = new HashSet<Integer>();
		for (int i = 0; i < g.getNodeCount(); ++i)
			leaders.add(g.getNode(i).getInt("leader"));

		Iterator<Edge> ie = g.edges();
		while (ie.hasNext()) {
			Edge t = ie.next();
			int a = g.getNode(t.getInt("source")).getInt("leader"), b = g.getNode(t.getInt("target")).getInt("leader");
			if (a != b)
				triG.addEdge(leaderMap[a], leaderMap[b]);
		}
		
		Graph tg2 = new Graph(true);
		tg2.addColumn("subGraph", Graph.class);
		tg2.addColumn("value", int.class);
		tg2.addColumn("size", int.class);
		tg2.addColumn("label", String.class);
		tg2.addColumn("id", int.class);
		Iterator<Node> in = triG.nodes();
		int i = 0;
		while (in.hasNext()) {
			Node t = in.next();
			if (!t.edges().hasNext())
				triG.removeNode(t);
			else {
				Node u = tg2.addNode();
				u.set("subGraph", t.get("subGraph"));
				u.set("value", t.getInt("value"));
				u.set("size", t.get("size"));
				u.set("label", t.get("label"));
				u.set("id", i);
				t.set("id", i++);
			}
		}

		Iterator<Edge> ed = triG.edges();
		while (ed.hasNext()) {
			Edge a = ed.next();
			tg2.addEdge(a.getSourceNode().getInt("id"), a.getTargetNode()
					.getInt("id"));
		}

		return tg2;

	}

	public static Graph setSCC(Graph g) {
		g.addColumn("explored", boolean.class, false);
		g.addColumn("exploredRev", boolean.class, false);
		g.addColumn("leader", int.class);
		g.addColumn("idNew", int.class);
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

		// making the scc's as new graphs
		Graph[] sgs = new Graph[g.getNodeCount()];
		String[] sgsLabels = new String[g.getNodeCount()];
		g.addColumn("set", boolean.class, false);

		for (int i = 0; i < g.getNodeCount(); ++i)
			if (g.getNode(i).getInt("leader") == i) {
				Node I = g.getNode(i);

				sgs[i] = new Graph(true);
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
				I.set("idNew", 0);
				
				sgsLabels[i] = new String(I.getString("label"));
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
							temp.set("value", n.getString("value"));
							temp.set("leader", n.getInt("leader"));
							temp.set("label", n.getString("label"));
							temp.set("source", n.getString("source"));
							n.set("idNew", c);
							
							sgsLabels[i] = new String(sgsLabels[i]
									+ " " + n.getString("label"));
							n.set("set", true);
						}

						loop++;
					} catch (Exception e) {
						break;
					}
				}
				
				// adding the edges
				Iterator<Node> sgsNodes = sgs[i].nodes();
				while(sgsNodes.hasNext())	{
					
					Node sgSrc = sgsNodes.next();
					Node oldSrc = g.getNode(sgSrc.getInt("idOld"));
					
					Iterator<Node> edges = oldSrc.outNeighbors();
					
					while(edges.hasNext())	{
						
						Node oldTrg = edges.next();
						if(oldTrg.getInt("leader") == i)
							sgs[i].addEdge(sgSrc.getInt("id"), oldTrg.getInt("idNew"));
						
					}
				}
			}

		int leaderMap[] = new int[g.getNodeCount()], l = 0;
		for (int i = 0; i < g.getNodeCount(); ++i)
			if (sgs[i] != null) {
				leaderMap[i] = l;
				Node sn = sccG.addNode();
				sn.set("subGraph", sgs[i]);
				sn.set("id", l++);
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
		for (int i = 0; i < g.getNodeCount(); ++i)
			leaders.add(g.getNode(i).getInt("leader"));

		Iterator<Edge> ie = g.edges();
		while (ie.hasNext()) {
			Edge t = ie.next();
			int a = g.getNode(t.getInt("source")).getInt("leader"), b = g
					.getNode(t.getInt("target")).getInt("leader");
			if (a != b)
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
		while (in.hasNext()) {
			Node t = in.next();
			if (!t.edges().hasNext())
				sccG.removeNode(t);
			else {
				Node u = sg2.addNode();
				u.set("subGraph", t.get("subGraph"));
				u.set("0", t.get("0"));
				u.set("1", t.get("1"));
				u.set("size", t.get("size"));
				u.set("label", t.get("label"));
				u.set("id", i);
				t.set("id", i++);
			}
		}

		Iterator<Edge> ed = sccG.edges();
		while (ed.hasNext()) {
			Edge a = ed.next();
			sg2.addEdge(a.getSourceNode().getInt("id"), a.getTargetNode()
					.getInt("id"));
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

	public static tuple countTriangles(Graph g) {
		tuple c = new tuple(0, 0.0);
		g.addColumn("Numerator", int.class, 0);
		g.addColumn("Triangles", int.class, 0);
		g.addColumn("localClusteringCoefficient", double.class, 0);
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
					Set<Node> intersection = new HashSet<Node>(
							(HashSet<Node>) s.get("close"));
					intersection.retainAll((HashSet<Node>) t.get("close"));
					int n = intersection.size();
					c.Triangles += n;
					s.setInt("Triangles", s.getInt("Triangles") + n);
					t.setInt("Triangles", t.getInt("Triangles") + n);
					Iterator<Node> i = intersection.iterator();
					while (i.hasNext()) {
						Node temp = i.next();
						int k = ((g.getEdge(s, t) == null ? 0 : 1) + (g
								.getEdge(t, s) == null ? 0 : 1))
								* ((g.getEdge(s, temp) == null ? 0 : 1) + (g
										.getEdge(temp, s) == null ? 0 : 1))
								* ((g.getEdge(temp, t) == null ? 0 : 1) + (g
										.getEdge(t, temp) == null ? 0 : 1));
						s.setInt("Numerator", s.getInt("Numerator") + k);
						t.setInt("Numerator", t.getInt("Numerator") + k);
						temp.setInt("Numerator", temp.getInt("Numerator") + k);
						temp.setInt("Triangles", temp.getInt("Triangles") + 1);
					}
					((HashSet<Node>) t.get("close")).add(s);
				}
			}
		}
		Iterator<Node> node = g.nodes();
		int[] Triangles = new int[g.getNodeCount()];
		int[] Degrees = new int[g.getNodeCount()];
		int i = 0;
		while (node.hasNext()) {
			Node temp = node.next();
			int n = temp.getDegree();
			int d = 0;
			Iterator<Node> I = temp.outNeighbors();
			while (I.hasNext()) {
				Node t = I.next();
				if (g.getEdge(t, temp) != null)
					d++;
			}
			int denominator = 2 * ((n * (n - 1)) - 2 * d);
			temp.setDouble("localClusteringCoefficient",
					(denominator > 0) ? (temp.getInt("Numerator"))
							/ (double) denominator : 0);
			c.Clustering += temp.getDouble("localClusteringCoefficient");
			Triangles[i] = temp.getInt("Triangles");
			Degrees[i++] = temp.getDegree();
		}
		c.Clustering /= (double) (g.getNodeCount());
		Statistics st = new Statistics();
		c.Pearson = st.PearsonStatistic(Triangles, Degrees);
		return c;
	}

	public static int classifyEdges(Graph g) {
		int sameEdge = 0;
		Iterator<Edge> i = g.edges();
		while (i.hasNext()) {
			Edge temp = i.next();
			if (temp.getSourceNode().getString("value")
					.equals(temp.getTargetNode().getString("value")))
				sameEdge++;
		}
		return sameEdge;
	}

	public static void main(String... args) throws DataIOException, IOException {
		Graph f = new GraphMLReader().readGraph("polblogs.xml");
		f.addColumn("id", int.class);
		Iterator<Node> n = f.nodes();
		int i = 0;
		while (n.hasNext()) {
			n.next().set("id", i++);
		}
		BufferedWriter bw = new BufferedWriter(new FileWriter("blogsGraphAnalysis.csv"));
		tuple t = countTriangles(f);
		FileOutputStream fos = new FileOutputStream("polblogsAnalysis.csv");
		Table tb = nodalAnalysis(f);
		CSVTableWriter tw = new CSVTableWriter();
		tw.writeTable(tb, fos);
		fos.close();
		bw.write("\"File Name\",\"Average Network Clustering Coefficient\",\"Edge Ratio\",\"Pearson\'s Correlation Coefficient\"");
		bw.newLine();
		bw.write("polblogs.xml," + t.Clustering + ","
				+ ((double) classifyEdges(f) / f.getEdgeCount()) + ","
				+ t.Pearson);
		bw.newLine();
		for (int j = 1; j <= 50; j++) {
			String filename = "polblogs_rand_" + j + ".xml";
			f = new GraphMLReader().readGraph("polblogs_rand_\\" + filename);
			f.addColumn("id", int.class);
			n = f.nodes();
			i = 0;
			while (n.hasNext()) {
				n.next().set("id", i++);

			}
			t = countTriangles(f);
			bw.write(filename + "," + t.Clustering + ","
					+ ((double) classifyEdges(f) / f.getEdgeCount()) + ","
					+ t.Pearson);
			bw.newLine();
		}
		bw.close();
	}
}
