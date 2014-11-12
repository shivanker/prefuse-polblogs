package assg1;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GraphicsEnvironment;
import java.awt.Shape;
import java.awt.event.MouseEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.geom.Point2D;
import java.util.Iterator;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JSplitPane;
import javax.swing.SwingConstants;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import prefuse.Constants;
import prefuse.Display;
import prefuse.Visualization;
import prefuse.action.ActionList;
import prefuse.action.RepaintAction;
import prefuse.action.animate.ColorAnimator;
import prefuse.action.assignment.ColorAction;
import prefuse.action.assignment.DataColorAction;
import prefuse.action.filter.GraphDistanceFilter;
import prefuse.action.layout.graph.ForceDirectedLayout;
import prefuse.activity.Activity;
import prefuse.controls.Control;
import prefuse.controls.ControlAdapter;
import prefuse.controls.DragControl;
import prefuse.controls.FocusControl;
import prefuse.controls.NeighborHighlightControl;
import prefuse.controls.PanControl;
import prefuse.controls.ToolTipControl;
import prefuse.controls.WheelZoomControl;
import prefuse.controls.ZoomControl;
import prefuse.controls.ZoomToFitControl;
import prefuse.data.Graph;
import prefuse.data.Node;
import prefuse.data.Tuple;
import prefuse.data.event.TupleSetListener;
import prefuse.data.io.GraphMLReader;
import prefuse.data.search.PrefixSearchTupleSet;
import prefuse.data.search.SearchTupleSet;
import prefuse.data.tuple.TupleSet;
import prefuse.render.DefaultRendererFactory;
import prefuse.render.EdgeRenderer;
import prefuse.render.ShapeRenderer;
import prefuse.util.ColorLib;
import prefuse.util.FontLib;
import prefuse.util.force.DragForce;
import prefuse.util.force.ForceSimulator;
import prefuse.util.force.NBodyForce;
import prefuse.util.force.SpringForce;
import prefuse.util.ui.JFastLabel;
import prefuse.util.ui.JForcePanel;
import prefuse.util.ui.JSearchPanel;
import prefuse.util.ui.JValueSlider;
import prefuse.util.ui.UILib;
import prefuse.visual.NodeItem;
import prefuse.visual.VisualGraph;
import prefuse.visual.VisualItem;

public class graphGamma extends JPanel {

	private static final long serialVersionUID = 1L;
	private static final String graph = "graph";
	private static final String nodes = "graph.nodes";
	private static final String edges = "graph.edges";

	private Visualization m_vis;

