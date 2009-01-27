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

import java.util.List;

/**
 * A LabeledSequence is a sequence of characters coupled with a list of labels.
 * The labels in the list should be non-overlapping and in sequential order. The
 * list can have any number of labels in it as long as there is at least one
 * and the previous conditions are met.
 */
public interface LabeledSequence {

    /**
     * @returns the list of labels for this LabeledSequence.
     */
    List<Label> getLabels();

    /**
     * @returns the character sequence for this LabeledSequence.
     */
    char[] getSequence();
}
