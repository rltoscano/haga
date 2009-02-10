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

import haga.labeler.Label;
import haga.labeler.LabeledAlignment;
import haga.labeler.LabeledSequence;
import haga.labeler.LabeledSequenceImpl;

public class BasicScoreLearner implements ScoreLearner {

	private int[][] scoreMatrix = null;
	private int gapPenalty = 0;
	public static char GAP = '-';

	@Override
	public void train(LabeledAlignment la, String labelName) {
		scoreMatrix = new int[4][4];

		int gapCount = 0;

		LabeledSequence ls1 = la.getLS1();
		LabeledSequence ls2 = la.getLS2();
		if (ls1.getLabels().size() != ls2.getLabels().size()) {
			throw new RuntimeException("ls1 and ls2 are not the same size");
		}

		// This assumes all corresponding labels are the same length
		for (int x = 0; x < ls1.getLabels().size(); x++) {
			Label label1 = ls1.getLabels().get(x);
			Label label2 = ls2.getLabels().get(x);
			if (!label1.toString().equals(label2.toString())) {
				throw new RuntimeException("Labels are not aligned: " + label1
						+ ", " + label2);
			}

			if (!label1.toString().toLowerCase().equals(labelName.toLowerCase())) {
				continue;
			}

			for (int i = 0; i <= label1.getEndIndex() - label1.getStartIndex(); i++) {
				char c1 = ls1.getSequence()[label1.getStartIndex() + i];
				char c2 = ls2.getSequence()[label2.getStartIndex() + i];
				if (c1 == GAP || c2 == GAP) {
					if (c1 == c2) {
						throw new RuntimeException(
								"Alignment contains double gaps (index " + i);
					}

					gapCount++;
				} else {
					scoreMatrix[charToIndex(c1)][charToIndex(c2)]++;
				}
			}
		}

//		printScoreMatrix();

		for (int i = 0; i < scoreMatrix.length; i++) {
			int sum = 0;
			for (int j = 0; j < scoreMatrix[i].length; j++) {
				sum += scoreMatrix[i][j];
			}
			for (int j = 0; j < scoreMatrix[i].length; j++) {
				scoreMatrix[i][j] = scoreMatrix[i][j] * 14 / Math.max(sum, 1);
			}
		}

//		printScoreMatrix();

		for (int i = 0; i < scoreMatrix.length; i++) {
			for (int j = 0; j < i; j++) {
				scoreMatrix[i][j] = scoreMatrix[i][j] + scoreMatrix[j][i];
				scoreMatrix[j][i] = scoreMatrix[i][j];
			}
		}
	}

	public void printScoreMatrix() {
		printScoreMatrix(scoreMatrix);
	}

	public static void printScoreMatrix(int[][] s) {
		for (int i = 0; i < s.length; i++) {
			for (int j = 0; j < s[i].length; j++) {
				System.out.print(s[j][i] + " ");
			}
			System.out.println();
		}
		System.out.println();
	}

	@Override
	public int[][] getScoreMatrix() {
		if (scoreMatrix == null) {
			throw new RuntimeException("Has not been trained yet");
		}

		return scoreMatrix;
	}

	// Finds the gap penalty
	@Override
	public GapPenaltyFunction getGapPenaltyFunction() {
		if (scoreMatrix == null) {
			throw new RuntimeException("Has not been trained yet");
		}

		return new ConstGapPenalty(gapPenalty);
	}

	private static int charToIndex(char c) {
		char cUpper = Character.toUpperCase(c);
		switch (cUpper) {
		case 'A':
			return 0;
		case 'C':
			return 1;
		case 'G':
			return 2;
		case 'T':
			return 3;
		case '-':
		case '_':
			return 4;
		default:
			throw new IllegalArgumentException(
					"'A', 'G', 'C', and 'T' are the only characters supported (case insensitive).");
		}
	}

	public static void main(String args[]) {
		String seq1 = "aaaaaaaggaccccccccccggggggggggttttttttttcgtagctgcgcgttgtagcggcggcctagctcgta";
		String seq2 = "aaaaaggaaaccccccccccggggggggggttttttttttcgcgctgcacgctgagtcgtagctaggctagctag";
		LabeledSequenceImpl ls1 = new LabeledSequenceImpl(seq1.toCharArray());
		LabeledSequenceImpl ls2 = new LabeledSequenceImpl(seq2.toCharArray());

		ls1.addLabel(new Label("User", 0, 10, true));
		ls2.addLabel(new Label("User", 0, 10, true));

		ls1.addLabel(new Label("User", 10, 20, true));
		ls2.addLabel(new Label("User", 10, 20, true));

		ls1.addLabel(new Label("User", 20, 30, true));
		ls2.addLabel(new Label("User", 20, 30, true));

		ls1.addLabel(new Label("exon", 30, 40, true));
		ls2.addLabel(new Label("exon", 30, 40, true));
		ScoreLearner sl = new BasicScoreLearner();
		sl.train(new LabeledAlignment(ls1, ls2), "User");
		int[][] s = sl.getScoreMatrix();
		printScoreMatrix(s);
		System.out.println(sl.getGapPenaltyFunction().calcGapPenalty(null, s));
		System.exit(0);
	}
}
