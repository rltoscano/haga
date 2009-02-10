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

package haga;

import haga.align.Aligner;
import haga.align.AlignerFactory;
import haga.gui.AlignmentManager;
import haga.gui.AlignmentManagerListener;
import haga.gui.AlignmentModel;
import haga.labeler.HMMLabeler;
import haga.labeler.LabeledAlignment;
import haga.labeler.LabeledSequence;
import haga.labeler.LabelerFactory;
import haga.labeler.XmlHmmLoader;

import java.io.File;
import java.util.Random;

import javax.swing.JFrame;
import javax.swing.SwingUtilities;

public class Haga {

    public static void main (String[] args) {
        if (args.length > 0) {
            // -lhmm --labelerHmm <path to xmlFile>
            // -anw --alignerNeedlemanWunsch <path to xmlFile>    
        } else {
            
        }
        
        // prepare Aligner
        AlignerFactory.SetInstance(new Aligner());
        
        // prepare Labeler with a default hmm labeler loaded from res/hmm.xml
        LabelerFactory.SetInstance(XmlHmmLoader.loadXmlFile(
                new File("res" + File.separator + "hmm.xml")));
        
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                // generate some random sequences by default, so the user has
                // something to play with
                char[] seq1 = HagaUtils.generateNucleotideSequence(1000);
                char[] seq2 = HagaUtils.generateNucleotideSequence(1000);

                AlignmentModel testModel = new AlignmentModel(new String(seq1), 
                                                              new String(seq2));
                final AlignmentManager manager = new AlignmentManager(testModel);
                manager.addListener(new AlignmentManagerListener() {

                    public void alignSequencesPressed(LabeledSequence s1, 
                                                      LabeledSequence s2) {
                        LabeledAlignment la = 
                            AlignerFactory.GetInstance().align(s1, s2);
                        manager.getModel().updateAlignment(la);
                    }

                    public void trainAlignerPressed(LabeledAlignment la) {
                        AlignerFactory.GetInstance().train(la);
                    }

                    public void trainLabelerPressed(LabeledSequence s1,
                                                    LabeledSequence s2) {
                    }

                    public void xmlHmmFileLoaded(File xmlFile) {
                        LabelerFactory.SetInstance(
                                XmlHmmLoader.loadXmlFile(xmlFile));
                    }

                    public void labelSequencesPressed(LabeledSequence ls1, 
                                                      LabeledSequence ls2) {
                        ls1 = LabelerFactory.GetInstance().labelSequence(ls1);
                        ls2 = LabelerFactory.GetInstance().labelSequence(ls2);
                        manager.getModel().updateAlignment(
                                new LabeledAlignment(ls1, ls2));
                    }

                });

                JFrame frame = new JFrame("Alignment Manager");
                frame.add(manager);
                frame.pack();
                frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
                frame.setVisible(true);
            }
        });
    }
}
