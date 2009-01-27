/**
 *    Copyright 2007 Robert Toscano and Shannon Iyo
 *
 *    This file is part of HAGA.
 *
 *    HAGA is free software: you can redistribute it and/or modify
 *    it under the terms of the GNU General Public License as published by
 *    the Free Software Foundation, either version 3 of the License, or
 *    (at your option) any later version.
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
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.MouseEvent;

import javax.swing.BoundedRangeModel;
import javax.swing.JComponent;
import javax.swing.JList;
import javax.swing.JScrollBar;
import javax.swing.JScrollPane;
import javax.swing.JViewport;
import javax.swing.SwingUtilities;
import javax.swing.plaf.metal.MetalScrollBarUI;

import sun.swing.DefaultLookup;

/**
 * A custom scroll bar for scrolling along a sequence alignment
 * 
 * @author Shannon Iyo
 * 
 */
public class AlignmentScrollBar extends JScrollBar {

    /**
     * 
     */
    private static final long serialVersionUID = 5828224619826226147L;
    private AlignmentModel alignmentModel;

    public AlignmentScrollBar(AlignmentModel model) {
        super();
        this.alignmentModel = model;
        setPreferredSize(new Dimension(100, 47));
        setOrientation(JScrollBar.HORIZONTAL);
        setUI(new AlignmentScrollBarUI());
        setUnitIncrement(model.getFirstSequence().length() / 20);
        setBlockIncrement(model.getFirstSequence().length() / 20);
    }

    @Override
    public void setOrientation(int orientation) {
        if (orientation != JScrollBar.HORIZONTAL) {
            return;
        }
        super.setOrientation(orientation);
    }

    /**
     * This is the custom UI for the AlignmentScrollBar class
     * 
     * @author Shannon Iyo
     * 
     */
    public class AlignmentScrollBarUI extends MetalScrollBarUI {
        public AlignmentScrollBarUI() {
            super();
        }

        @Override
        protected Dimension getMinimumThumbSize() {
            return new Dimension(1, 1);
        }

        @Override
        protected TrackListener createTrackListener() {
            return new TrackListener() {
                /**
                 * If the mouse is pressed outside the "thumb" component then
                 * scroll to the position pressed.
                 */
                @Override
                public void mousePressed(MouseEvent e) {
                    if (SwingUtilities.isRightMouseButton(e)
                            || (!getSupportsAbsolutePositioning() && SwingUtilities
                                    .isMiddleMouseButton(e)))
                        return;
                    if (!scrollbar.isEnabled())
                        return;

                    if (!scrollbar.hasFocus()
                            && scrollbar.isRequestFocusEnabled()) {
                        scrollbar.requestFocus();
                    }

                    // useCachedValue = true;
                    scrollbar.setValueIsAdjusting(true);

                    currentMouseX = e.getX();
                    currentMouseY = e.getY();

                    // Clicked in the Thumb area?
                    if (!getThumbBounds()
                            .contains(currentMouseX, currentMouseY)) {
                        /*
                         * if (getSupportsAbsolutePositioning() &&
                         * SwingUtilities.isMiddleMouseButton(e))
                         */
                        switch (scrollbar.getOrientation()) {
                        case JScrollBar.VERTICAL:
                            offset = getThumbBounds().height / 2;
                            break;
                        case JScrollBar.HORIZONTAL:
                            offset = getThumbBounds().width / 2;
                            break;
                        }
                        isDragging = true;
                        setValueFrom(e);
                        return;
                    } else {
                        super.mousePressed(e);
                    }
                }

                private void setValueFrom(MouseEvent e) {
                    boolean active = isThumbRollover();
                    BoundedRangeModel model = scrollbar.getModel();
                    Rectangle thumbR = getThumbBounds();
                    // float trackLength;
                    int thumbMin, thumbMax, thumbPos;

                    if (scrollbar.getOrientation() == JScrollBar.VERTICAL) {
                        thumbMin = decrButton.getY() + decrButton.getHeight();
                        thumbMax = incrButton.getY() - thumbR.height;
                        thumbPos = Math.min(thumbMax, Math.max(thumbMin, (e
                                .getY() - offset)));
                        setThumbBounds(thumbR.x, thumbPos, thumbR.width,
                                thumbR.height);
                        // trackLength = getTrackBounds().height;
                    } else {
                        if (scrollbar.getComponentOrientation().isLeftToRight()) {
                            thumbMin = decrButton.getX()
                                    + decrButton.getWidth();
                            thumbMax = incrButton.getX() - thumbR.width;
                        } else {
                            thumbMin = incrButton.getX()
                                    + incrButton.getWidth();
                            thumbMax = decrButton.getX() - thumbR.width;
                        }
                        thumbPos = Math.min(thumbMax, Math.max(thumbMin, (e
                                .getX() - offset)));
                        setThumbBounds(thumbPos, thumbR.y, thumbR.width,
                                thumbR.height);
                        // trackLength = getTrackBounds().width;
                    }

                    /*
                     * Set the scrollbars value. If the thumb has reached the
                     * end of the scrollbar, then just set the value to its
                     * maximum. Otherwise compute the value as accurately as
                     * possible.
                     */
                    if (thumbPos == thumbMax) {
                        if (scrollbar.getOrientation() == JScrollBar.VERTICAL
                                || scrollbar.getComponentOrientation()
                                        .isLeftToRight()) {
                            scrollbar.setValue(model.getMaximum()
                                    - model.getExtent());
                        } else {
                            scrollbar.setValue(model.getMinimum());
                        }
                    } else {
                        float valueMax = model.getMaximum() - model.getExtent();
                        float valueRange = valueMax - model.getMinimum();
                        float thumbValue = thumbPos - thumbMin;
                        float thumbRange = thumbMax - thumbMin;
                        int value;
                        if (scrollbar.getOrientation() == JScrollBar.VERTICAL
                                || scrollbar.getComponentOrientation()
                                        .isLeftToRight()) {
                            value = (int) (0.5 + ((thumbValue / thumbRange) * valueRange));
                        } else {
                            value = (int) (0.5 + (((thumbMax - thumbPos) / thumbRange) * valueRange));
                        }

                        // useCachedValue = true;
                        int scrollBarValue = value + model.getMinimum();
                        scrollbar
                                .setValue(adjustValueIfNecessary(scrollBarValue));
                    }
                    setThumbRollover(active);
                }

                private int adjustValueIfNecessary(int value) {
                    if (scrollbar.getParent() instanceof JScrollPane) {
                        JScrollPane scrollpane = (JScrollPane) scrollbar
                                .getParent();
                        JViewport viewport = scrollpane.getViewport();
                        Component view = viewport.getView();
                        if (view instanceof JList) {
                            JList list = (JList) view;
                            if (DefaultLookup.getBoolean(list, list.getUI(),
                                    "List.lockToPositionOnScroll", false)) {
                                int adjustedValue = value;
                                int mode = list.getLayoutOrientation();
                                int orientation = scrollbar.getOrientation();
                                if (orientation == JScrollBar.VERTICAL
                                        && mode == JList.VERTICAL) {
                                    int index = list.locationToIndex(new Point(
                                            0, value));
                                    Rectangle rect = list.getCellBounds(index,
                                            index);
                                    if (rect != null) {
                                        adjustedValue = rect.y;
                                    }
                                }
                                if (orientation == JScrollBar.HORIZONTAL
                                        && (mode == JList.VERTICAL_WRAP || mode == JList.HORIZONTAL_WRAP)) {
                                    if (scrollpane.getComponentOrientation()
                                            .isLeftToRight()) {
                                        int index = list
                                                .locationToIndex(new Point(
                                                        value, 0));
                                        Rectangle rect = list.getCellBounds(
                                                index, index);
                                        if (rect != null) {
                                            adjustedValue = rect.x;
                                        }
                                    } else {
                                        Point loc = new Point(value, 0);
                                        int extent = viewport.getExtentSize().width;
                                        loc.x += extent - 1;
                                        int index = list.locationToIndex(loc);
                                        Rectangle rect = list.getCellBounds(
                                                index, index);
                                        if (rect != null) {
                                            adjustedValue = rect.x + rect.width
                                                    - extent;
                                        }
                                    }
                                }
                                value = adjustedValue;

                            }
                        }
                    }
                    return value;
                }
            };
        }

