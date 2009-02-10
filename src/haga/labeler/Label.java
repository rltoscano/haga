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

public class Label implements Comparable {

	// startIndex and endIndex are both INCLUSIVE
	private int startIndex, endIndex;
	private String name;
	public boolean userDefined;

	public Label(String name, int startIndex, int endIndex, boolean userDefined) {
		this.name = name.toLowerCase();
		this.startIndex = startIndex;
		this.endIndex = endIndex;
		this.userDefined = userDefined;
	}

	public int getEndIndex() {
		return endIndex;
	}

	public int getStartIndex() {
		return startIndex;
	}

	@Override
	public String toString() {
		return name;
	}

	public boolean overlaps(Label label) {
		return ((getStartIndex() >= label.getStartIndex()) && (getStartIndex() <= label
				.getEndIndex()))
				|| ((getEndIndex() >= label.getStartIndex()) && (getEndIndex() <= label
						.getEndIndex()));
	}

	/**
	 * NOTE: label1.compareTo(label2) == 0 does NOT imply label1.equals(label2)
	 */
	@Override
	public int compareTo(Object o) {
		if (o == null || o.getClass() != Label.class) {
			throw new ClassCastException(o + " could not be cast to "
					+ Label.class);
		}

		Label label = (Label) o;

		if (overlaps(label)) {
			return (getEndIndex() + getStartIndex())
					- (label.getEndIndex() + label.getStartIndex());
		}

		return getStartIndex() - label.getStartIndex();
	}

	@Override
	public boolean equals(Object o) {
		if (o == null || o.getClass() != Label.class) {
			return false;
		} else {
			Label label = (Label) o;
			return ((getStartIndex() == label.getStartIndex())
					&& (getEndIndex() == label.getEndIndex())
					&& (name.equals(label.name)) && (userDefined == label.userDefined));
		}
	}

	@Override
	public int hashCode() {
		return (userDefined ? 0 : 1361) + 11
				* (startIndex + 29 * (endIndex + 613 * name.hashCode()));
	}
}
