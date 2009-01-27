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

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;


public class XmlHmmLoader {
	public static HMMLabeler loadXmlFile (File xmlFile) {		
		// get the factory
		DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();

		try {			
			//Using factory get an instance of document builder
			DocumentBuilder db = dbf.newDocumentBuilder();

			//parse using builder to get DOM representation of the XML file
			Document dom = db.parse(xmlFile);
			
			Element rootElmt = dom.getDocumentElement();

			NodeList stateNodes = rootElmt.getChildNodes();
			
			// for creation of hmm
			int nbStates = 0;
			List<Double> initProb = new ArrayList<Double>();
			List<String> labelNames = new ArrayList<String>();
			
			// initial pass on the data, getting count and labelNames
			for (int i=0; i<stateNodes.getLength(); i++) {
				Node state = stateNodes.item(i);
				
				if (state.getNodeType() == Node.ELEMENT_NODE &&
					state.getNodeName().equals("state")) {
					nbStates++;
					labelNames.add(state.getAttributes().getNamedItem("label").getNodeValue());
					initProb.add(new Double(state.getAttributes().getNamedItem("initialProbability").getNodeValue()));
				}
			}
			
			// for the hmm
			double[][] transProb = new double[nbStates][nbStates];
			double[][] initialDist = new double[nbStates][4];
			
			// second pass gathering transition probabilities and distributions
			for (int i=0; i<stateNodes.getLength(); i++) {
				if (stateNodes.item(i).getNodeType() != Node.ELEMENT_NODE ||
					!stateNodes.item(i).getNodeName().equals("state"))
					continue;
				
				int labelIndex = labelNames.indexOf(stateNodes.item(i).getAttributes().getNamedItem("label").getNodeValue());
				
				for (int j=0; j<stateNodes.item(i).getChildNodes().getLength(); j++) {
					Node child = stateNodes.item(i).getChildNodes().item(j);
					
					if (child.getNodeType() == Node.ELEMENT_NODE) {
						if (child.getNodeName().equals("transitionProbability")) {
							int fromLabelIndex = labelNames.indexOf(child.getAttributes().getNamedItem("from").getNodeValue());
							transProb[labelIndex][fromLabelIndex] = Double.parseDouble(child.getFirstChild().getNodeValue());
							
						} else if (child.getNodeName().equals("probabilityDistribution")) {
							initialDist[labelIndex][0] = Double.parseDouble(child.getAttributes().getNamedItem("a").getNodeValue());
							initialDist[labelIndex][1] = Double.parseDouble(child.getAttributes().getNamedItem("g").getNodeValue());
							initialDist[labelIndex][2] = Double.parseDouble(child.getAttributes().getNamedItem("c").getNodeValue());
							initialDist[labelIndex][3] = Double.parseDouble(child.getAttributes().getNamedItem("t").getNodeValue());
						}
					}
				}
			}
			
			print("Finished loading HMM labeler.");
			print("Number of States: "+nbStates);
			print("Labels: "+labelNames);
			print("Transition Probabilities:");
			print2DArray(transProb);
			print("Initial Distributions");
			print2DArray(initialDist);
			
			double[] initProbArr = new double[initProb.size()];
			
			for (int i=0; i<initProbArr.length; i++)
				initProbArr[i] = initProb.get(i);
			
			return new HMMLabeler(nbStates, initProbArr, transProb, initialDist, labelNames);
			
		}catch(ParserConfigurationException pce) {
			pce.printStackTrace();
		}catch(SAXException se) {
			se.printStackTrace();
		}catch(IOException ioe) {
			ioe.printStackTrace();
		}
		
		return null;
	}
	
	private static void print(Object o) {
		System.out.println(o);
	}
	
	private static void print2DArray (double[][] arr) {
		for (int i=0; i<arr.length; i++) {
			for (int j=0; j<arr[0].length; j++) {
				System.out.print(arr[i][j]+"\t");
			}
			System.out.print("\n");
		}
	}
}
