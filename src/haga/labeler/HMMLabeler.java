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

import haga.NucleotideLabeledSequence;

import java.util.*;

import be.ac.ulg.montefiore.run.jahmm.*;

public class HMMLabeler implements Labeler {

	private List<String> labelNames = new ArrayList<String>();  
	public Hmm<ObservationInteger> hmm;
	private static final int NB_CHARS = 4;
	
	public HMMLabeler (int nbStates, 
					   double[] initialProb, 
					   double[][] transProb, 
					   double[][] initialDist, 
					   List<String> labelNames) {
		
		if (initialDist.length != nbStates)
			throw new RuntimeException("length of 'initialDist' must be same as 'nbStates'");
		if (nbStates != labelNames.size())
			throw new RuntimeException("length of 'labelNames' must be same as 'nbStates'");
		
		List<OpdfInteger> opdfs = new ArrayList<OpdfInteger>(nbStates);
		for (double[] dist : initialDist) {
			opdfs.add(new OpdfInteger(dist));
		}
				
		hmm = new Hmm<ObservationInteger>(
				initialProb,
				transProb,
				opdfs);
		
		// add labels to the labelNames map
		for (String name : labelNames)
			this.labelNames.add(name.toLowerCase());
	}
	
	public LabeledSequence labelSequence(LabeledSequence sequence) {
		// result varialbes
		List<Label> lbls = new LinkedList<Label>();
		
		// create labels for every piece of unlabeled data
		int start = 0;
		for (Label l : sequence.getLabels()) {
			char[] subSequence =
				Arrays.copyOfRange(sequence.getSequence(),
								   start,
								   l.getStartIndex());
			if (subSequence.length > 0) {
				LabeledSequence ls = labelSequenceHelper(subSequence);
				for (Label subLabel : ls.getLabels()) {
					lbls.add(new Label(subLabel.toString(), subLabel
							.getStartIndex()
							+ start, subLabel.getEndIndex() + start,
							subLabel.userDefined));
				}
			}
			start = l.getEndIndex()+1;
			
			// add the user defined label
			lbls.add(l);
		}
		
		// get the last background piece
		if (start != sequence.getSequence().length) {
			char[] subSequence =
				Arrays.copyOfRange(sequence.getSequence(),
								   start,
								   sequence.getSequence().length);
			LabeledSequence ls = labelSequenceHelper(subSequence);
			for (Label subLabel : ls.getLabels()) {
				lbls.add(new Label(subLabel.toString(), 
								   subLabel.getStartIndex()+start,
								   subLabel.getEndIndex()+start, subLabel.userDefined));
			}
		}
		
		
		return new NucleotideLabeledSequence(sequence.getSequence(), lbls);
	}
	
	private LabeledSequence labelSequenceHelper (char[] sequence) {
//		 convert sequence into a List of ObservationIntegers
		List<ObservationInteger> s = new ArrayList<ObservationInteger>(sequence.length);
		for (char c : sequence)
			s.add(nucleotideCharToObservationInteger(c));
		
		// print observation sequence
		/*System.out.println("Observation Sequence:");
		for (ObservationInteger o : s) System.out.print(o.value);
		*/
		
		// label the sequence
		int[] intLabels = hmm.mostLikelyStateSequence(s);
		
		// print out the labels
		/*System.out.println("Labels:");
		for (int i : intLabels) System.out.print(i);
		System.out.println();*/
		
		// convert the int label sequence into a LabeledSequence
		List<Label> labels = new ArrayList<Label>();
		int prevLabel = -1;
		int startIndex = 0;
		for (int i=0; i<intLabels.length; i++) {
			if (intLabels[i] != prevLabel) { 
				if (prevLabel != -1) { // first iteration					
					labels.add(new Label(labelNames.get(prevLabel), startIndex, i-1, false));
				}
					
				// mark start of new label
				startIndex = i;
					
				prevLabel = intLabels[i];
			}		
		}
		
		// add last label
		labels.add(new Label(labelNames.get(prevLabel), startIndex, intLabels.length-1, false));
		
		//System.out.println(labels.get(0).toChar());

		return new NucleotideLabeledSequence(sequence, labels);
	}

