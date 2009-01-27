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

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.SwingUtilities;
import javax.swing.border.EmptyBorder;
import javax.swing.filechooser.FileFilter;

/**
 * This is a UI class that displays the alignment of 2 DNA sequences and allows
 * the user to edit and constrain the alignment.
 * 
 * Allows for scroll and zoom operations. Contains an AlignmentScroller and an
 * AlignmentViewport.
 */
public class AlignmentManager extends JComponent {

    private static final long serialVersionUID = -6049734058594112043L;
    private AlignmentModel model;
    private List<AlignmentManagerListener> listeners;

    public AlignmentManager() {
        this(new AlignmentModel());
    }

    public AlignmentManager(AlignmentModel model) {
        super();
        this.model = model;
        listeners = new LinkedList<AlignmentManagerListener>();
        setupUI();
    }

    // Create the UI
    private void setupUI() {
        AlignmentEditor editor = new AlignmentEditor(model);
        final JScrollPane scrollPane = new JScrollPane(editor);

        final AlignmentScrollBar scroller = new AlignmentScrollBar(model);
        scrollPane.setHorizontalScrollBar(scroller);

        model.addAlignmentListener(new AlignmentListener() {

            public void alignmentUpdated() {
                scrollPane.revalidate();
                scrollPane.repaint();
            }

            public void constraintsUpdated() {
            }
        });

        JPanel buttonPanel = new JPanel();
        buttonPanel.setLayout(new BoxLayout(buttonPanel, BoxLayout.LINE_AXIS));
        JButton loadButton = new JButton("Load HMM labeler..");
        loadButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                loadPressed();
            }
        });
        JButton trainLabelerButton = new JButton("Train labeler");
        trainLabelerButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                trainLabelerPressed();
            }
        });
        JButton labelButton = new JButton("Label sequences");
        labelButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                labelPressed();
            }
        });
        JButton trainAlignerButton = new JButton("Train aligner");
        trainAlignerButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                trainAlignerPressed();
            }
        });
        JButton alignButton = new JButton("Align sequences");
        alignButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                alignPressed();
            }
        });

        // buttonPanel.add(Box.createHorizontalStrut(10));
        buttonPanel.add(loadButton);
        buttonPanel.add(Box.createHorizontalGlue());
        buttonPanel.add(labelButton);
        buttonPanel.add(Box.createHorizontalStrut(10));
        buttonPanel.add(trainLabelerButton);
        buttonPanel.add(Box.createHorizontalStrut(10));
        buttonPanel.add(trainAlignerButton);
        buttonPanel.add(Box.createHorizontalStrut(10));
        buttonPanel.add(alignButton);
        buttonPanel.setBorder(new EmptyBorder(5, 5, 5, 5));

        setLayout(new BorderLayout());
        add(buttonPanel, BorderLayout.NORTH);
        add(scrollPane, BorderLayout.CENTER);
        setPreferredSize(new Dimension(800, 200));
    }

    private void loadPressed() {
        // Load XML file
        JFileChooser fc = new JFileChooser();
        fc.setAcceptAllFileFilterUsed(false);
        fc.setFileFilter(new FileFilter() {
            @Override
            public boolean accept(File f) {
                if (f.isDirectory()) {
                    return true;
                }
                String name = f.getName();
                int index = name.toLowerCase().lastIndexOf(".xml");
                return ((index != -1) && (index == name.length() - 4));
            }

            @Override
            public String getDescription() {
                return "XML files";
            }
        });

        if (fc.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
            File file = fc.getSelectedFile();
            for (AlignmentManagerListener listener : listeners) {
                listener.xmlHmmFileLoaded(file);
            }
        }
    }

    private void alignPressed() {
        for (AlignmentManagerListener listener : listeners) {
            listener.alignSequencesPressed(model.getFirstLabeledSequence(),
                    model.getSecondLabeledSequence());
        }
    }

    private void labelPressed() {
        for (AlignmentManagerListener listener : listeners) {
            listener.labelSequencesPressed(model.getLabeledAlignment().getLS1(),
                                           model.getLabeledAlignment().getLS2());
        }
    }

    private void trainLabelerPressed() {
        for (AlignmentManagerListener listener : listeners) {
            listener.trainLabelerPressed(model.getFirstLabeledSequence(), model
                    .getSecondLabeledSequence());
        }
    }

    private void trainAlignerPressed() {
        for (AlignmentManagerListener listener : listeners) {
            listener.trainAlignerPressed(model.getLabeledAlignment());
        }
    }

    public boolean addListener(AlignmentManagerListener listener) {
        return listeners.add(listener);
    }

    public boolean removeListener(AlignmentManagerListener listener) {
        return listeners.remove(listener);
    }

    public static void main(String args[]) {
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                JFrame frame = new JFrame("Alignment Manager");
                Random rand = new Random();
                char[] bases = new char[] { 'A', 'C', 'G', 'T' };

                String s1 = "ACGTACGTACGTACGTACGTACGTACGTACGTACGTACGTACGTACGTACGTACGTACGTACGTACGTACGTACGTACGTACGTACGTACGTACGTACGTACGTACGTACGTACGTACGT"
                        + "ATATATATATCGCGCGCGCGATATATATATCGCGCGCGCGATATATATATCGCGCGCGCGATATATATATCGCGCGCGCGATATATATATCGCGCGCGCGATATATATATCGCGCGCGCG";
                String s2 = "TGCATGCATGCATGCATGCATGCATGCATGCATGCATGCATGCATGCATGCATGCATGCATGCATGCATGCATGCATGCATGCATGCATGCATGCATGCATGCATGCATGCATGCATGCA"
                        + "ATATATATATCGCGCGCGCGATATATATATCGCGCGCGCGATATATATATCGCGCGCGCGATATATATATCGCGCGCGCGATATATATATCGCGCGCGCGATATATATATCGCGCGCGCG";
                for (int i = s1.length(); i < 5000; i++) {
                    s1 += bases[rand.nextInt(4)];
                    s2 += bases[rand.nextInt(4)];
                }
                AlignmentModel testModel = new AlignmentModel(s1, s2);
                frame.add(new AlignmentManager(testModel));
                frame.pack();
                frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
                frame.setVisible(true);
            }
        });
    }

    public AlignmentModel getModel() {
        return this.model;
    }
}
