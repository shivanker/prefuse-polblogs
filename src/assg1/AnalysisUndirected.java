package assg1;

import java.io.BufferedWriter;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
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
public class AnalysisUndirected {
	static Stack<Node> s = new Stack<Node>();

	public static Table nodalAnalysis(Graph g) {
		Iterator<Node> nodes = g.nodes();
		Table tb = new Table();
		g.addColumn("Triangles", int.class, 0);
		g.addColumn("close", HashSet.class, null);
		for(int i=0; i<g.getNodeCount(); ++i)
			g.getNode(i).set("close", new HashSet<Node>());
		tb.addColumn("Name", String.class);
		tb.addColumn("Affliation", String.class);
		tb.addColumn("Conservative", int.class, 0);
		tb.addColumn("Liberal", int.class, 0);
		tb.addColumn("Neutral", int.class, 0);
		tb.addColumn("Total", int.class, 0);
		tb.addColumn("Triangles", int.class, 0);
		
		tb.addRows(g.getNodeCount());
		while (nodes.hasNext()) {
			Node temp = nodes.next();
			Iterator<Node> neighbor = temp.neighbors();
			int c = 0, l = 0, n = 0, tot = 0;
			while (neighbor.hasNext()) {
				Node t = neighbor.next();
				if (t.get("value").equals("c"))
					c++;
				else if (t.get("value").equals("l"))
					l++;
				else
					n++;
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
			tb.setInt(temp.getInt("id"), "Conservative", c);
			tb.setInt(temp.getInt("id"), "Liberal", l);
			tb.setInt(temp.getInt("id"), "Neutral", n);
			tb.setInt(temp.getInt("id"), "Total", tot);
			tb.setInt(temp.getInt("id"), "Triangles", temp.getInt("Triangles"));
			tb.setString(temp.getInt("id"), "Affliation", (String) temp.get("value"));
			tb.setString(temp.getInt("id"), "Name", (String) temp.get("label"));
		}
		return tb;
	}
	
	public static tuple countTrianglesAndNetworkClusteringCoefficient(Graph g) {
		Statistics st = new Statistics();
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
					Set<Node> intersection = new HashSet<Node>((HashSet<Node>) s.get("close"));
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
		int [] Triangles = new int[g.getNodeCount()];
		int [] Degrees = new int[g.getNodeCount()];
		int i = 0;
		while (node.hasNext())
		{
			Node temp = node.next();
			int n = temp.getDegree();
			temp.setDouble("localClusteringCoefficient", (n>1)?(2*temp.getInt("Triangles"))/((double)n*(n-1)):0);
			c.Clustering += temp.getDouble("localClusteringCoefficient");
			Triangles[i] = temp.getInt("Triangles");
			Degrees[i++] = temp.getDegree();
		}
		c.Clustering /= (double)(g.getNodeCount());
		c.Pearson = st.PearsonStatistic(Triangles, Degrees);
		return c;
	}
	public static int classifyEdges(Graph g) {
		int sameEdge = 0;
		Iterator<Edge> i = g.edges();
		while (i.hasNext()) {
			Edge temp = i.next();
			if (temp.getSourceNode().getString("value").equals(temp.getTargetNode().getString("value")))
				sameEdge++;
		}
		return sameEdge;
	}
	
	public static int BFSheight(Graph g, int v)	{

		g.addColumn(v+"dist", int.class, -1);
		LinkedList<Node> q = new LinkedList<Node>();
		q.add(g.getNode(v));
		q.peekFirst().set(v+"dist", 0);
		int max = 0;
		while(!q.isEmpty())	{
			Node t = q.removeFirst();
			Iterator<Node> nei = t.neighbors();
			while(nei.hasNext())	{
				Node temp = nei.next();
				if(temp.getInt(v+"dist") == -1)	{
					int d = t.getInt(v+"dist") + 1;
					max = Math.max(d, max);
					temp.set(v+"dist", d);
					q.addLast(temp);
				}
			}
		}
		return max;
	}
	
	public static int dia(Graph g)	{
		int max = 0;
		for(int i=0; i<g.getNodeCount(); ++i)
			max = Math.max(max, BFSheight(g,i));
		return max;
	}

	public static void main(String... args) throws DataIOException, IOException	{
		Graph g = new GraphMLReader().readGraph("polbooks.xml");
		g.addColumn("id", int.class);
		Iterator<Node> n = g.nodes();
		int i = 0;
		while(n.hasNext())	{
			n.next().set("id", i++);
		}
		
		FileOutputStream fos = new FileOutputStream("polbooksAnalysis.csv");
		Table tb = nodalAnalysis(g);
		CSVTableWriter tw = new CSVTableWriter();
		tw.writeTable(tb, fos);
		fos.close();
		
		BufferedWriter bw = new BufferedWriter(new FileWriter("booksGraphAnalysis.csv"));
				
		tuple t = countTrianglesAndNetworkClusteringCoefficient(g);
		bw.write("\"File Name\",\"Global Clustering Coefficient\",\"Average Network Clustering Coefficient\",\"Edge Ratio\",\"Pearson\'s Correlation Coefficient\"");
		bw.newLine();
		bw.write("polbooks.xml,"+(((double)t.Triangles)/nC3(g.getNodeCount()))+","+t.Clustering+","+((double)classifyEdges(g)/g.getEdgeCount())+","+t.Pearson);
		bw.newLine();
		for (int j=1; j<=50; j++)
		{
			String filename = "polbooks_rand_"+j+".xml";
			g = new GraphMLReader().readGraph("polbooks_rand_\\"+filename);
			g.addColumn("id", int.class);
			n = g.nodes();
			i = 0;
			while(n.hasNext())	{
				n.next().set("id", i++);
			}
			t = countTrianglesAndNetworkClusteringCoefficient(g);
			bw.write((filename+","+((double)t.Triangles)/nC3(g.getNodeCount()))+","+t.Clustering+","+((double)classifyEdges(g)/g.getEdgeCount())+","+t.Pearson);
			bw.newLine();
		}
		bw.close();
		
	}
	static long nC3(int n)
	{
		return (n*(n-1)*(n-2)/6);
	}
}
class tuple
{
	int Triangles;
	double Clustering;
	double Pearson;
	tuple(int a, double b)
	{
		Triangles = a;
		Clustering = b;
	}
}