	public void train(LabeledSequence labeledSequence, double weight) {
		// first check if we have any labels to learn from
		if (labeledSequence.getLabels().size() <= 0) return;
		
		// step 1 - calculate given probabilities from labeled sequence
		// 1.a count frequencies of transition types
		int[][] transitionFrequencies = new int[hmm.nbStates()][hmm.nbStates()];
		
		Label prevLabel = null;
		for (Label l : labeledSequence.getLabels()) {
			int labelLength = l.getEndIndex() - l.getStartIndex() + 1;
			int labelIndex = labelNames.indexOf(l.toString());
			
			if (labelIndex == -1) { // we've never seen this label before
				addNewLabel(l.toString());
				labelIndex = labelNames.indexOf(l.toString());
				transitionFrequencies = grow2DArray(transitionFrequencies);
			}
			
			transitionFrequencies[labelIndex][labelIndex] += labelLength;
			
			if (prevLabel != null) {
				int prevLabelIndex = labelNames.indexOf(prevLabel.toString());
				transitionFrequencies[prevLabelIndex][labelIndex]++;
			}
			
			prevLabel = l;
		}
		
		// 1.b calculate probabilities of transition types
		double[][] transitionProbabilities = new double[hmm.nbStates()][hmm.nbStates()];
		double nbTransitions = (double)labeledSequence.getSequence().length-1;
		for (int i=0; i<hmm.nbStates(); i++) {
			for (int j=0; j<hmm.nbStates(); j++) {				
				transitionProbabilities[i][j] =
					// # of transitions of this type / 
					// # of possible transitions
					(double)transitionFrequencies[i][j] / 
					nbTransitions; 
			}
		}
		
		System.out.println("Transition frequencies.");
		print2DArray(transitionFrequencies);
		System.out.println("Total # of transitions: "+nbTransitions);
		System.out.println("Transition Probabilities.");
		print2DArray(transitionProbabilities);
		
		// 1.c count frequencies of characters in each state
		int[][] charFrequencies = new int[hmm.nbStates()][NB_CHARS];
		char[] seq = labeledSequence.getSequence();
		int labelIndex = 0;
		Label currLabel = labeledSequence.getLabels().get(labelIndex);
		for (int i=0; i<seq.length; i++) {
			charFrequencies[labelNames.indexOf(currLabel.toString())][nucleotideCharToInt(seq[i])]++;
			if (currLabel.getEndIndex() == i && i < seq.length-1) { // get next label
				labelIndex++;
				if (labelIndex == labeledSequence.getLabels().size()) break;
				currLabel = labeledSequence.getLabels().get(labelIndex);
			}
		}
		
		// 1.d calc probabilities of characters in each state
		int[] stateTotals = new int[hmm.nbStates()];
		for (int i=0; i<hmm.nbStates(); i++) {
			int sum = 0;
			for (int j=0; j<NB_CHARS; j++) {
				sum += charFrequencies[i][j];
			}
			stateTotals[i] = sum;
		}
		
		double[][] charProbabilities = new double[hmm.nbStates()][NB_CHARS];
		for (int i=0; i<hmm.nbStates(); i++) {
			if (stateTotals[i] > 0) {
				for (int j=0; j<NB_CHARS; j++) {
					charProbabilities[i][j] =
						(double)charFrequencies[i][j] /
						(double)stateTotals[i];
				}
				
			} else { // if there is no data, keep original value
				for (int j=0; j<NB_CHARS; j++) {
					charProbabilities[i][j] = hmm.getOpdf(i).probability(new ObservationInteger(j));
				}
			}
		}
		
		// step 2 - merge given probabilities with existing HMM probabilities			
		double[][] newTransitionProb = new double[hmm.nbStates()][hmm.nbStates()];
		double[][] newHiddenDist = new double[hmm.nbStates()][NB_CHARS];
		
		for (int i=0; i<hmm.nbStates(); i++) {
			Opdf<ObservationInteger> intDist = hmm.getOpdf(i);
			
			for (int j=0; j<hmm.nbStates(); j++) {
				if (hmm.getAij(i, j) < 0)
					System.out.println("HMM LESS THAN 0, THEIR BAD");
				if (transitionProbabilities[i][j] < 0)
					System.out.println("TPROB LESS THAN 0, MY BAD");
					
				// merge transition probabilities
				newTransitionProb[i][j] =  
						(1-weight)*hmm.getAij(i, j) +
					    weight*transitionProbabilities[i][j];
			}
			
			for (int j=0; j<NB_CHARS; j++) {
				// merge hidden character pdfs
				newHiddenDist[i][j] = 
						(1-weight*intDist.probability(new ObservationInteger(j))) +
						(weight*charProbabilities[i][j]);
			}
		}
		
		for (double[] dArr : newTransitionProb) {
			normalize(dArr);
		}
		
		for (double[] dArr : newHiddenDist) {
			normalize(dArr);
		}
		
		System.out.println("newTransitionProb:");
		print2DArray(newTransitionProb);
		System.out.println("newHiddenDist:");
		print2DArray(newHiddenDist);
		
		List<OpdfInteger> opdfs = new ArrayList<OpdfInteger>(hmm.nbStates());
		for (double[] dist : newHiddenDist) {
			opdfs.add(new OpdfInteger(dist));
		}
		
		double[] initprob = new double[hmm.nbStates()];
		for (int i=0; i<initprob.length; i++) {
			initprob[i] = 1/hmm.nbStates();
		}
		
		hmm = new Hmm<ObservationInteger>(
				initprob,
				newTransitionProb,
				opdfs);
		
	}
	
