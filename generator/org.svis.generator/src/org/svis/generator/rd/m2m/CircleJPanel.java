package org.svis.generator.rd.m2m;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.util.List;

import javax.swing.JPanel;

public class CircleJPanel extends JPanel {

	/**
	 * 
	 */
	private static final long serialVersionUID = -5387744622629823215L;
	private List<Circle> circleList;
	public int SHIFT_Y = 500;
	public int SHIFT_X = 300;
	private final int SCALE = 1;

	private double x_min = 0;
	private double x_max = 0;
	private double y_min = 0;
	private double y_max = 0;
	private double r_max = 0;

	public CircleJPanel(List<Circle> circleList) {
		this.circleList = circleList;

		setBackground(Color.white);

		// get the most left circle
		// get the most right circle
		for (Circle circle : circleList) {
			if (circle.getCentre().x - circle.getRadius() < x_min) {
				x_min = circle.getCentre().x - circle.getRadius();
			}

			if (circle.getCentre().y - circle.getRadius() < y_min) {
				y_min = circle.getCentre().y - circle.getRadius();
			}

			if (circle.getCentre().x + circle.getRadius() > x_max) {
				x_max = circle.getCentre().x + circle.getRadius();
			}
			if (circle.getCentre().y + circle.getRadius() > y_max) {
				y_max = circle.getCentre().y + circle.getRadius();
			}

			if (circle.getRadius() > r_max) {
				r_max = circle.getRadius();
			}
		}
		SHIFT_X = (int) (x_max - x_min);
		SHIFT_Y = (int) (y_max - y_min);
		setPreferredSize(new Dimension((int) (x_max - x_min) * SCALE + 10, (int) (y_min - y_max) * SCALE + 10));

	}

	public void paintComponent(Graphics g) {
		super.paintComponent(g);

		// Try drawing some example circles.
		// int i = 0;
		for (Circle circle : circleList) {
			drawCircle(g, ((int) (SCALE * circle.getCentre().x) + (int) (SCALE * Math.abs(x_min))), SHIFT_Y
					- ((int) (SCALE * circle.getCentre().y) + (int) (SCALE * Math.abs(y_min))),
					(int) (SCALE * circle.getRadius())); // center
															// (30,30)
															// r=20
			// g.drawString(Integer.toString(i)+" "+Double.toString(circle.getRadius()),
			// SHIFT_X + (int) (SCALE * circle.getCentre().x)
			// + (int) (SCALE * Math.abs(x_min)), SHIFT_Y - ((int) (SCALE *
			// circle
			// .getCentre().y) + (int) (SCALE * Math.abs(y_min))));
			// i++;
		}

	}

	public void drawCircle(Graphics cg, int xCenter, int yCenter, int r) {

		// int colorComponent = (int) (((float) r / (float) (r_max + 5)) *
		// 255.0f);
		// outer circle
		cg.drawOval(xCenter - r, yCenter - r, 2 * r, 2 * r);
		// cg.setColor(new Color(10, colorComponent, 255 - colorComponent,
		// colorComponent));
		// cg.fillOval(xCenter - r, yCenter - r, 2 * r, 2 * r);

		// // inner circle
		cg.setColor(Color.BLACK);
		cg.drawOval(xCenter - (int) (0.75 * r), yCenter - (int) (0.75 * r), (int) (1.5 * r), (int) (1.5 * r));
		// // component dividers
		cg.drawLine(xCenter - (int) (0.535 * r), yCenter - (int) (0.535 * r), xCenter, yCenter + (int) (0.06 * r));
		cg.drawLine(xCenter + (int) (0.535 * r), yCenter - (int) (0.535 * r), xCenter, yCenter + (int) (0.06 * r));
		cg.drawLine(xCenter, yCenter + (int) (0.75 * r), xCenter, yCenter + (int) (0.06 * r));
	}
}
