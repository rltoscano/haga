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

package haga.testing;

import haga.*;
import haga.labeler.HMMLabeler;
import haga.labeler.Label;
import haga.labeler.LabeledSequence;
import haga.labeler.Labeler;

import java.util.*;

public class LabelerTest {

	/**
	 * @param args
	 */
	public static void main(String[] args) {		
		// create a sequence
		char[] sequence = 
			("ACGTTTGTCACACGTAGCAGCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCC" +
					"ACGTAGCTAGTCGATGCTAGGTAGCTAGCTATATTAGCTATCATCATGCTAGCTA" +
					"GTCGATTAGTCGCGGCGCGGCGCGGGGGTTATTATATATTATTTATATGCGCATG" +
					"CTACTAGCTAGCTATTACGTACTCGGCTAGTATATTAAATAGCTAGTCTGAGTAC")
					.toCharArray(); 
		
		List<Label> labels = new ArrayList<Label>();
		labels.add(new Label("Exon", 0, 20, true));
		labels.add(new Label("Intron", 21, 40, true));
		labels.add(new Label("Exon", 41, sequence.length-1, true));
		LabeledSequence given = new NucleotideLabeledSequence(sequence, labels);
		
		// print given labels
		System.out.println("Given Labels:");
		printLabeledSequence(given);
		
		// possible label names
		List<String> possibleLabelNames = new ArrayList<String>(2);
		possibleLabelNames.add("Exon");
		possibleLabelNames.add("Intron");
		
		Labeler labeler = new HMMLabeler(2, 
										 new double[]{.5, .5},
										 new double[][]{{.5, .5},
													    {.5, .5}},
										 new double[][]{{.30, .20, .30, .20},
													   	{.25, .25, .25, .25}},
										 possibleLabelNames);
		
		LabeledSequence original = new NucleotideLabeledSequence(sequence, new LinkedList<Label>());
		
		// label it
		LabeledSequence ls = labeler.labelSequence(original);
		// print labels
		System.out.println("Calculated Labels w/o training:");
		printLabeledSequence(ls);
		
		// train it
		labeler.train(given, 0);		
			
		// label it again
		ls = labeler.labelSequence(original);
		System.out.println("Calculated Labels with training:");
		printLabeledSequence(ls);
		
		
		// print sequence
		String str = new String(sequence);
		System.out.println(str);
	}
	
	private static void printLabeledSequence(LabeledSequence seq) {
		int labelIndex = 0;
		for (int i=0;
	 	 	 i<seq.getSequence().length && labelIndex<seq.getLabels().size();
	 	 	 i++) {
			if (i == seq.getLabels().get(labelIndex).getStartIndex()) {
				System.out.print(seq.getLabels().get(labelIndex).toString().charAt(0));
				labelIndex++;
			} else
				System.out.print(" ");
		}
		System.out.print("\n");
	}

}
