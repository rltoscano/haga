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
import haga.labeler.LabeledAlignment;
import haga.labeler.LabeledSequence;
import haga.labeler.LabeledSequenceImpl;

import java.util.ArrayList;
import java.util.List;

/**
 * This is the model behind AlignmentManager.
 */
public class AlignmentModel {

    // private String sequence1, sequence2;
    private List<AlignmentConstraint> constraints;
    private LabeledSequence ls1, ls2;
    private List<AlignmentListener> listeners;

    public AlignmentModel() {
        this("", "");
    }

    public AlignmentModel(String sequence1, String sequence2) {
        this(new LabeledSequenceImpl(sequence1.toCharArray()),
                new LabeledSequenceImpl(sequence2.toCharArray()));
    }

    public AlignmentModel(LabeledSequence ls1, LabeledSequence ls2) {
        this.ls1 = ls1;
        this.ls2 = ls2;
        constraints = new ArrayList<AlignmentConstraint>();
        listeners = new ArrayList<AlignmentListener>();
    }

    public String getFirstSequence() {
        return new String(ls1.getSequence());
    }

    public String getSecondSequence() {
        return new String(ls2.getSequence());
    }

    public LabeledSequence getFirstLabeledSequence() {
        return ls1;
    }

    public LabeledSequence getSecondLabeledSequence() {
        return ls2;
    }

    public LabeledAlignment getLabeledAlignment() {
        return new LabeledAlignment(getFirstLabeledSequence(),
                getSecondLabeledSequence());
    }

    public List<AlignmentConstraint> getConstraints() {
        return constraints;
    }

    // NOTE: this assumes that la consists of two aligned sequences with
    // corresponding labels of equal length (including gaps)
    public boolean updateAlignment(LabeledAlignment la) {
        LabeledSequenceImpl userDefined1 = new LabeledSequenceImpl(la.getLS1()
                .getSequence());
        LabeledSequenceImpl userDefined2 = new LabeledSequenceImpl(la.getLS2()
                .getSequence());

        for (Label label : la.getLS1().getLabels()) {
            if (label.userDefined) {
                userDefined1.addLabel(label);
            }
        }

        for (Label label : la.getLS2().getLabels()) {
            if (label.userDefined) {
                userDefined2.addLabel(label);
            }
        }

        if ((userDefined1.getSequence().length != userDefined2.getSequence().length)
                || (userDefined1.getLabels().size() != userDefined2.getLabels()
                        .size())) {
            return false;
        }

        // Create constraints
        List<AlignmentConstraint> newConstraints = new ArrayList<AlignmentConstraint>();
        for (int i = 0; i < userDefined1.getLabels().size(); i++) {
            Label label1 = userDefined1.getLabels().get(i);
            Label label2 = userDefined2.getLabels().get(i);

            if (!label1.toString().equals(label2.toString())) {
                return false;
            }

            newConstraints.add(new AlignmentConstraint(label1, label2));
        }

        constraints = newConstraints;

        ls1 = la.getLS1();
        ls2 = la.getLS2();

        fireAlignmentUpdated();
        fireConstraintsUpdated();
        return true;
    }

    public boolean addConstraint(AlignmentConstraint constraint) {
        // TODO: optimize this

        int index1 = 0;
        for (Label label : getFirstLabeledSequence().getLabels()) {
            if (constraint.label1.overlaps(label)) {
                return false;
            } else if (constraint.label1.compareTo(label) > 0) {
                index1++;
            }
        }

        int index2 = 0;
        for (Label label : getSecondLabeledSequence().getLabels()) {
            if (constraint.label2.overlaps(label)) {
                return false;
            } else if (constraint.label2.compareTo(label) > 0) {
                index2++;
            }
        }

        if (index1 != index2) {
            return false;
        }

        if (constraints.add(constraint)) {
            // TODO: optimize this
            LabeledSequenceImpl newls1 = new LabeledSequenceImpl(ls1
                    .getSequence());
            for (Label label : ls1.getLabels()) {
                newls1.addLabel(label);
            }
            newls1.addLabel(constraint.label1);
            LabeledSequenceImpl newls2 = new LabeledSequenceImpl(ls2
                    .getSequence());
            for (Label label : ls2.getLabels()) {
                newls2.addLabel(label);
            }
            newls2.addLabel(constraint.label2);

            ls1 = newls1;
            ls2 = newls2;
            fireConstraintsUpdated();
            return true;
        } else {
            return false;
        }
    }

    public boolean removeConstraint(AlignmentConstraint constraint) {
        return (removeConstraint(constraints.indexOf(constraint)) != null);
    }

    public AlignmentConstraint removeConstraint(int index) {
        AlignmentConstraint removed = constraints.remove(index);
        if (removed != null) {
            // TODO: optimize this
            LabeledSequenceImpl newls1 = new LabeledSequenceImpl(ls1
                    .getSequence());
            for (Label label : ls1.getLabels()) {
                if (!label.equals(removed.label1)) {
                    newls1.addLabel(label);
                }
            }
            LabeledSequenceImpl newls2 = new LabeledSequenceImpl(ls2
                    .getSequence());
            for (Label label : ls2.getLabels()) {
                if (!label.equals(removed.label2)) {
                    newls2.addLabel(label);
                }
            }
            
            ls1 = newls1;
            ls2 = newls2;
            
            fireConstraintsUpdated();
        }
        return removed;
    }

    // Returns the score for the local alignment over the interval [start, end]
    public int getScore(int start, int end) {
        if ((start < 0) || (end >= ls1.getSequence().length) || (start > end)) {
            throw new RuntimeException("The interval [" + start + ", " + end
                    + "] is invalid.");
        }

        // TODO: return a more accurate score
        int score = 0;
        for (int i = start; i <= end; i++) {
            if (ls1.getSequence()[i] == ls2.getSequence()[i]) {
                score++;
            }
        }

        return score;
    }

    // Returns the maximum possible score for a local alignment of length n
    public int getMaxScore(int n) {
        // TODO: return a more accurate score
        return n;
    }

    public boolean addAlignmentListener(AlignmentListener listener) {
        return listeners.add(listener);
    }

    public boolean removeAlignmentListener(AlignmentListener listener) {
        return listeners.remove(listener);
    }

    private void fireAlignmentUpdated() {
        for (AlignmentListener listener : listeners) {
            listener.alignmentUpdated();
        }
    }

    private void fireConstraintsUpdated() {
        for (AlignmentListener listener : listeners) {
            listener.constraintsUpdated();
        }
    }
}
