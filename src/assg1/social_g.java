package assg1;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.util.Iterator;
import java.util.Random;
import java.util.Scanner;

import prefuse.data.Edge;
import prefuse.data.Graph;
import prefuse.data.Node;

public class social_g {

	public static Graph genSocial(int size) {
		Graph g = new Graph(true);
		g.addColumn("degree", int.class);
		g.addColumn("value", String.class);
		g.addColumn("id", int.class);
		Random random = new Random();
		int degreeSum = 0;

		for (int i = 0; i < size; i++) {
			Node newNode = g.addNode();
			int newDegree = 0;
			while ((newDegree == 0) && (i != 0))
				// we want our graph to be connected
				for (int j = 0; j < g.getNodeCount() - 1; j++)

					if ((degreeSum == 0)
							|| (random.nextInt(degreeSum) < g.getNode(j)
									.getInt("degree"))) {

						g.getNode(j).set("degree",
								g.getNode(j).getInt("degree") + 1);
						newDegree++;
						degreeSum += 2;

						if (random.nextInt(2) == 0) {
							g.addEdge(g.getNode(j), newNode);
						} else {
							g.addEdge(newNode, g.getNode(j));
						}

					}

			g.getNode(i).set("degree", newDegree);
			g.getNode(i).set("id", i);
			g.getNode(i).set("value", new String(i+""));
		}

		return g;

	}

	public static void main(String[] args) throws IOException {
		// intialize
		Graph g;
		
		String file_name = "polbooks.xml";
		int no_of_file = 50;
		if (args.length > 1) {
			file_name = args[0];
			no_of_file = Integer.parseInt(args[1]);
		}

		String fb = file_name.substring(0, file_name.indexOf('.')) + "_rand_soc";
		String ex = file_name.substring(file_name.indexOf('.'));

		// variables
		File f = new File(file_name);
		Scanner s = new Scanner(f);
		String file_str = "";

		// read input file
		while (s.hasNextLine()) {
			file_str += s.nextLine() + " \n";
		}
		s.close();

		// Token File
		long tim = System.currentTimeMillis();
		String tokens[] = file_str.split(" ");
		tim = (System.currentTimeMillis() - tim);
		System.out.println("Time taken to tokenize the File: " + file_name
				+ " " + tim + " millisec\n\n");

		// Write File
		new File(fb).mkdir();

		int no_of_nodes = 0;
		for (int j = 0; j < tokens.length; j++)
			if (tokens[j].contains("<node"))
				no_of_nodes++;

		for (int k = 1; k <= no_of_file; k++) {
			System.out.print("File " + k + ":  Writing");
			File file = new File(fb + "\\" + fb + k + ex);
		
			Writer output = new BufferedWriter(new FileWriter(file));

			for (int i = 0; i < tokens.length; i++) {
				if (tokens[i].contains("<edge"))
					break;
				output.write(tokens[i] + " ");
			}

			g = genSocial(no_of_nodes);
			@SuppressWarnings("unchecked")
			Iterator<Edge> edItr = g.edges();
			
			while(edItr.hasNext())	{
				Edge cur = edItr.next();
				int src = cur.getSourceNode().getInt("id");
				int trg = cur.getTargetNode().getInt("id");
				output.write("<edge source=\""+src+"\" target=\""+trg+"\" />\n");
			}
			
			output.write("</graph>\n");
			output.write("</graphml>\n");
			output.close();
			System.out.println("......Complete");
		}
	}

}
