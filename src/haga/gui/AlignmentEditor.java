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

import haga.labeler.Label;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.HashMap;
import java.util.Map;

import javax.swing.JOptionPane;

/**
 * Extends AlignmentViewport to allow editing of alignments
 * 
 * @author Shannon Iyo
 * 
 */
public class AlignmentEditor extends AlignmentViewport {

	/**
	 * 
	 */
	private static final long serialVersionUID = 2607078383317442855L;
	private int selectionStart1, selectionEnd1;
	private int selectionStart2, selectionEnd2;
	private Rectangle selectionBox;
	private int selectedConstraint;
	private Map<String, Color> labelColors;

	public static final Color[] LABEL_COLORS = new Color[] { Color.PINK,
			Color.CYAN.darker(), Color.ORANGE };

	public AlignmentEditor(AlignmentModel model) {
		super(model);

		labelColors = new HashMap<String, Color>();
		model.addAlignmentListener(new AlignmentListener() {
			@Override
			public void alignmentUpdated() {
				updateColors();
			}

			@Override
			public void constraintsUpdated() {
				updateColors();
			}
		});
		clearSelection();
		AlignmentAdapter adapter = new AlignmentAdapter();
		addMouseListener(adapter);
		addMouseMotionListener(adapter);
		addKeyListener(adapter);
		setToolTipText("Highlight sequences with mouse. ENTER to define an alignment, DELETE to remove one.");
	}

	private void updateColors() {
		for (Label label : model.getFirstLabeledSequence().getLabels()) {
			if (labelColors.get(label.toString()) == null) {
				labelColors.put(label.toString(), LABEL_COLORS[labelColors
						.keySet().size()
						% LABEL_COLORS.length]);
			}
		}

		for (Label label : model.getSecondLabeledSequence().getLabels()) {
			if (labelColors.get(label.toString()) == null) {
				labelColors.put(label.toString(), LABEL_COLORS[labelColors
						.keySet().size()
						% LABEL_COLORS.length]);
			}
		}
	}

	private void clearSelection() {
		selectionBox = null;
		selectionStart1 = -1;
		selectionEnd1 = -1;
		selectionStart2 = -1;
		selectionEnd2 = -1;
		selectedConstraint = -1;
	}

	// @Override
	// public void paint(Graphics g) {
	// super.paint(g);
	// // paintSelectionBox(g);
	// }

	@Override
	protected void drawBackground(Graphics g, Rectangle clipRect) {
		g.setColor(Color.GRAY.brighter());
		g.fillRect(clipRect.x, clipRect.y, clipRect.width, clipRect.height);
		drawHighlights(g, clipRect);
	}

	protected void drawHighlights(Graphics g, Rectangle clipRect) {

		for (Label label : model.getFirstLabeledSequence().getLabels()) {
			drawHighlight(g, clipRect, labelColors.get(label.toString()), 39,
					label.getStartIndex(), label.getEndIndex());
		}

		for (Label label : model.getSecondLabeledSequence().getLabels()) {
			drawHighlight(g, clipRect, labelColors.get(label.toString()), 54,
					label.getStartIndex(), label.getEndIndex());
		}

		for (AlignmentConstraint constraint : model.getConstraints()) {
			drawHighlight(g, clipRect, labelColors.get(constraint.label1
					.toString()), 39, constraint.label1.getStartIndex(),
					constraint.label1.getEndIndex());

			drawHighlight(g, clipRect, labelColors.get(constraint.label2
					.toString()), 54, constraint.label2.getStartIndex(),
					constraint.label2.getEndIndex());
		}

		if (selectedConstraint != -1) {
			// System.out.println(model.getConstraints());
			AlignmentConstraint constraint = model.getConstraints().get(
					selectedConstraint);
			drawHighlight(g, clipRect, Color.GREEN.darker(), 39,
					constraint.label1.getStartIndex(), constraint.label1
							.getEndIndex());

			drawHighlight(g, clipRect, Color.GREEN.darker(), 54,
					constraint.label2.getStartIndex(), constraint.label2
							.getEndIndex());
		}

		if (selectionStart1 != -1) {
			drawHighlight(g, clipRect, Color.GRAY, 39, selectionStart1,
					selectionEnd1);
		}

		if (selectionStart2 != -1) {
			drawHighlight(g, clipRect, Color.GRAY, 54, selectionStart2,
					selectionEnd2);
		}
	}

