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

public class ConstGapPenalty implements GapPenaltyFunction {

	private int gapPenalty;

	public ConstGapPenalty() {
		this(-7);
	}

	public ConstGapPenalty(int gapPenalty) {
		this.gapPenalty = gapPenalty;
	}

	public int calcGapPenalty(int[][] dynProgMatrix, int[][] scoreMatrix) {
		return gapPenalty;
	}

	@Override
	public GapPenaltyFunction merge(GapPenaltyFunction other, double otherWeight) {
		if (other != null && other.getClass() == ConstGapPenalty.class) {
			return new ConstGapPenalty((int) Math.round(otherWeight
					* ((ConstGapPenalty) other).gapPenalty + (1 - otherWeight)
					* gapPenalty));
		} else {
			return this;
		}
	}
}
