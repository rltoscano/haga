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

package haga;

import haga.labeler.Label;
import haga.labeler.LabeledSequence;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.Random;

public class HagaUtils {
	public static char[] generateNucleotideSequence(int length) {
		char[] seq = new char[length];
		char[] nucleotides = new char[] { 'A', 'C', 'G', 'T' };

		Random rand = new Random();

		for (int i = 0; i < length; i++) {
			seq[i] = nucleotides[rand.nextInt(4)];
		}

		return seq;
	}

	public static char[] getHumanHox() {
		StringBuffer str1 = new StringBuffer();

		try {
			File f = new File(
					"/home/rtoscano/docs/mit/6.878/problemSets/ps1/ps1-code/human_HoxA13.fa");
			BufferedReader br = new BufferedReader(new FileReader(f));

			while (br.ready()) {
				String line = br.readLine();
				if (line.startsWith(">"))
					continue;
				str1.append(line);
			}

			br.close();

		} catch (Exception e) {
			e.printStackTrace();
		}

		return str1.toString().toCharArray();
	}

	public static char[] getMouseHox() {
		StringBuffer str1 = new StringBuffer();

		try {
			File f = new File(
					"/home/rtoscano/docs/mit/6.878/problemSets/ps1/ps1-code/mouse_HoxA13.fa");
			BufferedReader br = new BufferedReader(new FileReader(f));

			while (br.ready()) {
				String line = br.readLine();
				if (line.startsWith(">"))
					continue;
				str1.append(line);
			}

			br.close();

		} catch (Exception e) {
			e.printStackTrace();
		}

		return str1.toString().toCharArray();
	}

	public static void printLabeledSequence(LabeledSequence ls) {
		int lastIndex = 0;
		for (Label l : ls.getLabels()) {
			for (int i = lastIndex; i < l.getStartIndex(); i++)
//				if (i%500 == 0) System.out.println();
				System.out.print(" ");
			for (int i = l.getStartIndex(); i < l.getEndIndex() + 1; i++) {
//				if (i%500 == 0) System.out.println();
				if (l.userDefined) {
					System.out.print(l.toString().toUpperCase().charAt(0));
				} else {
					System.out.print(l.toString().toLowerCase().charAt(0));
				}
			}
			lastIndex = l.getEndIndex() + 1;
		}
		System.out.println();

		int i=0;
		for (char c : ls.getSequence()) {
			i++;
//			if (i%500 == 0) System.out.println();
			System.out.print(c);
		}
		System.out.println();
	}
}
