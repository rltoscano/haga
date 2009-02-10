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

public class NeedlemanWunschIyoToscano extends NeedlemanWunsch {

	public static final int MATCHING_BONUS = 5;

	@Override
	protected int calcScore(char c1, char c2, boolean labelsMatch) {
		/*
		 * return (int) Math .round((super.calcScore(c1, c2, labelsMatch) /
		 * (labelsMatch ? MATCHING_BONUS : 1)));
		 */
		return super.calcScore(c1, c2, labelsMatch)
				- (labelsMatch ? MATCHING_BONUS : 0);
		// return labelsMatch ? -100 : 100;
	}

	@Override
	protected int calcGapPen(boolean labelsMatch) {
		return super.calcGapPen(labelsMatch)
				+ (labelsMatch ? MATCHING_BONUS : 0);
		// return labelsMatch? 50 : -100;
	}
}
