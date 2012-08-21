package assg1;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;

import prefuse.data.Edge;
import prefuse.data.Graph;
import prefuse.data.Node;
import prefuse.data.Table;
import prefuse.data.io.DataIOException;

public class readGML {

	public static Graph loadGraph(File f) throws DataIOException, IOException {
		// Generates a Graph for The gml file(File Specific)
		Table nodeData = new Table();
		Table edgeData = new Table();
		nodeData.addColumn("id", String.class);
		nodeData.addColumn("label", String.class);
		nodeData.addColumn("value", String.class);
		edgeData.addColumn(Graph.DEFAULT_SOURCE_KEY, int.class);
		edgeData.addColumn(Graph.DEFAULT_TARGET_KEY, int.class);
		edgeData.addColumn("type", String.class);
		BufferedReader br = null;
		Graph g = null;
		String s;
		boolean directed = false;

		try {
			br = new BufferedReader(new FileReader(f));
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		while ((s = br.readLine()) != null) {
			s = s.trim();
			String[] tokens = s.split("\\s+");
			// Finding Directed or non directed graph
			if (tokens[0].equals("directed")) {
				directed = tokens[1].equals("1");
				// System.out.println(directed);
				g = new Graph(nodeData, edgeData, directed);
			}
			// Adding node data
			else if (tokens[0].equals("node")) {
				// System.out.println("inside node");
				Node n1 = g.addNode();
				String s1;
				while (!(s1 = br.readLine().trim()).equals("]")) {
					if (s1.equals("[")) {
						continue;
					}
					// System.out.println(s1);
					String[] tokens1 = s1.split("\\s+", 2);
					// System.out.println(tokens1[0]);
					System.out.println(tokens1[1]);

					n1.set(tokens1[0], tokens1[1]);
				}
				// System.out.println(n1);

			}
			// Adding edge data

			else if (tokens[0].equals("edge")) {
				// System.out.println("inside edge");
				String s2;
				int source = -1;
				int target = -1;
				while (!(s2 = br.readLine().trim()).equals("]")) {
					if (s2.equals("[")) {
						continue;
					}
					// System.out.println(s1);
					String[] tokens2 = s2.split("\\s+", 2);
					source = tokens2[0].equals("source") ? Integer
							.parseInt(tokens2[1]) : source;
					target = tokens2[0].equals("target") ? Integer
							.parseInt(tokens2[1]) : target;
				}
				Edge e1 = g.getEdge(g.addEdge(source, target));
				// Adding Type of edge data
				if (g.getNode(source).get("value")
						.equals(g.getNode(target).get("value"))) {
					e1.set("type", "same");
				} else {
					e1.set("type", "cross");
				}
			}

		}

		return g;

	}
}
