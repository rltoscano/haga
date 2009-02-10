/**
 *    Copyright 2007 Robert Toscano and Shannon Iyo
 *
 *    This file is part of HAGA.
 *
 *    HAGA is free software: you can redistribute it and/or modify
 *    it under the terms of the GNU General Public License as published by
 *    the Free Software Foundation, either version 3 of the License, or
 *    any later version.
 *
 *    HAGA is distributed in the hope that it will be useful,
 *    but WITHOUT ANY WARRANTY; without even the implied warranty of
 *    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *    GNU General Public License for more details.
 *
 *    You should have received a copy of the GNU General Public License
 *    along with HAGA.  If not, see <http://www.gnu.org/licenses/>.
 */

package haga.gui;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Rectangle;

import javax.swing.JComponent;

/**
 * Allows detailed viewing of a portion of an alignment
 * 
 * @author Shannon Iyo
 * 
 */
public class AlignmentViewport extends JComponent {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1804841562859482966L;
	protected AlignmentModel model;
	protected int maxScore;
	private static final int PIXELS_PER_CHAR = 7;
	private static final int PADDING_PIXELS = 10;
	protected static final Font PLAIN_FONT = new Font("Lucida Console",
			Font.PLAIN, 11);
	protected static final Font BOLD_FONT = new Font("Lucida Console",
			Font.BOLD, 11);
	protected static final Font RULER_FONT = new Font("Lucida Console",
			Font.PLAIN, 8);
	protected static final Font LABEL_FONT_PLAIN = new Font("Lucida Console",
			Font.BOLD, 9);
	protected static final Font LABEL_FONT_BOLD = new Font("Lucida Console",
			Font.BOLD, 9);

	public AlignmentViewport(AlignmentModel model) {
		super();
		this.model = model;
		maxScore = model.getMaxScore(1);
		resize();

		// setBackground(Color.DARK_GRAY);
		model.addAlignmentListener(new AlignmentListener() {
			public void alignmentUpdated() {
				resize();
			}

			public void constraintsUpdated() {
				repaint();
			}
		});
	}

	public AlignmentModel getModel() {
		return model;
	}

	private void resize() {
		setPreferredSize(new Dimension(indexToPixel(model.getFirstSequence()
				.length())
				+ PADDING_PIXELS, 50));
		repaint();
	}

	// Converts from an x pixel coordinate to a sequence index
	public static int pixelToIndex(int pixel) {
		return Math.max(0, (pixel - PADDING_PIXELS) / PIXELS_PER_CHAR);
	}

	// Converts from a sequence index to an x pixel coordinate
	public static int indexToPixel(int index) {
		return PADDING_PIXELS + index * PIXELS_PER_CHAR;
	}

	@Override
	public void paint(Graphics g) {
		super.paint(g);

		// Draw the ruler
		Rectangle clipRect = g.getClipBounds();
		drawBackground(g, clipRect);

		drawRuler(g, clipRect);

		drawSequences(g, clipRect);
		drawLabels(g, clipRect);
	}

	protected void drawRuler(Graphics g, Rectangle clipRect) {
		g.setColor(Color.DARK_GRAY);
		g.setFont(RULER_FONT);

		for (int i = (pixelToIndex(clipRect.x) - pixelToIndex(clipRect.x) % 10); i <= pixelToIndex(clipRect.x
				+ clipRect.width); i += 10) {
			if (i % 50 == 0) {
				g.drawLine(indexToPixel(i), 69, indexToPixel(i), 80);
				g.drawString(String.valueOf(i), indexToPixel(i) + 2, 80);
			} else {
				g.drawLine(indexToPixel(i), 75, indexToPixel(i), 80);
			}
		}
	}

	protected void drawLabels(Graphics g, Rectangle clipRect) {
		g.setColor(Color.YELLOW.darker());
		g.setFont(LABEL_FONT_PLAIN);

		for (AlignmentConstraint constraint : model.getConstraints()) {
			int start1x = indexToPixel(constraint.label1.getStartIndex());
			int end1x = indexToPixel(constraint.label1.getEndIndex() + 1);
			int start2x = indexToPixel(constraint.label2.getStartIndex());
			int end2x = indexToPixel(constraint.label2.getEndIndex() + 1);
			if (((start1x >= clipRect.x) && (start1x < clipRect.x
					+ clipRect.width))
					|| ((end1x >= clipRect.x) && (end1x < clipRect.x
							+ clipRect.width))) {
				g.drawString(constraint.label1.toString(), start1x, 35);
			}

			if (((start2x >= clipRect.x) && (start2x < clipRect.x
					+ clipRect.width))
					|| ((end2x >= clipRect.x) && (end2x < clipRect.x
							+ clipRect.width))) {
				g.drawString(constraint.label2.toString(), start2x, 78);
			}
		}
	}

	protected void drawBackground(Graphics g, Rectangle clipRect) {
		g.setColor(Color.GRAY.brighter());
		g.fillRect(clipRect.x, clipRect.y, clipRect.width, clipRect.height);
	}

	protected void drawSequences(Graphics g, Rectangle clipRect) {
		int startIndex = pixelToIndex(clipRect.x);
		int endIndex = Math.min(pixelToIndex(clipRect.x + clipRect.width),
				model.getFirstSequence().length());

		g.setFont(PLAIN_FONT);

		char[] firstSeq = model.getFirstSequence().toCharArray();
		char[] secondSeq = model.getSecondSequence().toCharArray();
		for (int i = startIndex; i <= endIndex; i++) {
			float ratio = model.getScore(i, (i + 1)) / maxScore;
			g.setColor(new Color(1 - ratio, 1 - ratio, 1));
			// if (ratio == 1) {
			// g.setFont(boldFont);
			// } else {
			// g.setFont(plainFont);
			// }
			g.drawChars(firstSeq, i, 1, indexToPixel(i), 50);
			g.drawChars(secondSeq, i, 1, indexToPixel(i), 65);
		}
	}
}
