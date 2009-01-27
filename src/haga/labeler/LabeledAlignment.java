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

/**
 * A LabeledAlignment is a pair of LabeledSequences: LS1 and LS2.
 */
public class LabeledAlignment {
    private LabeledSequence ls1;
    private LabeledSequence ls2;

    /**
     * @param ls1 not copied, stored as LS1
     * @param ls2 not copied, stored as LS2
     */
    public LabeledAlignment (LabeledSequence ls1, LabeledSequence ls2) {
        this.ls1 = ls1;
        this.ls2 = ls2;
    }

    /**
     * @returns LS1
     */
    public LabeledSequence getLS1() {
        return ls1;
    }

    /**
     * @returns LS2
     */
    public LabeledSequence getLS2() {
        return ls2;
    }
}
