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

import haga.labeler.Label;
import haga.labeler.LabeledSequence;

import java.util.List;
import java.util.LinkedList;

public class NucleotideLabeledSequence implements LabeledSequence {
    private char[] sequence;
    private List<Label> labels;

    /**
     * @param sequence - is not copied
     * @param labels - is not copied
     */
    public NucleotideLabeledSequence (char[] sequence, List<Label> labels) {
        this.sequence = sequence;
        this.labels = labels;
    }

    /**
     * Like the other constructor except that this one applies the input label
     * to the entire sequence.
     * @param sequence - is not copied
     * @param labels - is not copied
     */
    public NucleotideLabeledSequence (char[] sequence, Label label) {
        this.sequence = sequence;
        this.labels = new LinkedList<Label>();
        this.labels.add(label);
    }

    /**
     * @returns not a copy 
     */
    public List<Label> getLabels() {
        return labels;
    }

    /**
     * @returns not a copy
     */
    public char[] getSequence() {
        return sequence;
    }
}