	public graphGamma(Graph g) {
		super(new BorderLayout());

		// create a new, empty visualization for our data
		m_vis = new Visualization();

		// --------------------------------------------------------------------
		// set up the renderers

		EdgeRenderer er = new EdgeRenderer(Constants.EDGE_TYPE_LINE,
				Constants.EDGE_ARROW_FORWARD);
		nodeRenderer nr = new nodeRenderer();
		m_vis.setRendererFactory(new DefaultRendererFactory(nr, er));

		er.setArrowHeadSize(7, 10);
		er.setDefaultLineWidth(0.7);

		// --------------------------------------------------------------------
		// register the data with a visualization
		// adds graph to visualization and sets renderer label field
		setGraph(g);

		// fix selected focus nodes
		TupleSet focusGroup = m_vis.getGroup(Visualization.FOCUS_ITEMS);

		focusGroup.addTupleSetListener(new TupleSetListener() {
			public void tupleSetChanged(TupleSet ts, Tuple[] add, Tuple[] rem) {
				for (int i = 0; i < rem.length; ++i)
					((VisualItem) rem[i]).setFixed(false);
				for (int i = 0; i < add.length; ++i) {
					((VisualItem) add[i]).setFixed(false);
					((VisualItem) add[i]).setFixed(true);
				}
				if (ts.getTupleCount() == 0) {
					ts.addTuple(rem[0]);
					((VisualItem) rem[0]).setFixed(false);
				}
				m_vis.run("draw");
			}
		});

		// --------------------------------------------------------------------
		// create actions to process the visual data
		int hops = 30;
		final GraphDistanceFilter filter = new GraphDistanceFilter(graph, hops);

		// color set for default node
		int[] palette = new int[] { ColorLib.rgba(200, 200, 255, 250),
				ColorLib.rgba(200, 255, 200, 250),
				ColorLib.rgba(255, 200, 125, 250),
				ColorLib.rgba(255, 200, 200, 250) };
		// color set for highlighted node
		int[] palette2 = new int[] { ColorLib.rgba(0, 0, 128, 200),
				ColorLib.rgba(0, 128, 0, 200),
				ColorLib.rgba(225, 150, 100, 200),
				ColorLib.rgba(255, 20, 147, 200) };

		// map nominal data values to colors using our provided palette
		DataColorAction fill = new DataColorAction(nodes, "size",
				Constants.NOMINAL, VisualItem.FILLCOLOR, palette);
		DataColorAction fill2 = new DataColorAction(nodes, "size",
				Constants.NOMINAL, VisualItem.FILLCOLOR, palette2);
		fill.add(VisualItem.FIXED, ColorLib.rgba(0, 0, 0, 200));
		fill.add(VisualItem.HIGHLIGHT, fill2);
		fill.add("ingroup('_search_')", ColorLib.rgba(0, 0, 0, 200));
		
		// use white for node text
		ColorAction text = new ColorAction(nodes, VisualItem.TEXTCOLOR,
				ColorLib.gray(0));
		text.add("ingroup('_search_')", ColorLib.rgb(255, 255, 255));

		// outlines of nodes
		ColorAction nStroke = new ColorAction(nodes, VisualItem.STROKECOLOR);
		nStroke.setDefaultColor(ColorLib.gray(50));
		nStroke.add("_hover", ColorLib.gray(10));
		nStroke.add(VisualItem.HIGHLIGHT, ColorLib.gray(10));
		nStroke.add("ingroup('_search')", ColorLib.gray(10));

		// use light grey for edges
		ColorAction edge = new ColorAction(edges, VisualItem.STROKECOLOR,
				ColorLib.gray(200));
		// use dark light grey for edges
		ColorAction edge1 = new ColorAction(edges, VisualItem.FILLCOLOR,
				ColorLib.gray(150));

		ColorAction edge2 = new ColorAction(edges, VisualItem.STROKECOLOR,
				ColorLib.gray(50));
		ColorAction edge3 = new ColorAction(edges, VisualItem.FILLCOLOR,
				ColorLib.gray(50));
		edge.add(VisualItem.HIGHLIGHT, edge2);
		edge1.add(VisualItem.HIGHLIGHT, edge3);

		// animate paint change
		ActionList animatePaint = new ActionList(1000);
		animatePaint.add(new ColorAnimator(nodes));
		animatePaint.add(new RepaintAction());
		m_vis.putAction("animatePaint", animatePaint);

		// search
		SearchTupleSet s = new PrefixSearchTupleSet();
		m_vis.addFocusGroup(Visualization.SEARCH_ITEMS, s);
		s.addTupleSetListener(new TupleSetListener() {
			public void tupleSetChanged(TupleSet t, Tuple[] add, Tuple[] rem) {
				m_vis.cancel("animatePaint");
				m_vis.run("fill");
				m_vis.run("animatePaint");
			}
		});

		ActionList draw = new ActionList();
		draw.add(filter);
		draw.add(text);
		draw.add(new RepaintAction());

		ForceSimulator fsim = new ForceSimulator();
		fsim.addForce(new NBodyForce(-10.0f, -1.0f, 0.9f));
		fsim.addForce(new SpringForce(0.00001f,180));
		fsim.addForce(new DragForce(0.015f));

		// create an action list containing all color assignments
		ActionList color = new ActionList(Activity.INFINITY);
		color.add(fill);
		color.add(text);
		color.add(edge);
		color.add(edge1);
		color.add(nStroke);
		color.add(new RepaintAction());

		ActionList animate = new ActionList(Activity.INFINITY);
		animate.add(new ForceDirectedLayout(graph, fsim, false));
		animate.add(new RepaintAction());

		// finally, we register our ActionList with the Visualization.
		// we can later execute our Actions by invoking a method on our
		// Visualization, using the name we've chosen below.
		m_vis.putAction("draw", draw);
		m_vis.putAction("layout", animate);
		m_vis.runAfter("draw", "layout");
		m_vis.putAction("layout", color);

		// --------------------------------------------------------------------
		// set up a display to show the visualization
		Display display = new Display(m_vis);
		display.setSize(1000, 700);
		display.pan(500, 350);
		display.zoom(new Point2D.Float(500, 350), 0.55);
		display.setForeground(Color.GRAY);
		display.setBackground(Color.white);

		// main display controls
		display.addControlListener(new FocusControl(1));
		display.addControlListener(new DragControl());
		display.addControlListener(new PanControl());
		display.addControlListener(new ZoomControl());
		display.addControlListener(new WheelZoomControl());
		display.addControlListener(new ZoomToFitControl());
		display.addControlListener(new NeighborHighlightControl());
		display.addControlListener(new modToolTip());
		display.addControlListener(new sccOpener());

		// --------------------------------------------------------------------
		// launch the visualization

		// create a panel for editing force values Sliders
		JForcePanel fpanel = new JForcePanel(fsim);

		// Hops Slider
		final JValueSlider slider = new JValueSlider("Distance", 0, hops, hops);
		slider.addChangeListener(new ChangeListener() {
			public void stateChanged(ChangeEvent e) {
				filter.setDistance(slider.getValue().intValue());
				m_vis.run("draw");
			}
		});
		slider.setBackground(Color.WHITE);
		slider.setPreferredSize(new Dimension(300, 30));
		slider.setMaximumSize(new Dimension(300, 30));

		// Connectivity filter BOX
		Box cf = new Box(BoxLayout.Y_AXIS);
		cf.add(slider);
		cf.setBorder(BorderFactory.createTitledBorder("Connectivity Filter"));
		fpanel.add(cf);

		JSearchPanel search = new JSearchPanel(m_vis, nodes, "label", true,
				true);
		search.setShowResultCount(true);
		search.setBorder(BorderFactory.createEmptyBorder(5, 5, 4, 0));
		search.setFont(FontLib.getFont("Tahoma", Font.PLAIN, 11));
		search.setPreferredSize(new Dimension(300, 30));
		search.setMaximumSize(new Dimension(300, 30));

		// title label
		final JFastLabel title = new JFastLabel(" ");
		title.setPreferredSize(new Dimension(300, 30));
		title.setMaximumSize(new Dimension(300, 30));
		title.setVerticalAlignment(SwingConstants.TOP);
		title.setBorder(BorderFactory.createEmptyBorder(3, 0, 0, 0));
		title.setFont(FontLib.getFont("Calibri", Font.PLAIN, 16));
		title.setBackground(Color.WHITE);

		// value label
		final JFastLabel value = new JFastLabel(" ");
		value.setPreferredSize(new Dimension(300, 30));
		value.setMaximumSize(new Dimension(300, 30));
		value.setVerticalAlignment(SwingConstants.TOP);
		value.setBorder(BorderFactory.createEmptyBorder(3, 0, 0, 0));
		value.setFont(FontLib.getFont("Calibri", Font.PLAIN, 16));
		value.setBackground(Color.WHITE);

		final JFastLabel value2 = new JFastLabel(" ");
		value2.setPreferredSize(new Dimension(300, 30));
		value2.setMaximumSize(new Dimension(300, 30));
		value2.setVerticalAlignment(SwingConstants.TOP);
		value2.setBorder(BorderFactory.createEmptyBorder(3, 0, 0, 0));
		value2.setFont(FontLib.getFont("Calibri", Font.PLAIN, 16));
		value2.setBackground(Color.WHITE);

		// printing labels
		display.addControlListener(new ControlAdapter() {
			public void itemEntered(VisualItem item, MouseEvent e) {
				if (item instanceof NodeItem) {
					int size = item.getInt("size"), l = item.getInt("0"), c = item
							.getInt("1");
					title.setText("Size of SCC: " + size);
					value.setText("No. of liberals: " + l);
					value2.setText("No. of conservatives: " + c);
				} else
					title.setText(":O");
			}

			public void itemExited(VisualItem item, MouseEvent e) {
				title.setText(null);
				value.setText(null);
				value2.setText(null);
			}
		});

		Box box = UILib.getBox(
				new Component[] { title, value, value2, search }, false, 10, 3,
				0);
		box.setBorder(BorderFactory.createTitledBorder("SCC Info"));
		box.setMaximumSize(new Dimension(310, 90));

		fpanel.add(box);
		fpanel.add(Box.createVerticalGlue());

		// create a new JSplitPane to present the interface
		JSplitPane split = new JSplitPane();
		split.setLeftComponent(display);
		split.setRightComponent(fpanel);
		split.setOneTouchExpandable(true);
		split.setContinuousLayout(false);
		split.setDividerLocation(1000);

		// now we run our action list
		m_vis.run("draw");

		add(split);

	}

