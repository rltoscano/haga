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

package haga.align;

import haga.NucleotideLabeledSequence;
import haga.labeler.Label;
import haga.labeler.LabeledAlignment;
import haga.labeler.LabeledSequence;
import haga.labeler.LabeledSequenceImpl;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.SortedSet;
import java.util.TreeSet;


public class Aligner {

    private Map<String, GlobalAlignmentAlgorithm> labelToAligner = 
        new HashMap<String, GlobalAlignmentAlgorithm>();
    private GlobalAlignmentAlgorithm bkgrndAligner = 
        new NeedlemanWunschIyoToscano();

    public LabeledAlignment align(LabeledSequence ls1, LabeledSequence ls2) {

        // remove gaps from sequences
        LabeledAlignment laWithGaps = new LabeledAlignment(ls1, ls2);
        LabeledAlignment laWithoutGaps = removeGaps(laWithGaps);
        ls1 = laWithoutGaps.getLS1();
        ls2 = laWithoutGaps.getLS2();

        // initialize some convenience variables
        char[] seq1 = ls1.getSequence();
        char[] seq2 = ls2.getSequence();
        List<Label> labels1 = ls1.getLabels(),
                    labels2 = ls2.getLabels(),
                    userLabels1 = new LinkedList<Label>(), 
                    userLabels2 = new LinkedList<Label>();

        // filter out the user labels
        for (Label l : labels1)
            if (l.userDefined)
                userLabels1.add(l);
        for (Label l : labels2)
            if (l.userDefined)
                userLabels2.add(l);

        // do some argument checking
        if (userLabels1.size() != userLabels2.size())
            throw new IllegalStateException("Aligner: The two labeled sequences"
                    + "need to have the same number of user-defined labels.");

        /*
         * pieces = divide alignment into AlignmentPieces
         * for each piece p in pieces
         *   if p is a user defined piece
         *     Alignment a = getAligner(p.labelName).align(p.seq1, p.seq2)
         *     resultPieces.append(toLabeledAlignment(a, p.labelName))
         *   else
         *     Alignment a = bkgrndAligner.align(p.seq1, p.seq2, lbls1, lbls2)
         *     resultPieces.append(toLabeledAlignment(a, lbls1, lbls2))
         * 
         * return resultPieces
         */

        List<AlignmentPiece> unalignedPieces = split(userLabels1.size(), ls1,
                ls2);
        AlignmentHelper alignedPieces = new AlignmentHelper();

        for (AlignmentPiece p : unalignedPieces) {
            if (p.isUserDefined) {
                GlobalAlignmentAlgorithm aligner = 
                        labelToAligner.get(p.labelName);

                // lazy initialization - create a new aligner for this label if
                // its the first time processing this label
                if (aligner == null) { // first time we've seen this label
                    aligner = new NeedlemanWunsch();
                    labelToAligner.put(p.labelName, aligner);
                }

                Label userLabel =
                    new Label(p.labelName, 0, p.seq1.length-1, true);
                Alignment a = aligner.align(
                        new NucleotideLabeledSequence(p.seq1, userLabel),
                        new NucleotideLabeledSequence(p.seq2, userLabel));
                alignedPieces.append(toLabeledAlignment(a, p.labelName));

            } else {
                Alignment a = bkgrndAligner.align(
                        new NucleotideLabeledSequence(p.seq1, p.lbls1),
                        new NucleotideLabeledSequence(p.seq2, p.lbls2));
                p.lbls1 = adjustLabelsForGaps(a.getSequence1(), p.lbls1);
                p.lbls2 = adjustLabelsForGaps(a.getSequence2(), p.lbls2);
                alignedPieces.append(toLabeledAlignment(a, p.lbls1, p.lbls2));
            }
        }

        return alignedPieces.toLabeledAlignment();
    }

    // Assumes that labels is sorted by sequential order
    private List<Label> adjustLabelsForGaps(char[] sequence,
                                            List<Label> labels) {
        if (labels.size() == 0) return labels;

        List<Label> newLabels = new ArrayList<Label>();

        Iterator<Label> labelIterator = labels.iterator();
        Label currentLabel = labelIterator.next();
        int totalGaps = 0;
        int nonGaps = 0;
        int labelStart = 0;
        for (int i = 0; i < sequence.length; i++) {
            if (sequence[i] == '-') {
                totalGaps++;
            } else {
                nonGaps++;
                if (nonGaps == currentLabel.getEndIndex()
                        - currentLabel.getStartIndex() + 1) {
                    
                    while(i + 1 < sequence.length && sequence[i+1] == '-') {
                        i++;
                    }
                    
                    if (!labelIterator.hasNext()) {
                        newLabels.add(new Label(currentLabel.toString(),
                                labelStart, sequence.length - 1,
                                currentLabel.userDefined));
                        break;
                    }
                    newLabels.add(new Label(currentLabel.toString(),
                            labelStart, i, currentLabel.userDefined));
                    nonGaps = 0;
                    labelStart = i + 1;
                    currentLabel = labelIterator.next();
                }
            }
        }

        return newLabels;
    }

