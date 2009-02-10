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
import haga.align.AlignerFactory;
import haga.labeler.Label;
import haga.labeler.LabeledAlignment;
import haga.labeler.LabeledSequence;

import java.util.LinkedList;
import java.util.List;

public class AlignerTest {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		
		// create the labeled sequences to align
		List<Label> labels1 = new LinkedList<Label>();
		List<Label> labels2 = new LinkedList<Label>();
		
		labels1.add(new Label("intron", 0, 0, true));
		labels1.add(new Label("ExOn", 3, 20, true));
		labels1.add(new Label("intron", 21, 24, true));
		labels1.add(new Label("ExOn", 27, 39, true));
		labels1.add(new Label("intron", 45, 50, true));
		
		labels2.add(new Label("intron", 0, 0, true));
		labels2.add(new Label("ExOn", 10, 35, true));
		labels2.add(new Label("intron", 36, 39, true));
		labels2.add(new Label("ExOn", 40, 50, true));
		labels2.add(new Label("intron", 60, 71, true));
		/*
		LabeledSequence ls1 = new NucleotideLabeledSequence(
				HagaUtils.generateNucleotideSequence(1000), 
				new LinkedList<Label>());
		LabeledSequence ls2 = new NucleotideLabeledSequence(
				HagaUtils.generateNucleotideSequence(1000), 
				new LinkedList<Label>());
		*/
		LabeledSequence ls1 = new NucleotideLabeledSequence(
				HagaUtils.getHumanHox(), 
				labels1);
		LabeledSequence ls2 = new NucleotideLabeledSequence(
				HagaUtils.getMouseHox(), 
				labels2);
		
		HagaUtils.printLabeledSequence(ls1);
		HagaUtils.printLabeledSequence(ls2);
		
		AlignerFactory.SetInstance(new Aligner());
		Aligner aligner = AlignerFactory.GetInstance();
		LabeledAlignment la = aligner.align(ls1, ls2);
		
		la = aligner.align(la.getLS1(), la.getLS2());
		
		HagaUtils.printLabeledSequence(la.getLS1());
		HagaUtils.printLabeledSequence(la.getLS2());
		
		/*la = aligner.removeGaps(la);
		
		HagaUtils.printLabeledSequence(la.getLS1());
		HagaUtils.printLabeledSequence(la.getLS2());*/
	}

}
