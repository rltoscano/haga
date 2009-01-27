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

package haga.labeler;


public interface Labeler {
	/**
	 * @param sequence the sequence of characters to label
	 * @return a LabeledSequence labeled by this
	 */
	LabeledSequence labelSequence (LabeledSequence sequence);
	
	/**
	 * @param sequence a LabeledSequence to train the Labeler with
	 * @param weight the weight that this training datum has on changing the
	 *   Labeler
	 */
	void train (LabeledSequence sequence, double weight);
}