    // splits the LabeledSequences into a series of AlignmentPieces, split on
    // the user-/non-user-specified and user labels
    private List<AlignmentPiece> split(int count, LabeledSequence ls1,
            LabeledSequence ls2) {
        List<AlignmentPiece> result = new LinkedList<AlignmentPiece>();
        List<Label> tempBuckets1 = new LinkedList<Label>();
        List<Label> tempBuckets2 = new LinkedList<Label>();

        Iterator<Label> lbls1Iterator, lbls2Iterator;
        lbls1Iterator = ls1.getLabels().iterator();
        lbls2Iterator = ls2.getLabels().iterator();

        while (lbls1Iterator.hasNext() || lbls2Iterator.hasNext()) {
            Label curr1 = null, curr2 = null;
            while (lbls1Iterator.hasNext()) {
                curr1 = lbls1Iterator.next();

                if (curr1.userDefined) {
                    break;

                } else {
                    tempBuckets1.add(curr1);
                }
            }

            while (lbls2Iterator.hasNext()) {
                curr2 = lbls2Iterator.next();

                if (curr2.userDefined) {
                    break;

                } else {
                    tempBuckets2.add(curr2);
                }
            }

            if (tempBuckets1.size() > 0 || tempBuckets2.size() > 0) {
                AlignmentPiece p = new AlignmentPiece();
                p.isUserDefined = false;
                int start1, start2, end1, end2;
                start1 = tempBuckets1.get(0).getStartIndex();
                start2 = tempBuckets2.get(0).getStartIndex();
                end1 = tempBuckets1.get(tempBuckets1.size() - 1).getEndIndex() + 1;
                end2 = tempBuckets2.get(tempBuckets2.size() - 1).getEndIndex() + 1;
                p.lbls1 = new LinkedList<Label>();
                for (Label l : tempBuckets1) {
                    p.lbls1.add(new Label(l.toString(), l.getStartIndex()
                            - start1, l.getEndIndex() - start1, false));
                }
                p.lbls2 = new LinkedList<Label>();
                for (Label l : tempBuckets2) {
                    p.lbls2.add(new Label(l.toString(), l.getStartIndex()
                            - start2, l.getEndIndex() - start2, false));
                }
                p.seq1 = Arrays.copyOfRange(ls1.getSequence(), start1, end1);
                p.seq2 = Arrays.copyOfRange(ls2.getSequence(), start2, end2);
                result.add(p);
                tempBuckets1.clear();
                tempBuckets2.clear();
            }

            if (curr1.userDefined) { // finished loops because hit a user
                // label
                AlignmentPiece p = new AlignmentPiece();
                p.isUserDefined = true;
                p.labelName = curr1.toString();
                p.seq1 = Arrays.copyOfRange(ls1.getSequence(), curr1
                        .getStartIndex(), curr1.getEndIndex() + 1);
                p.seq2 = Arrays.copyOfRange(ls2.getSequence(), curr2
                        .getStartIndex(), curr2.getEndIndex() + 1);
                result.add(p);
            }
        }

        return result;
    }

    private LabeledAlignment toLabeledAlignment(Alignment a, String labelName) {

        Label l1 = new Label(labelName, 0, a.getSequence1().length - 1, true);
        Label l2 = new Label(labelName, 0, a.getSequence2().length - 1, true);

        List<Label> temp = new LinkedList<Label>();
        temp.add(l1);
        LabeledSequence ls1 = new NucleotideLabeledSequence(a.getSequence1(),
                temp);
        temp.clear();
        temp.add(l2);
        LabeledSequence ls2 = new NucleotideLabeledSequence(a.getSequence2(),
                temp);

        return new LabeledAlignment(ls1, ls2);
    }

    private LabeledAlignment toLabeledAlignment(Alignment a, List<Label> lbls1,
            List<Label> lbls2) {
        LabeledSequence ls1, ls2;
        ls1 = new NucleotideLabeledSequence(a.getSequence1(), lbls1);
        ls2 = new NucleotideLabeledSequence(a.getSequence2(), lbls2);
        return new LabeledAlignment(ls1, ls2);
    }

    public void train(LabeledAlignment la) {
        MultiScoreLearner msl = new BasicMultiScoreLearner();
        msl.train(la);
        for (String labelName : msl.getScoreMatrices().keySet()) {
            GlobalAlignmentAlgorithm gaa = labelToAligner.get(labelName);
            if (gaa == null) {
                gaa = new NeedlemanWunsch(
                        msl.getScoreMatrices().get(labelName), msl
                                .getGapPenaltyFunctions().get(labelName));
                labelToAligner.put(labelName, gaa);
            } else {
                final double WEIGHT = .5;
                gaa.updateScoreMatrix(msl.getScoreMatrices().get(labelName),
                        WEIGHT);
                gaa.updateGapPenaltyFunction(new ConstGapPenalty(), WEIGHT);
            }
        }
    }

