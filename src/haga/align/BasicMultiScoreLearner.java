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

import java.util.HashMap;
import java.util.Map;

public class BasicMultiScoreLearner implements MultiScoreLearner {

	private Map<String, int[][]> scoreMatrices = null;
	private Map<String, GapPenaltyFunction> penaltyFunctions = null;

	@Override
	public void train(LabeledAlignment la) {
		scoreMatrices = new HashMap<String, int[][]>();
		penaltyFunctions = new HashMap<String, GapPenaltyFunction>();

		LabeledSequence ls1 = la.getLS1();
		LabeledSequence ls2 = la.getLS2();
		if (ls1.getLabels().size() != ls2.getLabels().size()) {
			throw new RuntimeException("ls1 and ls2 are not the same size");
		}

		for (int x = 0; x < ls1.getLabels().size(); x++) {
			Label label1 = ls1.getLabels().get(x);
			Label label2 = ls2.getLabels().get(x);
			String labelName = label1.toString();
			if (!labelName.equals(label2.toString())) {
				throw new RuntimeException("Labels are not aligned: " + label1
						+ ", " + label2);
			}

			if (scoreMatrices.get(labelName) == null) {
				ScoreLearner sl = new BasicScoreLearner();
				sl.train(la, labelName);
				// This assumes all corresponding labels are the same length
				scoreMatrices.put(labelName, sl.getScoreMatrix());
				penaltyFunctions.put(labelName, sl.getGapPenaltyFunction());
			}
		}
	}

	@Override
	public Map<String, GapPenaltyFunction> getGapPenaltyFunctions() {
		if (penaltyFunctions == null) {
			throw new RuntimeException("Has not been trained yet");
		}

		return penaltyFunctions;
	}

	@Override
	public Map<String, int[][]> getScoreMatrices() {
		if (scoreMatrices == null) {
			throw new RuntimeException("Has not been trained yet");
		}

		return scoreMatrices;
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

		ls1.addLabel(new Label("Exon", 30, 40, true));
		ls2.addLabel(new Label("Exon", 30, 40, true));

		ls1.addLabel(new Label("User", 40, 75, true));
		ls2.addLabel(new Label("User", 40, 75, true));
		MultiScoreLearner msl = new BasicMultiScoreLearner();
		msl.train(new LabeledAlignment(ls1, ls2));
		BasicScoreLearner.printScoreMatrix(msl.getScoreMatrices().get("user"));
		BasicScoreLearner.printScoreMatrix(msl.getScoreMatrices().get("exon"));
		System.exit(0);
	}
}