	public void setGraph(Graph g) {

		// update graph
		m_vis.removeGroup(graph);
		VisualGraph vg = m_vis.addGraph(graph, g);
		m_vis.setValue(edges, null, VisualItem.INTERACTIVE, Boolean.FALSE);
		VisualItem f = (VisualItem) vg.getNode(0);
		m_vis.getGroup(Visualization.FOCUS_ITEMS).setTuple(f);
		f.setFixed(false);
	}

	// ------------------------------------------------------------------------
	// Main and demo methods

	public static void main(String[] args) {
		UILib.setPlatformLookAndFeel();

		// create graphGamma
		String datafile = "polblogs.xml";
		if (args.length > 0) {
			datafile = args[0];
		}
		GraphicsEnvironment e = GraphicsEnvironment
				.getLocalGraphicsEnvironment();
		JFrame frame = demo(datafile);

		frame.setMaximizedBounds(e.getMaximumWindowBounds());
		frame.setExtendedState(frame.getExtendedState() | JFrame.MAXIMIZED_BOTH);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

	}

	public static JFrame demo(String datafile) {
		Graph g = null;
		try {
			g = new GraphMLReader().readGraph(datafile);
		} catch (Exception e) {
			e.printStackTrace();
			System.exit(1);
		}

		if (!g.getNode(0).canGetInt("id")) {
			g.addColumn("id", int.class);
			@SuppressWarnings("unchecked")
			Iterator<Node> n = g.nodes();
			int i = 0;
			while (n.hasNext()) {
				n.next().set("id", i++);
			}
		}

		return demo(g);
	}