    // iterates over each sequence eliminating gaps and adjusting labels
    public LabeledAlignment removeGaps(LabeledAlignment la) {

        // some convenience variables
        char[] s1 = la.getLS1().getSequence();
        char[] s2 = la.getLS2().getSequence();
        List<Label> lbls1 = la.getLS1().getLabels();
        List<Label> lbls2 = la.getLS2().getLabels();

        // result variables
        List<Character> s1_ = new LinkedList<Character>(), s2_ = new LinkedList<Character>();
        List<Label> lbls1_ = new LinkedList<Label>(), lbls2_ = new LinkedList<Label>();

        removeGapsHelper(s1, lbls1, s1_, lbls1_);
        removeGapsHelper(s2, lbls2, s2_, lbls2_);

        return new LabeledAlignment(new NucleotideLabeledSequence(
                toCharArray(s1_), lbls1_), new NucleotideLabeledSequence(
                toCharArray(s2_), lbls2_));
    }

    private void removeGapsHelper(char[] s, List<Label> lbls,
            List<Character> s_, List<Label> lbls_) {
        // keep track of the current label during the loop
        Label currLabel = null;
        Iterator<Label> lblIterator = lbls.iterator();
        if (lblIterator.hasNext()) {
            currLabel = lblIterator.next();
        }
        int lblStart = -1;

        // iterate over sequence removing gaps and adjusting labels
        for (int i = 0; i < s.length; i++) {
            char c = s[i];

            // if we're at the start of a new label, save the start location
            if (currLabel != null && currLabel.getStartIndex() == i)
                lblStart = s_.size();

            // we are ending a label, save it, and move to the next label
            if (currLabel != null && currLabel.getEndIndex() == i) {
                int lblEnd = c == '-' ? s_.size() - 1 : s_.size();
                lbls_.add(new Label(currLabel.toString(), lblStart, lblEnd,
                        currLabel.userDefined));

                if (lblIterator.hasNext())
                    currLabel = lblIterator.next();
                else
                    currLabel = null;
            }

            if (c != '-')
                s_.add(c);
        }
    }

    // indices are inclusive
    private char[] subArray(char[] arr, int start, int end) {
        char[] subArr = new char[end - start + 1];
        for (int i = 0; i < subArr.length; i++) {
            subArr[i] = arr[start + i];
        }
        return subArr;
    }

    private class SubAlignment {
        public String labelName;
        public char[] seq1, seq2;
        public List<Label> containedLabels;

        public SubAlignment(String labelName, char[] seq1, char[] seq2,
                List<Label> containedLabels) {
            if (seq1.length != seq2.length)
                throw new IllegalArgumentException(
                        "'seq1' and 'seq2' must have the same length.");

            this.labelName = labelName;
            this.seq1 = seq1;
            this.seq2 = seq2;
            this.containedLabels = containedLabels;
        }
    }

    private char[] toCharArray(List<Character> globalSeq) {
        char[] charArr = new char[globalSeq.size()];
        int index = 0;
        for (Character c : globalSeq) {
            charArr[index++] = c;
        }
        return charArr;
    }

    private class AlignmentPiece {
        public List<Label> lbls1, lbls2;
        public char[] seq1, seq2;
        public int gapCount;
        public boolean isUserDefined;
        public String labelName;
    }

    private class AlignmentHelper {

        private StringBuffer s1 = new StringBuffer();
        private StringBuffer s2 = new StringBuffer();
        private SortedSet<Label> labels1 = new TreeSet<Label>();
        private SortedSet<Label> labels2 = new TreeSet<Label>();
        private LabeledAlignment la = null;

        public LabeledAlignment toLabeledAlignment() {
            if (la == null) {
                LabeledSequenceImpl ls1 = new LabeledSequenceImpl(s1.toString()
                        .toCharArray());
                for (Label label : labels1) {
                    ls1.addLabel(label);
                }

                LabeledSequenceImpl ls2 = new LabeledSequenceImpl(s2.toString()
                        .toCharArray());
                for (Label label : labels2) {
                    ls2.addLabel(label);
                }
                la = new LabeledAlignment(ls1, ls2);
            }
            return la;
        }

        public void append(LabeledAlignment la) {
            int offset = s1.length();
            s1.append(la.getLS1().getSequence());
            for (Label label : la.getLS1().getLabels()) {
                Label newLabel = new Label(label.toString(), label
                        .getStartIndex()
                        + offset, label.getEndIndex() + offset,
                        label.userDefined);
                labels1.add(newLabel);
            }

            offset = s2.length();
            s2.append(la.getLS2().getSequence());
            for (Label label : la.getLS2().getLabels()) {
                Label newLabel = new Label(label.toString(), label
                        .getStartIndex()
                        + offset, label.getEndIndex() + offset,
                        label.userDefined);
                labels2.add(newLabel);
            }
        }
    }
}