        @Override
        protected void paintThumb(Graphics g, JComponent comp, Rectangle rect) {

            // float trackW = getTrackBounds().width;
            // float extent = scrollbar.getVisibleAmount();
            // float range = scrollbar.getMaximum() - scrollbar.getMinimum();
            // int trueWidth = (range <= 0) ? getMaximumThumbSize().width
            // : (int) (trackW * (extent / range));
            // float position = scrollbar.getValue() / (float)
            // scrollbar.getMaximum();
            // int offset = (int) ((rect.width - trueWidth) * position);
            // System.out.println(position * 10000);

            g.setColor(Color.LIGHT_GRAY.brighter());
            g.fillRect(rect.x, rect.y, rect.width, 10);
            g.fillRect(rect.x, rect.y + 21, rect.width, 4);
            g.fillRect(rect.x, rect.y + 36, rect.width, 10);

            // g.setColor(Color.LIGHT_GRAY.brighter());
            // g.fillRect(rect.x + offset, rect.y, trueWidth, 10);
            // g.fillRect(rect.x + offset, rect.y + 21, trueWidth, 4);
            // g.fillRect(rect.x + offset, rect.y + 36, trueWidth, 10);

            g.setColor(Color.DARK_GRAY);
            g.drawRect(rect.x, rect.y, rect.width, rect.height - 1);
        }

        @Override
        protected void paintTrack(Graphics g, JComponent comp, Rectangle rect) {

            g.setColor(Color.GRAY);
            g.drawLine(rect.x, rect.y, rect.x + rect.width, rect.y);
            g.drawLine(rect.x, rect.y + rect.height, rect.x + rect.width,
                    rect.y + rect.height);

            // Draw an interpretation of the two strands
            int numPixels = rect.width;
            int numBases = alignmentModel.getFirstSequence().length();
            int bpp = numBases / numPixels; // bases per pixel

            float maxScore = alignmentModel.getMaxScore(Math.max(1, bpp));

            if (bpp > 0) {
                // TODO: smooth out the colors?
                for (int i = 0; i < numPixels; i++) {
                    float ratio = alignmentModel.getScore(i * bpp, (i + 1)
                            * bpp - 1)
                            / maxScore;
                    g.setColor(new Color(1 - ratio, 1 - ratio, 1));

                    g
                            .drawLine(i + rect.x, rect.y + 10, i + rect.x,
                                    rect.y + 20);
                    g
                            .drawLine(i + rect.x, rect.y + 25, i + rect.x,
                                    rect.y + 35);
                }
            } else {
                // TODO: this still doesn't work well for numpixels > numbases
                int ppb = numPixels / numBases; // pixels per base
                // System.out.println(ppb);
                for (int i = 0; i < numBases; i++) {
                    float ratio = alignmentModel.getScore(i, i) / maxScore;
                    g.setColor(new Color(1 - ratio, 1 - ratio, 1));

                    g.fillRect(i * ppb + rect.x, rect.y + 10, ppb, rect.y + 10);
                    g.fillRect(i * ppb + rect.x, rect.y + 25, ppb, rect.y + 10);
                }
            }
        }
    }
}
