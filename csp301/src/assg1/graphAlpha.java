package assg1;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Shape;
import java.awt.event.MouseEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Rectangle2D;

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
import prefuse.action.assignment.ColorAction;
import prefuse.action.assignment.DataColorAction;
import prefuse.action.filter.GraphDistanceFilter;
import prefuse.action.layout.graph.ForceDirectedLayout;
import prefuse.activity.Activity;
import prefuse.controls.ControlAdapter;
import prefuse.controls.DragControl;
import prefuse.controls.FocusControl;
import prefuse.controls.NeighborHighlightControl;
import prefuse.controls.PanControl;
import prefuse.controls.WheelZoomControl;
import prefuse.controls.ZoomControl;
import prefuse.controls.ZoomToFitControl;
import prefuse.data.Graph;
import prefuse.data.Tuple;
import prefuse.data.event.TupleSetListener;
import prefuse.data.io.GraphMLReader;
import prefuse.data.search.PrefixSearchTupleSet;
import prefuse.data.search.SearchTupleSet;
import prefuse.data.tuple.TupleSet;
import prefuse.render.AbstractShapeRenderer;
import prefuse.render.DefaultRendererFactory;
import prefuse.util.ColorLib;
import prefuse.util.FontLib;
import prefuse.util.GraphicsLib;
import prefuse.util.display.DisplayLib;
import prefuse.util.display.ItemBoundsListener;
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

public class graphAlpha extends JPanel {

	private static final String graph = "graph";
	private static final String nodes = "graph.nodes";
	private static final String edges = "graph.edges";

	private Visualization m_vis;

	public graphAlpha(Graph g, String label) {
		super(new BorderLayout());

		// create a new, empty visualization for our data
		m_vis = new Visualization();

		// --------------------------------------------------------------------
		// set up the renderers

		// ShapeRenderer sr = new ShapeRenderer();
		nodeRenderer nr = new nodeRenderer();
		// LabelRenderer tr = new LabelRenderer();
		// tr.setRoundedCorner(20, 20);
		m_vis.setRendererFactory(new DefaultRendererFactory(nr));

		// --------------------------------------------------------------------
		// register the data with a visualization
		// adds graph to visualization and sets renderer label field
		setGraph(g, label);

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

		int[] palette = new int[] { ColorLib.rgb(255, 180, 180),
				ColorLib.rgb(190, 190, 255), ColorLib.rgba(255, 255, 0, 150) };
		// map nominal data values to colors using our provided palette
		DataColorAction fill = new DataColorAction(nodes, "value",
				Constants.NOMINAL, VisualItem.FILLCOLOR, palette);

		fill.add(VisualItem.FIXED, ColorLib.rgba(255, 0, 0, 200));
		fill.add(VisualItem.HIGHLIGHT, ColorLib.rgba(0, 0, 255, 200));
		fill.add("ingroup('_search_')", ColorLib.rgba(0, 0, 0, 200));

		// use black for node text
		ColorAction text = new ColorAction("graph.nodes", VisualItem.TEXTCOLOR,
				ColorLib.gray(0));
		text.add("ingroup('_search_')", ColorLib.rgb(255, 255, 255));
		// use light grey for edges
		ColorAction edge = new ColorAction("graph.edges",
				VisualItem.STROKECOLOR, ColorLib.gray(200));

		// create an action list containing all color assignments
		ActionList color = new ActionList();
		color.add(fill);
		color.add(text);
		color.add(edge);

		SearchTupleSet searchset = new PrefixSearchTupleSet();
		m_vis.addFocusGroup(Visualization.SEARCH_ITEMS, searchset);

		ActionList draw = new ActionList();
		draw.add(filter);
		draw.add(fill);
		draw.add(text);
		draw.add(new ColorAction(nodes, VisualItem.STROKECOLOR, 0));
		draw.add(new ColorAction(nodes, VisualItem.TEXTCOLOR, ColorLib.rgb(0,
				0, 0)));
		draw.add(new ColorAction(edges, VisualItem.FILLCOLOR, ColorLib
				.gray(200)));
		draw.add(new ColorAction(edges, VisualItem.STROKECOLOR, ColorLib
				.gray(200)));

		ForceSimulator fsim = new ForceSimulator();
		fsim.addForce(new NBodyForce(-2.6f, -1.0f, 0.9f));
		fsim.addForce(new SpringForce());
		fsim.addForce(new DragForce(0.015f));

		ActionList animate = new ActionList(Activity.INFINITY);
		animate.add(new ForceDirectedLayout(graph, fsim, false));
		animate.add(fill);
		animate.add(text);
		animate.add(new RepaintAction());

		// finally, we register our ActionList with the Visualization.
		// we can later execute our Actions by invoking a method on our
		// Visualization, using the name we've chosen below.
		m_vis.putAction("draw", draw);
		m_vis.putAction("layout", animate);
		m_vis.runAfter("draw", "layout");
		m_vis.putAction("color", color);

		// --------------------------------------------------------------------
		// set up a display to show the visualization

		Display display = new Display(m_vis);
		display.setSize(700, 700);
		display.pan(350, 350);
		display.setForeground(Color.GRAY);
		display.setBackground(Color.WHITE);

		// main display controls
		display.addControlListener(new FocusControl(1));
		display.addControlListener(new DragControl());
		display.addControlListener(new PanControl());
		display.addControlListener(new ZoomControl());
		display.addControlListener(new WheelZoomControl());
		display.addControlListener(new ZoomToFitControl());
		display.addControlListener(new NeighborHighlightControl());

		// overview display
		Display overview = new Display(m_vis);
		overview.setSize(290, 290);
		overview.addItemBoundsListener(new FitOverviewListener());

		display.setForeground(Color.GRAY);
		display.setBackground(Color.WHITE);

		// --------------------------------------------------------------------
		// launch the visualization

		// create a panel for editing force values
		JForcePanel fpanel = new JForcePanel(fsim);

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

		/*
		 * SearchTupleSet s = new PrefixSearchTupleSet();
		 * m_vis.addFocusGroup(Visualization.SEARCH_ITEMS, s);
		 * s.addTupleSetListener(new TupleSetListener() { public void
		 * tupleSetChanged(TupleSet t, Tuple[] add, Tuple[] rem) {
		 * m_vis.cancel("animatePaint"); m_vis.run("recolor");
		 * m_vis.run("animatePaint"); } });
		 */

		final JFastLabel title = new JFastLabel(" ");
		title.setPreferredSize(new Dimension(300, 30));
		title.setMaximumSize(new Dimension(300, 30));
		title.setVerticalAlignment(SwingConstants.TOP);
		title.setBorder(BorderFactory.createEmptyBorder(3, 0, 0, 0));
		title.setFont(FontLib.getFont("Calibri", Font.PLAIN, 16));
		title.setBackground(Color.WHITE);

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
		

		display.addControlListener(new ControlAdapter() {
			public void itemEntered(VisualItem item, MouseEvent e) {
				if (item instanceof NodeItem) {
					String label = item.getString("label");
					String aff = ""+item.get("value");
					String source = ""+item.get("source");
					if (aff.equals("0"))
						aff = "Conservative";
					else if (aff.equals("1"))
						aff = "Neutral";
					else
						aff = "Liberal";
					title.setText(label);
					value.setText("Affiliation: " + aff );
					value2.setText("Source: "+ source);
				} else
					title.setText(":O");
			}

			public void itemExited(VisualItem item, MouseEvent e) {
				title.setText(null);
				value.setText(null);
				value2.setText(null);
				
			}
		});

		Box box = UILib.getBox(new Component[] { title, value, value2, search }, false,
				10, 3, 0);
		box.setBorder(BorderFactory.createTitledBorder("Node Info"));
		box.setMaximumSize(new Dimension(310, 90));

		fpanel.add(box);
		fpanel.add(Box.createVerticalGlue());
		fpanel.add(overview);

		// create a new JSplitPane to present the interface
		JSplitPane split = new JSplitPane();
		split.setLeftComponent(display);
		split.setRightComponent(fpanel);
		split.setOneTouchExpandable(true);
		split.setContinuousLayout(false);
		split.setDividerLocation(700);

		// now we run our action list
		m_vis.run("draw");

		add(split);
	}

