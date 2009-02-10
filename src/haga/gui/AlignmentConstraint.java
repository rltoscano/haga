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

public class AlignmentConstraint {

	final public Label label1, label2;

	public AlignmentConstraint(Label label1, Label label2) {
		this.label1 = label1;
		this.label2 = label2;
	}

	public boolean firstSeqContains(int index) {
		return index >= label1.getStartIndex() && index <= label1.getEndIndex();
	}

	public boolean secondSeqContains(int index) {
		return index >= label2.getStartIndex() && index <= label2.getEndIndex();
	}
}