	protected void drawHighlight(Graphics g, Rectangle clipRect, Color color,
			int y, int startIndex, int endIndex) {
		g.setColor(color);
		int x = indexToPixel(startIndex);
		int width = indexToPixel(endIndex + 1) - x;
		Rectangle selected = new Rectangle(x, y, width, 15);
		Rectangle intersect = selected.intersection(clipRect);
		g.fillRect(intersect.x, intersect.y, intersect.width, intersect.height);
	}

	@Override
	protected void drawLabels(Graphics g, Rectangle clipRect) {

		g.setFont(LABEL_FONT_PLAIN);
		Color labelColor = Color.YELLOW.darker();
		Color fontColor = Color.WHITE;
		for (Label label : model.getFirstLabeledSequence().getLabels()) {
			drawLabel(g, clipRect, labelColors.get(label.toString()),
					fontColor, 28, label.getStartIndex(), label.getEndIndex(),
					label.toString());
		}

		for (Label label : model.getSecondLabeledSequence().getLabels()) {
			drawLabel(g, clipRect, labelColors.get(label.toString()),
					fontColor, 69, label.getStartIndex(), label.getEndIndex(),
					label.toString());
		}

		fontColor = Color.BLACK;
		for (AlignmentConstraint constraint : model.getConstraints()) {
			// g.setFont(LABEL_FONT_PLAIN);
			labelColor = (model.getConstraints().indexOf(constraint) == selectedConstraint) ? Color.GREEN
					.darker()
					: labelColors.get(constraint.label1.toString());

			drawLabel(g, clipRect, labelColor, fontColor, 28, constraint.label1
					.getStartIndex(), constraint.label1.getEndIndex(),
					constraint.label1.toString());

			drawLabel(g, clipRect, labelColor, fontColor, 69, constraint.label2
					.getStartIndex(), constraint.label2.getEndIndex(),
					constraint.label2.toString());
		}
	}

	protected void drawLabel(Graphics g, Rectangle clipRect, Color labelColor,
			Color fontColor, int y, int startIndex, int endIndex,
			String labelString) {

		int startX = indexToPixel(startIndex);
		int endX = indexToPixel(endIndex + 1);

		if (((startX >= clipRect.x) && (startX < clipRect.x + clipRect.width))
				|| ((endX >= clipRect.x) && (endX < clipRect.x + clipRect.width))) {
			g.setColor(labelColor);
			g.fillRect(startX, y, 4 + 5 * labelString.length(), 11);
			g.setColor(fontColor);
			g.drawString(labelString, startX + 2, y + 9);
		}
	}

	@Override
	protected void drawSequences(Graphics g, Rectangle clipRect) {
		int startIndex = pixelToIndex(clipRect.x);
		int endIndex = Math.min(pixelToIndex(clipRect.x + clipRect.width),
				model.getFirstSequence().length());

		char[] firstSeq = model.getFirstSequence().toCharArray();
		char[] secondSeq = model.getSecondSequence().toCharArray();
		for (int i = startIndex; i < endIndex; i++) {
			float ratio = model.getScore(i, i) / maxScore;
			g.setColor(new Color(1 - ratio, 1 - ratio, 1));
			if ((i >= selectionStart1 && i <= selectionEnd1)
					|| (selectedConstraint != -1
							&& i >= model.getConstraints().get(
									selectedConstraint).label1.getStartIndex() && i <= model
							.getConstraints().get(selectedConstraint).label1
							.getEndIndex())) {
				g.setFont(BOLD_FONT);
			} else {
				g.setFont(PLAIN_FONT);
			}
			g.drawChars(firstSeq, i, 1, indexToPixel(i), 50);

			if ((i >= selectionStart2 && i <= selectionEnd2)
					|| (selectedConstraint != -1
							&& i >= model.getConstraints().get(
									selectedConstraint).label2.getStartIndex() && i <= model
							.getConstraints().get(selectedConstraint).label2
							.getEndIndex())) {
				g.setFont(BOLD_FONT);
			} else {
				g.setFont(PLAIN_FONT);
			}
			g.drawChars(secondSeq, i, 1, indexToPixel(i), 65);
		}
	}

