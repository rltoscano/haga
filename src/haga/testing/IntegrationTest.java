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

import haga.HagaUtils;
import haga.NucleotideLabeledSequence;
import haga.align.Aligner;
import haga.align.Alignment;
import haga.align.GlobalAlignmentAlgorithm;
import haga.align.NeedlemanWunsch;
import haga.labeler.HMMLabeler;
import haga.labeler.Label;
import haga.labeler.LabeledAlignment;
import haga.labeler.LabeledSequence;
import haga.labeler.LabeledSequenceImpl;
import haga.labeler.Labeler;
import haga.labeler.XmlHmmLoader;

import java.io.File;
import java.util.LinkedList;
import java.util.List;

public class IntegrationTest {
	public static void main(String[] args) {
		// create a pair of sequences
		char[] s1 = HagaUtils.generateNucleotideSequence(100);
		s1 = "GAGGTCTGGGGTATATACCTACTGGGTATCGCGTTATTTCCACGGTCAATATTTTAACGGAAGTTAGGATGGCCTCTGCCCTATCCTCCTACGTTTCAGT"
				.toCharArray();
		char[] s2 = HagaUtils.generateNucleotideSequence(100);
		s2 = "CCCCCTGGGGCACTAACGAGAGCCTTGTCTTCATCCGACTGTAACAGTT".toCharArray();

		// assign some user-defined labels
		List<Label> lbls1, lbls2;
		lbls1 = new LinkedList<Label>();
		lbls2 = new LinkedList<Label>();
		// lbls1.add(new Label("intron", 0, 3, false));
		// lbls2.add(new Label("intron", 0, 0, false));
		// lbls1.add(new Label("exon", 4, 6, false));
		// lbls2.add(new Label("exon", 1, 3, false));

		// initialize labeled sequence
		LabeledSequence ls1 = new NucleotideLabeledSequence(s1, lbls1);
		LabeledSequence ls2 = new NucleotideLabeledSequence(s2, lbls2);

		// print
		HagaUtils.printLabeledSequence(ls1);
		HagaUtils.printLabeledSequence(ls2);

		// label it using our labeler
		Labeler l = XmlHmmLoader.loadXmlFile(new File("res/hmm.xml"));
		ls1 = l.labelSequence(ls1);
		ls2 = l.labelSequence(ls2);

		// print
		HagaUtils.printLabeledSequence(ls1);
		HagaUtils.printLabeledSequence(ls2);
		System.out.println();

		// align it
		Aligner a = new Aligner();
		LabeledAlignment la = a.align(ls1, ls2);

		// print
		HagaUtils.printLabeledSequence(la.getLS1());
		HagaUtils.printLabeledSequence(la.getLS2());

		System.out.println();

		s1 = ("CGCCTACGGGGCCGACCAGCG" + "TACGAGACAGTCTCGGTATTAGCTTGCAA"
				+ "CGACGTCGAGGCCAACGTTCGCGCATAGGTCGCC").toCharArray();
		s2 = ("ACGTGTAGCATAGCTATGCGATGAGAT" + "CGCCTACGTCGGGCCGACCAGCG"
				+ "AGACAGTACAGACTTCGGAACATTAGCTTGAA"
				+ "CGACGTGGAGGCCAACGTTCGCGCATAGGTCGCC").toCharArray();

		ls1 = new NucleotideLabeledSequence(s1, lbls1);
		ls2 = new NucleotideLabeledSequence(s2, lbls2);

		// label it using our labeler
		ls1 = l.labelSequence(ls1);
		ls2 = l.labelSequence(ls2);

		// print
		HagaUtils.printLabeledSequence(ls1);
		HagaUtils.printLabeledSequence(ls2);

		System.out.println();
		la = a.align(ls1, ls2);

		// print
		HagaUtils.printLabeledSequence(la.getLS1());
		HagaUtils.printLabeledSequence(la.getLS2());

		GlobalAlignmentAlgorithm nw = new NeedlemanWunsch();
		Alignment alignment = nw.align(ls1, ls2);
		HagaUtils.printLabeledSequence(new LabeledSequenceImpl(alignment
				.getSequence1()));
		HagaUtils.printLabeledSequence(new LabeledSequenceImpl(alignment
				.getSequence2()));

		System.out.println();
		
		char[] trainingSequence = "ACCCACAGCACTCATTCCTGAAGCTACTGGTTGGTTCCCTGAGAGGTCCCAGAACTCTGCGAAGTGAGTCCAGCGCTGGTAAGTCACCACCTGCTTAGGGTCATGCCCATCTGATCAGCAGCCAGCCAGTCAGGGACGGTGACACACATCCCAAAGTGGCACACAATATTTTTCTGTCTGTTTCGTGAGATGAACAGATTTAGGCTTTCATTTTTCCTCTAAATGTAGTTTTGTCTTCATCCATCAAATTGTGATTTGTGCTTGGTTTTTGTCATTTTAAAATTCTTATCGAAGCAGGTTTTTTAAAAATATATTAAAAATTTACAGTGACATGAATTTTTATTTCTTGACATTTGAAGTTATTTGTTTTTGTGCCCTTCAATTACAGTTCATAGACTTGGTGTTATTGTGATTCTCCAAGTATGCTTTCATTTTCATAAAATCCTTAAAGGTATCCCACACAGCAATCTCAAGAGTGCAGTTTTGCTCAGATCATGGGATTCATCTTTGCCCCTAGGATCTGTCCAAAAGTGGGTAATTGTGAGTATGTGGAAGTGATGTCTATAGGAACCTTCATCTTAGAGTTACAGTGCTCTAGAATAGCATGGTAGCACTTTTACGGTTTCTGGTTAATTTTTTTTTTCAGATGGAGTTTCCCTATTGTTGCCCAGGCTGCAGTGCCATGGTGTGATTTGGCTCACTGAAATTTCTGCCTCCTAGTTACATGCGATTCTCCTGCTTCAGCCTCCTGAGTAGCTTGGATTACAGGCACTCACCACCATGCCCAGCTAATTTTTGTATTTTTAGTAGACACAGGGTTTTGCCATGTTGGCCATGCTGTCCTCAAACTCCTGACCTCAGGAGATCTGCCCACCTCAGACTCCCAAAGTGCTGGGATTACATGAGTGAGCCACCGCGCCCAGCTACAGTTAGCATTTCTATACATACCTTCCAAATGCTGTGGAATACCATCACACCACTTTTACAGTTCCAGTGAATTTTGTTTTTTTCTGTGATGTACTCTGAGTGTGTCACCCAGACTGGAGTGCAGGGCCCTGAGCTGGGCTCCCTGGAAACTCTGCCTCTGGGCTTCAAGTGATTCTCCTTTCTCTGCCTCCAGAGTAGCTAGGATTACAGTCATGCATGACCACACCTGGCTAACATTTTAATTAACTTATTTATCAATTTGTTTTTGTTTGAGTCAGAGTCCAAGTCTGTCACCCAGGCTGGAGAGCAGTGGTGCGATCTTGGCTCATTGCAACTTCTGCCTTCTGGAGTCAAATGATTCTTAATTTTTTTATATTTAGTAGAGACATGGTTTCATTACGTAGGCCAGGCTGTTCTCAAATTACGGACCTCAAGTGATCTGCCTCCTTGGTGTCCAGCAGTGTTGGGATTACAGACATGAGCCACAGCACCTGGTCCATTTCTGGTAGAAAATTTTCAAAATAAAAAATAATGGCATCGATTTTAGGGAGTCCCTTTAGTGTTCCCCCAGCATGTTTATGGTGTAAACTGAGAATGGAGGCTGTCTGGGGCCACAGGACACTCTCATTCTCATTGCTTTAGGGTGGTAAGTGACAAGAAATTTTTCTTCAAAGAGGTAGAGCTTGGCTTTCAGGATCCTCAGTGGCACTGTCCGGTGGTTCTGGGATTCAGTGGAGCAATGGAAGAAAATTAATAAGTCAGTGGTCTCCATGACCCCTCCCTCCTTGGTGTTTGGAAGACATTCTTCCTGGTACCAGTAGAAGCAGATGATTGTCTTTGCCCTGAGAGTGACACATTTTCCCTGGATTTGTCTTCTAGAGATTTTCCTTGCAGATCTATCAGGATGAGCATCCAGGCCCCACCCAGACTACTGGAGCTGGCAGGGCAGAGCCTGCTGAGAGACCAGGCCTTGTCCATCTCTGCCATGGAGGAGCTGCCCAGGGTGCTCTATCTCCCACTCTTCATGGAGGCCTTCAGCAGGAGACACTTCCAGACTCTGACGGTGATGGTTCAGGCCTGGCCCTTCACCTGCCTCCCTCTGGGATCACTGATGAAGACGCTTCATTTGGAGACCTTAAAAGCATTGCTGGAAGGGCTTCATATGCTGCTTACACAGAAGGATCGCCCCAGGTGAGGTGACCCAGGAGGGCTGGTAGATAGGGCTCAGGTGTCCAGGGAAAGAACAGCAGGGTCAGGCAGAGAAGTAGCCCAAGTGTGGCCCAGAGTCTTCTGATGGTGTTGGCGAGGAAGATCAGGGAGGCTTTGGCCATTTTCCAGATCCTCAGAGAAAGGACTGCTCACCATACAGGGTCCACTGTGGGAACAGAAACCTGCCTTTACTCAGTGGAAGGTAAAGGGAATAGAAGTGGGGAATCAAAAGTCAGAATCAAAAGGGAACAGGGATTGAGAAAAGACAAAGAGAACAGGGAGCACTGAGGACAGGAGCAGCTGATTTATGGGATGACAATGAAAGCAAAGGTCAGGGATGAGTCCTTCTAAATTCTGAGTCTCTCCCTTACTTTACCCACAGGAGGTGGAAACTTCAAGTGCTGGATTTGCGGGATGTTGACGAGAATTTCTGGGCCAGATGGCCTGGAGCCTGGGCCCTGTCCTGCTTCCCAGAGACCACGAGTAAGAGGCAGACAGCAGAGGACTGTCCAAGGATGGGAGAGCACCAGCCCTTAAAGGTGTTCATAGACATCTGCCTCAAGGAAATACCCCAGGATGAATGCCTGAGATACCTCTTCCAGTGGGTTTACCAAAGGAGAGGTTTAGTACACCTGTGCTGTAGTAAGCTGGTCAATTATCTAACGCCGATTAAATATCTCAGAAAGTCATTGAAAATAATATACCTGAATAGTATTCAAGAGCTGGAAATTCGCAACATGTCCTGGCCACGTCTGATAAGAAAGCTTCGTTGTTACCTGAAGGAGATGAAGAATCTTCGCAAACTCGTTTTCTCCAGGTGCCATCATTACACGTCAGATAATGAACTCCAAGGACGGTTAGTTGCCAAATTCAGCTCTGTGTTCCTCAGGCTGGAACACCTTCAGTTGCTTAAAATAAAATTGATCACCTTCTTCAGTGGGCACCTGGAACAGCTGATCAGGTGAGAAAGGATCATGCACTTTGTATGCAGACCACAGCATAGCCTTGTTCTGTAACAGCAAACATTAGAAGGCATGTACTGTGTGCCAGCCAGTGGCAACGTCACAGTGAAGGGGACATCAGAATGTCAACACATTGTCCCATTCAGTGTTCCATGTCCTGGAGTGGCTATCACAGGATCGCTCCAATAAGGGGAGAGGGGTCACCTGGGGTAGAAGCTAGAGAGGGACATCATGTACAAGCTAGTTAGTGGGGGTTTCAGCTCTATTGGGGGTGCACGTGTGAATTTCCTGTTACAAAGTGTGTTTCAAGTTGATATGATGTCAAAGAGATAATAGAGGAGGGTATGAAAGGAGGGAAAGCGCATCAAACCTGTCCATTTCACAATAGAACGTCTGTCCTCACCGGCTTAGTGATCACGAATGATCCTGTCTCTGATTCCCTGTTTGTAAAAGGTTGTTTTGAACTCCAGGAAAGGTAATTGACATGGGAAATGCGTGCTTCCGGGATGGAGGTGAGGGAGTAGGCGTGAGAGTGGTAAAAAGTGACAGTTGGTTTGCAGATGCAGGCATGTCAGGGAGCCCCTGCTGACATGTAGCTCTAGCTGATGTCCCTAGACCTTGCTCAGTTGAGTTCTTTGTTCACATCTCCCACCGGGTACCTGTGGCCCAGAGATGAAGTTTTCTGCTAAAAGATGAAAAAAAAAAGGCTTTAGAGATTTTATGGCCTTGAACCAATCACACAAGCAATGGTGAAAGGGCTGAGGCTAAAATGGGACAGCCCCTGAACGATCAGGGTCCTCATCATGCAGCAACTTCCATGAGGACCATCATCAGATGGTGGGAACAAACTTGTGTTTGTTTGACGCAGGCATTTTCCTAGATGAAGGCACTACCTTCATCTAACTGGTATCACTGCCCAGAACTAACTTCTTGATCTCCACAGGTGCCTCCAGAACCCCTTGGAGAACTTGGAATTAACTTATGGCTACCTATTGGAAGAAGACATGAAGTGTCTCTCCCAGTACCCAAGCCTCGGTTACCTAAAGCATCTGAATCTCAGCTACGTGCTGCTGTTCCGCATCAGTCTTGAACCCCTCGGAGCTCTGCTGGAGAAAATTGCTGCCTCTCTCAAAACCCTCATCTTGGAGGGCTGTCAGATCCACTACTCCCAACTCAGTGCCATCCTGCCTGCCCTGAGCCGGTGCTCCCAGCTCACCACCTTCTACTTTGGCAGAAATTGCATGTCTATTGACGCCCTGAAGGACCTGCTGCGCCACACCAGTGGGCTGAGCAAGTTAAGCCTGGAGACGTATCCTGCCCCTGAGGAGAGTTTGAATTCCTTGGTTCGTGTCAATTGGGAGATCTTCACCCCACTTCGGGCTGAGCTGATGTGTACACTGAGGGAAGTCAGGCAGCCCAAGAGGATCTTCATTGGCCCCACCCCCTGCCCTTCCTGTGGCTCATCACCGTCTGAGGAACTGGAGCTCCATCTTTGCTGCTAGGGAAGGCGTGCCCAGTGGGGTAGAGAAATCCAAAGTTCTCTTCCAGGCACTTGGACACTAAAATCTACTATGTGGGTG"
				.toCharArray();
		List<Label> trainingls = new LinkedList<Label>();
		trainingls.add(new Label("Intron", 0, 1831, true));
		trainingls.add(new Label("Exon", 1832, 2118, true));
		trainingls.add(new Label("Intron", 2119, 2518, true));
		trainingls.add(new Label("Exon", 2519, 3097, true));
		trainingls.add(new Label("Intron", 3098, 4041, true));
		trainingls.add(new Label("Exon", 4042, 4600, true));
		trainingls.add(new Label("Intron", 4601, trainingSequence.length - 1, true));		
		
		
		l.train(new NucleotideLabeledSequence(trainingSequence, trainingls), .5);
		System.out.println(((HMMLabeler)l).hmm.getAij(1, 1));
		
		LabeledSequence result = l.labelSequence(new LabeledSequenceImpl(trainingSequence));
		HagaUtils.printLabeledSequence(result);
	}
}