	// assumes that arr is rectangluar, but I think it can only be rectangular
	private int[][] grow2DArray (int[][] arr) {
		int[][] result = new int[arr.length+1][arr[0].length+1];
		
		for (int i=0; i<arr.length; i++) {
			for (int j=0; j<arr[0].length; j++) {
				result[i][j] = arr[i][j];
			}
		}
		
		return result;
	}

	// replaces our old hmm with one that supports the new label
	private void addNewLabel(String labelName) {
		// add the label names to the list
		labelNames.add(labelName);
		
		// calculate new initial probabilities
		double[] initprob = new double[hmm.nbStates()+1];
		double avgprob = 0;
		for (int i=0; i<hmm.nbStates(); i++) {
			initprob[i] = hmm.getPi(i);
			avgprob += hmm.getPi(i);
		}
		initprob[initprob.length-1] = avgprob/hmm.nbStates();
		normalize(initprob);
		
		// add new transition probablities
		double[][] transprob = new double[hmm.nbStates()+1][hmm.nbStates()+1];
		for (int i=0; i<transprob.length; i++) {
			for (int j=0; j<transprob.length; j++) {
				if (i < transprob.length-1 && j < transprob.length-1) { // old array
					transprob[i][j] = hmm.getAij(i, j);
					
				} else {
					if (i == transprob.length-1 && j == transprob.length-1) { // from new state to itself
						transprob[i][j] = hmm.nbStates();
					} else if (i == transprob.length-1) { // from new state to the other states
						transprob[i][j] = 1;
					} else { // from other states to the new state
						transprob[i][j] = .01; // arbitrary, probably should be 
											   // based on the other transition
											   // probabilities somehow
					}
				}
			}
		}
		
		// normalize all the new transition probabilities
		for (int i=0; i<transprob.length; i++) {
			normalize(transprob[i]);
		}
		
		// add the new pdf for the new state, we'll use a background model 
		// initially
		List<OpdfInteger> opdfs = new ArrayList<OpdfInteger>(hmm.nbStates()+1);
		double[][] pdfs = new double[hmm.nbStates()+1][NB_CHARS];
		
		// copy old pdfs
		for (int i=0; i<hmm.nbStates()+1; i++) {
			for (int j=0; j<NB_CHARS; j++) {
				if (i==hmm.nbStates())
					pdfs[i][j] = .25;
				else 
					pdfs[i][j] = hmm.getOpdf(i).probability(new ObservationInteger(j));
			}
		}
		
		for (double[] dist : pdfs) {
			opdfs.add(new OpdfInteger(dist));
		}
		
		// update the hmm
		hmm = new Hmm<ObservationInteger>(
				initprob,
				transprob,
				opdfs);
	}
	
	private ObservationInteger nucleotideCharToObservationInteger (char c) {		
		return new ObservationInteger(nucleotideCharToInt(c));
	}

	private int nucleotideCharToInt (char c) {
		switch (c) {
		case 'A': return 0;
		case 'C': return 1;
		case 'G': return 2;
		case 'T': return 3;			
		default: throw new RuntimeException("Only 'A', 'C', 'G', and 'T' characters are supported.");
		}
	}
	
	private void print2DArray (double[][] arr) {
		for (int i=0; i<arr.length; i++) {
			for (int j=0; j<arr[0].length; j++) {
				System.out.print(arr[i][j]+"\t");
			}
			System.out.print("\n");
		}
	}
	
	private void print2DArray (int[][] arr) {
		for (int i=0; i<arr.length; i++) {
			for (int j=0; j<arr[0].length; j++) {
				System.out.print(arr[i][j]+"\t");
			}
			System.out.print("\n");
		}
	}
	
	private void normalize (double[] arr) {
		double sum = 0;
		for (double d : arr) sum += d;
		for (int i=0; i<arr.length; i++)
			arr[i] /= sum;
	}
}