	public void setGraph(Graph g, String label) {
		// update labeling
		DefaultRendererFactory drf = (DefaultRendererFactory) m_vis
				.getRendererFactory();
		// ((LabelRenderer) drf.getDefaultRenderer()).setTextField("value");
		// ((LabelRenderer) drf.getDefaultRenderer()).setHorizontalPadding(5);
		// ((LabelRenderer) drf.getDefaultRenderer())
		// .setHorizontalAlignment(Constants.CENTER);
		// ((LabelRenderer) drf.getDefaultRenderer())
		// .setVerticalAlignment(Constants.CENTER);

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

		// create graphAlpha
		String datafile = "polblogs.xml";
		String label = "label";
		if (args.length > 1) {
			datafile = args[0];
			label = args[1];
		}

		JFrame frame = demo(datafile, label);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
	}

	public static JFrame demo() {
		return demo((String) null, "label");
	}

	public static JFrame demo(String datafile, String label) {
		Graph g = null;
		try {
			g = new GraphMLReader().readGraph(datafile);
		} catch (Exception e) {
			e.printStackTrace();
			System.exit(1);
		}

		return demo(g, label);
	}

	public static JFrame demo(Graph g, String label) {
		final graphAlpha view = new graphAlpha(g, label);

		// launch window
		JFrame frame = new JFrame(
				"c s p 3 0 1  |  a s s i g n m e n t 1 | p o l b l o g s");
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

	public static class FitOverviewListener implements ItemBoundsListener {
		private Rectangle2D m_bounds = new Rectangle2D.Double();
		private Rectangle2D m_temp = new Rectangle2D.Double();
		private double m_d = 15;

		public void itemBoundsChanged(Display d) {
			d.getItemBounds(m_temp);
			GraphicsLib.expand(m_temp, 25 / d.getScale());

			double dd = m_d / d.getScale();
			double xd = Math.abs(m_temp.getMinX() - m_bounds.getMinX());
			double yd = Math.abs(m_temp.getMinY() - m_bounds.getMinY());
			double wd = Math.abs(m_temp.getWidth() - m_bounds.getWidth());
			double hd = Math.abs(m_temp.getHeight() - m_bounds.getHeight());
			if (xd > dd || yd > dd || wd > dd || hd > dd) {
				m_bounds.setFrame(m_temp);
				DisplayLib.fitViewToBounds(d, m_bounds, 0);
			}
		}
	}

	class nodeRenderer extends AbstractShapeRenderer {
		// protected RectangularShape m_box = new Rectangle2D.Double();
		protected Ellipse2D m_box = new Ellipse2D.Double();

		@Override
		protected Shape getRawShape(VisualItem item) {
			m_box.setFrame(item.getX(), item.getY(), 10, 10);
			return m_box;
		}
	}
} // end of class graphAlpha
