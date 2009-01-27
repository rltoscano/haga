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

public class Alignment {
	private char[] s1;
	private char[] s2;
	
	public Alignment (char[] sequence1, char[] sequence2) {
		if (sequence1.length != sequence2.length)
			throw new IllegalArgumentException("arguments must be of equal length");
		
		this.s1 = sequence1;
		this.s2 = sequence2;
	}
	
	public char[] getSequence1() {
		return s1;
	}
	
	public char[] getSequence2() {
		return s2;
	}
	
	@Override
	public String toString() {
		StringBuffer sb = new StringBuffer();
		for (char c : s1) sb.append(c);
		sb.append("\n");
		for (char c : s2) sb.append(c);
		return sb.toString();
	}
}
