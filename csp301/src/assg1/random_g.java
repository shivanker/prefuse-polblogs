package assg1;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.util.HashSet;
import java.util.Random;
import java.util.Scanner;

public class random_g {

	public static void main(String[] args) throws IOException {
		// intialise
		String file_name = "polbooks.xml";
		int no_of_file = 50;
		if(args.length>1){
			file_name = args[0];
			no_of_file = Integer.parseInt(args[1]);
		}
		
		String fb = file_name.substring(0,file_name.indexOf('.')) + "_rand_";
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
		/* System.out.println(file_str); */
		

		// Token File
		long tim = System.currentTimeMillis();
		String tokens[] = file_str.split(" ");
		tim = (System.currentTimeMillis() - tim);
		System.out.println("Time taken to tokenize the File: " + file_name + " "+ tim + " millisec\n\n");
		

		// Write File
		new File(fb).mkdir();
		Random rgen = new Random();
		
		for (int k = 1; k <= no_of_file; k++) {
			System.out.print("File "+k+":  Writing");
			File file = new File(fb+"\\"+fb+k+ex);
			Random rand = new Random(System.nanoTime()*rgen.nextLong());
			
			int no_of_nodes = 0;
			for (int j = 0; j < tokens.length; j++) {
				if (tokens[j].contains("<node"))
					no_of_nodes++;
			}
			Writer output = new BufferedWriter(new FileWriter(file));
			int src = 0;
			int trg = 0;
			HashSet s1 = new HashSet();
			HashSet h = new HashSet();
			for (int i = 0; i < tokens.length; i++) {
				if (tokens[i].contains("source=\"")) {
					src = rand.nextInt(no_of_nodes);
					output.write("source=\"" + src + "\" ");
					s1.add(src);
				} else if (tokens[i].contains("target=\"")) {
					while (true) {
						trg = rand.nextInt(no_of_nodes);
						if (src == trg)
							continue;
						else {
							s1.add(trg);
							if (h.contains(s1)) {
								continue;
							} else {
								h.add(s1);
								s1 = new HashSet();
								break;
							}
						}
					}
					output.write("target=\"" + trg + "\"/>");
				} else {
					output.write(tokens[i] + " ");
				}

			}
			output.close();
			System.out.println("......Complete");
			/*
			 * { System.out.println("File : 1"); System.out.println(
			 * "---------------------------------------------------------");
			 * File f2 = new File("try1.xml"); Scanner s2 = new Scanner(f2);
			 * while(s2.hasNextLine()){ System.out.println(s2.nextLine()); }
			 * System.out
			 * .println("---------------------------------------------------------"
			 * ); s2.close(); }
			 */
		}

	}

}
