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

import haga.labeler.LabeledAlignment;
import haga.labeler.LabeledSequence;

import java.io.File;

public interface AlignmentManagerListener {

	public void xmlHmmFileLoaded(File xmlFile);

	public void trainLabelerPressed(LabeledSequence s1, LabeledSequence s2);

	public void trainAlignerPressed(LabeledAlignment la);

	public void alignSequencesPressed(LabeledSequence s1, LabeledSequence s2);
	
	public void labelSequencesPressed(LabeledSequence s1, LabeledSequence s2);
}
