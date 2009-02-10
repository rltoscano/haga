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

package haga.labeler;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class LabeledSequenceImpl implements LabeledSequence {

	private List<Label> labels;
	private char[] sequence;

	public LabeledSequenceImpl(char[] sequence) {
		this.sequence = sequence;
		labels = new ArrayList<Label>();
	}

	@SuppressWarnings("unchecked")
	public boolean addLabel(Label label) {
		if (label.getStartIndex() < 0 || label.getEndIndex() >= sequence.length) {
			throw new IllegalArgumentException("Label out of bounds: " + label + " ("
					+ label.getStartIndex() + ":" + label.getEndIndex() + ")");
		}

		for (Label l : labels) {
			if (l.overlaps(label)) {
				return false;
			}
		}

		if (labels.add(label)) {
			Collections.sort(labels);
			return true;
		}
		return false;
	}

	public boolean removeLabel(Label label) {
		return labels.remove(label);
	}

	@Override
	public List<Label> getLabels() {
		return labels;
	}

	@Override
	public char[] getSequence() {
		return sequence;
	}
}
