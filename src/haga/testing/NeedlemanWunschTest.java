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

package haga.testing;

import haga.align.ConstGapPenalty;
import haga.align.GlobalAlignmentAlgorithm;
import haga.align.NeedlemanWunsch;
import haga.labeler.LabeledSequenceImpl;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;

public class NeedlemanWunschTest {
	public static void main(String args[]) {
		GlobalAlignmentAlgorithm alg = new NeedlemanWunsch(
				new int[][] { { 0, 4, 5, 5 }, { 4, 0, 5, 5 }, { 5, 5, 0, 4 },
						{ 5, 5, 4, 0 } }, new ConstGapPenalty());
		// System.out.println(alg.align("aggctttgctaa", "agatcaagctctac"));

		// read in hox gene
		try {
			File f = new File(
					"/home/rtoscano/docs/mit/6.878/problemSets/ps1/ps1-code/human_HoxA13.fa");
			BufferedReader br = new BufferedReader(new FileReader(f));

			StringBuffer str1 = new StringBuffer();

			while (br.ready()) {
				String line = br.readLine();
				if (line.startsWith(">")) {
					continue;
				}

				str1.append(line);
			}
			br.close();

			StringBuffer str2 = new StringBuffer();
			f = new File(
					"/home/rtoscano/docs/mit/6.878/problemSets/ps1/ps1-code/mouse_HoxA13.fa");
			br = new BufferedReader(new FileReader(f));

			while (br.ready()) {
				String line = br.readLine();
				if (line.startsWith(">")) {
					continue;
				}

				str2.append(line);
			}
			br.close();

			// System.out.println(str1.toString());
			// System.out.println(str2.toString());

			System.out.println(alg.align(new LabeledSequenceImpl(str1
					.toString().toCharArray()), new LabeledSequenceImpl(str2
					.toString().toCharArray())));
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