	private void doCreateConstraint(int start1, int end1, int start2, int end2,
			String name) {
		model.addConstraint(new AlignmentConstraint(new Label(name, start1,
				end1, true), new Label(name, start2, end2, true)));
	}

	private void doDeleteSelectedConstraint() {
		if (selectedConstraint != -1) {
			model.removeConstraint(selectedConstraint);
			selectedConstraint = -1;
		}
	}

	// private void paintSelectionBox(Graphics g) {
	// System.out.println(selectionBox);
	// if (selectionBox != null) {
	// g.setColor(Color.DARK_GRAY);
	// g.drawRect(selectionBox.x, selectionBox.y, selectionBox.width,
	// selectionBox.height);
	// }
	// }

	private class AlignmentAdapter extends MouseAdapter implements KeyListener {

		// private boolean dragging;
		private int selectionSequence; // Which sequence has been selected
		private Point selectStart;

		public AlignmentAdapter() {
			// dragging = false;
			selectionSequence = -1;
		}

		@Override
		public void mousePressed(MouseEvent e) {
			Point p = e.getPoint();
			selectedConstraint = -1;
			if (p.y >= 39 && p.y < 54) {
				for (AlignmentConstraint constraint : model.getConstraints()) {
					if (constraint.firstSeqContains(pixelToIndex(p.x))) {
						clearSelection();
						selectedConstraint = model.getConstraints().indexOf(
								constraint);
					}
				}
			} else if (p.y >= 54 && p.y < 69) {
				for (AlignmentConstraint constraint : model.getConstraints()) {
					if (constraint.secondSeqContains(pixelToIndex(p.x))) {
						clearSelection();
						selectedConstraint = model.getConstraints().indexOf(
								constraint);
					}
				}
			}
			if (selectedConstraint == -1) {
				selectionSequence = (p.y < 53) ? 1 : 2;
				selectStart = p;
			}
			repaint();
		}

		@Override
		public void mouseDragged(MouseEvent e) {
			Point p = e.getPoint();
			Rectangle previous = selectionBox;

			selectionBox = new Rectangle(Math.min(selectStart.x, p.x), Math
					.min(selectStart.y, p.y), Math.abs(selectStart.x - p.x),
					Math.abs(selectStart.y - p.y));
			if (previous == null) {
				// repaint(selectionBox.x, 0, selectionBox.width + 1,
				// Integer.MAX_VALUE);
				repaint();
			} else {
				if (selectionSequence == 1) {
					selectionStart1 = pixelToIndex(selectionBox.x);
					selectionEnd1 = pixelToIndex(selectionBox.x
							+ selectionBox.width);
				} else if (selectionSequence == 2) {
					selectionStart2 = pixelToIndex(selectionBox.x);
					selectionEnd2 = pixelToIndex(selectionBox.x
							+ selectionBox.width);
				}
				repaint();
				// repaint(Math.min(previous.x, selectionBox.x), 0,
				// Math.max(
				// previous.width, selectionBox.width) + 1,
				// Integer.MAX_VALUE);
			}
			// }
		}

		@Override
		public void mouseClicked(MouseEvent e) {
		}

		@Override
		public void mouseReleased(MouseEvent e) {
			requestFocusInWindow();
			if (selectionSequence != -1) {
				selectionSequence = -1;

				repaint();
				selectionBox = null;
			}
		}

		public void keyPressed(KeyEvent e) {
			switch (e.getKeyCode()) {
			case KeyEvent.VK_ENTER:
				if (selectionStart1 != -1 && selectionStart2 != -1) {
					String label = (String) JOptionPane.showInputDialog(
							AlignmentEditor.this,
							"Enter label for the constraint: ",
							"New constraint", JOptionPane.QUESTION_MESSAGE,
							null, null, "User");
					if (label != null) {
						selectionSequence = -1;
						doCreateConstraint(selectionStart1, selectionEnd1,
								selectionStart2, selectionEnd2, label);
						clearSelection();
						repaint();
					}
				}
				break;
			case KeyEvent.VK_DELETE:
				doDeleteSelectedConstraint();
				clearSelection();
				repaint();
				break;
			case KeyEvent.VK_ESCAPE:
				clearSelection();
				repaint();
				break;
			default:
				break;
			}
		}

		public void keyReleased(KeyEvent e) {
		}

		public void keyTyped(KeyEvent e) {
		}
	}
}