	public static JFrame demo(Graph g) {

		g = AnalysisDirected.setSCC(g);
		if (!g.getNode(0).canGetInt("id")) {
			g.addColumn("id", int.class);
			@SuppressWarnings("unchecked")
			Iterator<Node> n = g.nodes();
			int i = 0;
			while (n.hasNext()) {
				n.next().set("id", i++);
			}
		}

		final graphGamma view = new graphGamma(g);

		// launch window
		JFrame frame = new JFrame(
				"c s p 3 0 1  |  a s s i g n m e n t 1 | p o l b l o g s | Strongly Connected Components");
		frame.setContentPane(view);
		frame.pack();
		frame.setVisible(true);

		frame.addWindowListener(new WindowAdapter() {
			public void windowActivated(WindowEvent e) {
				view.m_vis.run("layout");
			}

			public void windowDeactivated(WindowEvent e) {
				view.m_vis.cancel("layout");
			}
		});

		return frame;
	}

	class nodeRenderer extends ShapeRenderer {

		@Override
		protected Shape getRawShape(VisualItem item) {
			double x = item.getX();
			if (Double.isNaN(x) || Double.isInfinite(x))
				x = 0;
			double y = item.getY();
			if (Double.isNaN(y) || Double.isInfinite(y))
				y = 0;
			double width =  getBaseSize()*item.getSize()*1.75;

			if (item instanceof NodeItem) {
				if(item.getInt("size") == 2)
					width *= item.getInt("size");
				else if(item.getInt("size") == 3)
					width *= item.getInt("size");
				else
					width += item.getInt("size") * 0.35;
			}

			// Center the shape around the specified x and y
			if (width > 1) {
				x = x - width / 2;
				y = y - width / 2;
			}

//			if(item.getInt("size")>100)
//				return rectangle(x, y, width, width);
			return ellipse(x, y, width, width);
		}

	}

	class sccOpener extends ControlAdapter implements Control {

		public void itemClicked(VisualItem item, MouseEvent e) {
			if (item instanceof NodeItem)
				graphAlpha.demo((Graph) item.get("subGraph"), "label");
		}

	}

	class modToolTip extends ToolTipControl {

		public modToolTip() {
			super(new String[0]);
		}

		public modToolTip(String field) {
			super(field);
		}

		@Override
		public void itemEntered(VisualItem item, MouseEvent e) {
			Display d = (Display) e.getSource();
			if (item.canGetString("size"))
				d.setToolTipText("Super-node (SCC) of size: "
						+ item.getString("size"));
		}
	}

}