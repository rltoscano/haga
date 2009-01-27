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

package haga.align;

import haga.labeler.LabeledSequence;

public interface GlobalAlignmentAlgorithm {
	Alignment align(LabeledSequence ls1, LabeledSequence ls2);

	// Update the score matrix. weight specifies weight for the new matrix
	public void updateScoreMatrix(int[][] newScoreMatrix, double weight);

	// Update the gap penalty function. weight specifies weight for the function
	public void updateGapPenaltyFunction(GapPenaltyFunction gpf, double weight);
}
