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

import haga.labeler.Label;
import haga.labeler.LabeledSequence;

import java.util.Iterator;

public class NeedlemanWunsch implements GlobalAlignmentAlgorithm {

	private int[][] alignmentMatrix;
	private GapPenaltyFunction gpf;
	private int[][] scoreMatrix;

	public NeedlemanWunsch(int[][] scoreMatrix,
			GapPenaltyFunction gapPenaltyFunc) {
		if (gapPenaltyFunc == null)
			throw new IllegalArgumentException(
					"'gapPenaltyFunc' cannot be null");

		gpf = gapPenaltyFunc;
		setScoreMatrix(scoreMatrix);
	}

	public NeedlemanWunsch() {
		gpf = new ConstGapPenalty();
		setScoreMatrix(new int[][] { { 0, 4, 5, 5 }, { 4, 0, 5, 5 },
				{ 5, 5, 0, 4 }, { 5, 5, 4, 0 } });
	}

	public void setScoreMatrix(int[][] newScoreMatrix) {
		if (newScoreMatrix == null)
			throw new IllegalArgumentException(
					"'newScoreMatrix' cannot be null");

		this.scoreMatrix = newScoreMatrix;
	}

	private enum Pointer {
		UP, LEFT, DIAG
	}

	@Override
	public Alignment align(LabeledSequence ls1, LabeledSequence ls2) {

		Pointer[][] tb = new Pointer[ls1.getSequence().length + 1][ls2
				.getSequence().length + 1];

		char[] str1 = ls1.getSequence();
		char[] str2 = ls2.getSequence();
		Iterator<Label> iterator1 = ls1.getLabels().iterator();
		Label label1 = iterator1.hasNext() ? iterator1.next() : null;
		// initialize alignmentMatrix
		alignmentMatrix = new int[str1.length + 1][str2.length + 1];
		for (int i = 1; i < alignmentMatrix.length; i++) {
			alignmentMatrix[i][0] = -i * calcGapPen(false);
		}
		for (int i = 1; i < alignmentMatrix[0].length; i++) {
			alignmentMatrix[0][i] = -i * calcGapPen(false);
		}

		for (int i = 0; i < tb.length; i++) {
			tb[i][0] = Pointer.UP;
		}
		
		for (int i = 0; i < tb[0].length; i++) {
			tb[0][i] = Pointer.LEFT;
		}
		
		// fill in the matrix
		for (int i = 1; i < alignmentMatrix.length; i++) {
			Iterator<Label> iterator2 = ls2.getLabels().iterator();
			Label label2 = iterator2.hasNext() ? iterator2.next() : null;
			for (int j = 1; j < alignmentMatrix[0].length; j++) {
				boolean labelsMatch = (label1 == null || label2 == null) ? true
						: label1.toString().equals(label2.toString());
				
				int matchScore = alignmentMatrix[i - 1][j - 1]
						+ calcScore(str1[i - 1], str2[j - 1], labelsMatch);
				int gap1Score = alignmentMatrix[i][j - 1]
						- calcGapPen(labelsMatch);
				int gap2Score = alignmentMatrix[i - 1][j]
						- calcGapPen(labelsMatch);

				alignmentMatrix[i][j] = Math.min(
						Math.min(gap1Score, gap2Score), matchScore);

				if (alignmentMatrix[i][j] == matchScore) {
					tb[i][j] = Pointer.DIAG;
				} else if (alignmentMatrix[i][j] == gap1Score) {
					tb[i][j] = Pointer.LEFT;
				} else {
					tb[i][j] = Pointer.UP;
				}

				if (label2 != null && j - 1 == label2.getEndIndex() 
						&& iterator2.hasNext()) {
					label2 = iterator2.next();
				}
			}
			if (label1 != null && i - 1 == label1.getEndIndex()
					&& iterator1.hasNext()) {
				label1 = iterator1.next();
			}
		}

		// System.out.println(alignmentMatrix[alignmentMatrix.length-1][alignmentMatrix[0].length-1]);

		/*
		 * for (int i=0; i<alignmentMatrix.length; i++) { for (int j=0; j<alignmentMatrix[0].length;
		 * j++) { System.out.print(alignmentMatrix[i][j]+" "); }
		 * 
		 * System.out.print('\n'); }
		 */

		// calculate best path through alignmentMatrix
		int i = alignmentMatrix.length - 1;
		int j = alignmentMatrix[0].length - 1;
		StringBuffer strbuf1 = new StringBuffer();
		StringBuffer strbuf2 = new StringBuffer();

		while (i > 0 || j > 0) {
			if (i == 0) {
				strbuf1.append('-');
				strbuf2.append(str2[j - 1]);
				j--;
				continue;
			} else if (j == 0) {
				strbuf1.append(str1[i - 1]);
				strbuf2.append('-');
				i--;
				continue;
			}
//
//			int topScore = alignmentMatrix[i - 1][j];
//			int leftScore = alignmentMatrix[i][j - 1];
			// int diagScore = alignmentMatrix[i - 1][j - 1];
			//
			// boolean top = false, diag = false;

//			int score = alignmentMatrix[i][j];
			switch (tb[i][j]) {
			case UP:
				strbuf1.append(str1[i - 1]);
				strbuf2.append('-');
				i--;
				break;
			case LEFT:
				strbuf1.append('-');
				strbuf2.append(str2[j - 1]);
				j--;
				break;
			default:
				strbuf1.append(str1[i - 1]);
				strbuf2.append(str2[j - 1]);
				i--;
				j--;
				break;
			}
//			if (leftScore - gpf.calcGapPenalty(alignmentMatrix, scoreMatrix) == score) {
//				strbuf1.append('-');
//				strbuf2.append(str2[j - 1]);
//				j--;
//			} else if (topScore
//					- gpf.calcGapPenalty(alignmentMatrix, scoreMatrix) == score) {
//				strbuf1.append(str1[i - 1]);
//				strbuf2.append('-');
//				i--;
//			} else {
//				strbuf1.append(str1[i - 1]);
//				strbuf2.append(str2[j - 1]);
//				i--;
//				j--;
//			}
		}

		int len = strbuf1.length();

		char[] result1 = new char[strbuf1.length()];
		for (int index = 0; index < strbuf1.length(); index++)
			result1[len - index - 1] = strbuf1.charAt(index);

		char[] result2 = new char[strbuf2.length()];
		i = result2.length - 1;
		for (int index = 0; index < strbuf2.length(); index++)
			result2[len - index - 1] = strbuf2.charAt(index);

		return new Alignment(result1, result2);
	}

