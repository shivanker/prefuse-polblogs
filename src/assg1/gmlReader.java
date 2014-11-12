package assg1;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

import prefuse.data.Edge;
import prefuse.data.Graph;
import prefuse.data.Node;
import prefuse.data.Table;
import prefuse.data.io.AbstractGraphReader;
import prefuse.data.io.DataIOException;
import prefuse.data.io.GraphReader;
import prefuse.util.io.IOLib;

public class gmlReader extends AbstractGraphReader implements GraphReader {

	public Graph readGraph(InputStream is) throws DataIOException {

		int size = 1024, len;
		byte[] buf;
		ByteArrayOutputStream bos = new ByteArrayOutputStream();
		buf = new byte[size];
		try {
			while ((len = is.read(buf, 0, size)) != -1)
				bos.write(buf, 0, len);
			is.close();
			bos.close();
		} catch (IOException e) {
			e.printStackTrace();
			throw new DataIOException(e);
		}
		buf = bos.toByteArray();
		String siii = new String(buf).trim();
		String tokens[] = siii
				.split("((?<!\"[^\\n]{0,1000})\\s+|\\n+\\s+|\\s+(?![^\\n]*\"))");

		Table nodeData = new Table();
		Table edgeData = new Table();
		nodeData.addColumn("id", String.class);
		edgeData.addColumn(Graph.DEFAULT_SOURCE_KEY, int.class);
		edgeData.addColumn(Graph.DEFAULT_TARGET_KEY, int.class);

		int i = 0;
		while (!(tokens[i++].equals("graph") && tokens[i].equals("[")))
			;

		// Addition of required columns
		// To the node table
		int j = i;
		while (!(tokens[j++].equals("node") && tokens[j].equals("[")))
			;
		j++;
		while (!tokens[j].equals("]")) {
			if (!tokens[j].equals("id"))
				if (tokens[j + 1].charAt(0) == '\"')
					nodeData.addColumn(tokens[j], String.class);
				else
					try {
						Integer.parseInt(tokens[j + 1]);
						nodeData.addColumn(tokens[j], int.class);
					} catch (Exception e) {
						try {
							Long.parseLong(tokens[j + 1]);
							nodeData.addColumn(tokens[j], long.class);
						} catch (Exception e1) {
							nodeData.addColumn(tokens[j], double.class);
						}
					}
			j += 2;
		}

		// To the edge table
		j = i;
		while (!(tokens[j++].equals("edge") && tokens[j].equals("[")))
			;
		j++;
		while (!tokens[j].equals("]")) {
			if (!(tokens[j].equals("source") || tokens[j].equals("target"))) {
				if (tokens[j + 1].charAt(0) == '\"')
					edgeData.addColumn(tokens[j], String.class);
				else
					try {
						Integer.parseInt(tokens[j + 1]);
						edgeData.addColumn(tokens[j], int.class);
					} catch (Exception e) {
						try {
							Long.parseLong(tokens[j + 1]);
							edgeData.addColumn(tokens[j], long.class);
						} catch (Exception e2) {
							edgeData.addColumn(tokens[j], double.class);
						}
					}
			}
			j += 2;
		}

		// Determining whether the graph is directed or undirected
		j = i;
		while (!(tokens[j++].equals("directed") && (tokens[j].equals("0") || tokens[j]
				.equals("1"))))
			;
		boolean directed = tokens[j].equals("1");
		Graph g = new Graph(nodeData, edgeData, directed);

		while (i + 1 < tokens.length) {
			while (i + 1 < tokens.length
					&& !((tokens[i].equals("node") || tokens[i].equals("edge")) && tokens[i + 1]
							.equals("[")))
				i++;

			// Adding Node Data
			if (tokens[i].equals("node")) {
				Node n1 = g.addNode();
				i += 2;
				while (!tokens[i].equals("]")) {
					if (tokens[i + 1].charAt(0) == '\"')
						tokens[i + 1] = tokens[i + 1].substring(1,
								tokens[i + 1].length() - 1);
					n1.set(tokens[i], tokens[i + 1]);
					i += 2;
				}
			}

			// Adding edge data
			else if (tokens[i].equals("edge")) {
				i += 2;
				j = i;
				int source = -1, target = -1;
				while (!tokens[j].equals("]")) {
					if (tokens[j].equals("source"))
						source = Integer.parseInt(tokens[j + 1]);
					else if (tokens[j].equals("target"))
						target = Integer.parseInt(tokens[j + 1]);
					j += 2;
				}
				if (source == 1490 && target == 802)
					System.out.println("Lets start");
				Edge e1 = g.getEdge(g.addEdge(source, target));

				while (!tokens[i].equals("]")) {
					if (!tokens[i].equals("source")
							&& !tokens[i].equals("target")) {
						if (tokens[i + 1].charAt(0) == '\"')
							tokens[i + 1] = tokens[i + 1].substring(1,
									tokens[i + 1].length() - 1);
						e1.set(tokens[i], tokens[i + 1]);
					}
					i += 2;
				}
			}

		}
		return g;
	}

	public static void main(String... args) throws DataIOException {
		String location = "polblogs.gml";
		@SuppressWarnings("unused")
		Graph g;
		try {
			InputStream is = IOLib.streamFromString(location);
			if (is == null)
				throw new DataIOException("Couldn't find " + location
						+ ". Not a valid file, URL, or resource locator.");
			g = new gmlReader().readGraph(is);
		} catch (IOException e) {
			throw new DataIOException(e);
		}

	}
}
