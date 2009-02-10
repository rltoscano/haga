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

public interface GapPenaltyFunction {
	/**
	 * Calculates the gap penalty for an alignment
	 * @param dynProgMatrix the dynamic programming matrix calculated so far
	 * @param scoreMatrix the score matrix for the alignment
	 * @return a negative value that represents the gap penalty
	 */
	int calcGapPenalty (int[][] dynProgMatrix, int[][] scoreMatrix);
	
	GapPenaltyFunction merge(GapPenaltyFunction other, double otherWeight);
}