	@Override
	public void updateGapPenaltyFunction(GapPenaltyFunction gpf, double weight) {
		this.gpf = this.gpf.merge(gpf, weight);
	}

	@Override
	public void updateScoreMatrix(int[][] newScoreMatrix, double weight) {
		if (weight < 0 || weight > 1) {
			throw new IllegalArgumentException(
					"weight must be between 0 and 1 (was " + weight + ")");
		}
		if (newScoreMatrix.length != scoreMatrix.length) {
			throw new IllegalArgumentException(
					"Score matrices have different sizes");
		}
		if (scoreMatrix.length == 0) {
			return;
		}

		if (newScoreMatrix[0].length != scoreMatrix[0].length) {
			throw new IllegalArgumentException(
					"Score matrices have different sizes");
		}

		for (int i = 0; i < scoreMatrix.length; i++) {
			for (int j = 0; j < scoreMatrix[i].length; j++) {
				scoreMatrix[i][j] = (int) Math.round(weight
						* newScoreMatrix[i][j] + (1 - weight)
						* scoreMatrix[i][j]);
			}
		}
	}

	protected int calcScore(char c1, char c2, boolean labelsMatch) {
		return scoreMatrix[getIndex(c1)][getIndex(c2)];
	}

	protected int calcGapPen(boolean labelsMatch) {
		return gpf.calcGapPenalty(alignmentMatrix, scoreMatrix);
	}

	private int getIndex(char c) {
		char cUpper = Character.toUpperCase(c);

		switch (cUpper) {
		case 'A':
			return 0;
		case 'G':
			return 1;
		case 'C':
			return 2;
		case 'T':
			return 3;
		default:
			throw new IllegalArgumentException(
					"'A', 'G', 'C', and 'T' are the only characters supported (case insensitive).");
		}
	}
}
