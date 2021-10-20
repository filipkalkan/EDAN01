
/**
 *  SimpleDFS.java 
 *  This file is part of JaCoP.
 *
 *  JaCoP is a Java Constraint Programming solver. 
 *	
 *	Copyright (C) 2000-2008 Krzysztof Kuchcinski and Radoslaw Szymanek
 *
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU Affero General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU Affero General Public License for more details.
 *  
 *  Notwithstanding any other provision of this License, the copyright
 *  owners of this work supplement the terms of this License with terms
 *  prohibiting misrepresentation of the origin of this work and requiring
 *  that modified versions of this work be marked in reasonable ways as
 *  different from the original version. This supplement of the license
 *  terms is in accordance with Section 7 of GNU Affero General Public
 *  License version 3.
 *
 *  You should have received a copy of the GNU Affero General Public License
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *
 */

import java.util.ArrayList;

import org.jacop.constraints.Not;
import org.jacop.constraints.PrimitiveConstraint;
import org.jacop.constraints.XeqC;
import org.jacop.constraints.XgteqC;
import org.jacop.constraints.XltC;
import org.jacop.constraints.XlteqC;
import org.jacop.core.FailException;
import org.jacop.core.IntDomain;
import org.jacop.core.IntVar;
import org.jacop.core.Store;

/**
 * Implements Simple Depth First Search .
 * 
 * @author Krzysztof Kuchcinski
 * @version 4.1
 */

public class DFSv1 {
	
	/**
	 * 								Tot num nodes	Wrong decisions
	 * 
	 * 	SimpleDFS (input order)			21683			10788
	 * 
	 * 	SplitSearch 1 (input order)		22463			11170
	 * 
	 * 	SplitSearch 1 (middle)			51261			25521
	 * 
	 * 	SplitSearch 1 (most const)		47381			23676
	 * 
	 * 	SplitSearch 2 (input order)		698132			348881
	 * 
	 * 	SplitSearch 2 (most constr)		1582480			790356
	 */

	boolean trace = true;
	int failed = 0;
	int nbrSearchNodes = 0;

	/**
	 * Store used in search
	 */
	Store store;

	/**
	 * Defines varibales to be printed when solution is found
	 */
	IntVar[] variablesToReport;

	/**
	 * It represents current depth of store used in search.
	 */
	int depth = 0;

	/**
	 * It represents the cost value of currently best solution for FloatVar cost.
	 */
	public int costValue = IntDomain.MaxInt;

	/**
	 * It represents the cost variable.
	 */
	public IntVar costVariable = null;

	public DFSv1(Store s) {
		store = s;
	}

	/**
	 * This function is called recursively to assign variables one by one.
	 */
	public boolean label(IntVar[] vars) {
		nbrSearchNodes++;
		
		if (trace) {
			for (int i = 0; i < vars.length; i++)
				System.out.print(vars[i] + " ");
			System.out.println();
		}

		ChoicePoint choice = null;
		boolean consistent;

		// Instead of imposing constraint just restrict bounds
		// -1 since costValue is the cost of last solution
		if (costVariable != null) {
			try {
				if (costVariable.min() <= costValue - 1)
					costVariable.domain.in(store.level, costVariable, costVariable.min(), costValue - 1);
				else
					return false;
			} catch (FailException f) {
				return false;
			}
		}

		consistent = store.consistency();

		if (!consistent) {
			// Failed leaf of the search tree
			failed++;
			return false;
		} else { // consistent

			if (vars.length == 0) {
				// solution found; no more variables to label

				// update cost if minimization
				if (costVariable != null)
					costValue = costVariable.min();

				reportSolution();

				return costVariable == null; // true is satisfiability search and false if minimization
			}

			choice = new ChoicePoint(vars);

			levelUp();

			store.impose(choice.getConstraint());

			// choice point imposed.

			consistent = label(choice.getSearchVariables());

			if (consistent) {
				levelDown();
				return true;
			} else {

				restoreLevel();

				store.impose(new Not(choice.getConstraint()));

				// negated choice point imposed.

				consistent = label(vars);

				levelDown();

				if (consistent) {
					return true;
				} else {
					return false;
				}
			}
		}
	}

	void levelDown() {
		store.removeLevel(depth);
		store.setLevel(--depth);
	}

	void levelUp() {
		store.setLevel(++depth);
	}

	void restoreLevel() {
		store.removeLevel(depth);
		store.setLevel(store.level);
	}

	public void reportSolution() {
		if (costVariable != null)
			System.out.println("Cost is " + costVariable);

		for (int i = 0; i < variablesToReport.length; i++)
			System.out.print(variablesToReport[i] + " ");
		System.out.println("\nNumber of search nodes: " + nbrSearchNodes);
		System.out.println("Failed leafs of the search tree: " + failed);
		System.out.println("\n---------------");
	}

	public void setVariablesToReport(IntVar[] v) {
		variablesToReport = v;
	}

	public void setCostVariable(IntVar v) {
		costVariable = v;
	}

	public class ChoicePoint {

		IntVar var;
		IntVar[] searchVariables;
		int value;

		public ChoicePoint(IntVar[] v) {
			var = selectVariable(v);
			value = selectValue(var);
		}

		public IntVar[] getSearchVariables() {
			return searchVariables;
		}

		/**
		 * example variable selection; input order
		 * 0 for input order
		 * 1 for last
		 * 2 for middle
		 * 3 for most constrained
		 * 4 for first fail
		 */
		IntVar selectVariable(IntVar[] v) {
			int input = 0;
			
			if (v.length != 0) {
				//TODO: Kolla om den valda variabeln har ett single value. ISF så har vi prunat färdigt och kan ta bort den från vår array.
				//I detta fallet tar vi alltid bort första v[0] elementet. Ändra detta.
				searchVariables = new IntVar[v.length - 1];

				switch(input) {
				case 0:
					for(int i = 0; i < v.length - 1; i++) {
						if(i < 0) {
							searchVariables[i] = v[i];
						} else {
							searchVariables[i] = v[i + 1];
						}
					}
					if(v[0].min() == v[0].max()) {
						return v[0];
					} else {
						searchVariables = new IntVar[v.length];
						for(int i = 0; i < v.length; i++) {
							searchVariables[i] = v[i];
						}
					}
					return v[0];
				case 1:
					for(int i = 0; i < v.length - 1; i++) {
						if(i < v.length - 1) {
							searchVariables[i] = v[i];
						} else {
							searchVariables[i] = v[i + 1];
						}
					}
					if(v[v.length - 1].min() == v[v.length - 1].max()) {
						return v[v.length - 1];
					} else {
						searchVariables = new IntVar[v.length];
						for(int i = 0; i < v.length; i++) {
							searchVariables[i] = v[i];
						}
					}
					return v[v.length - 1];
			case 2:
				for(int i = 0; i < v.length - 1; i++) {
					if(i < v.length / 2) {
						searchVariables[i] = v[i];
					} else {
						searchVariables[i] = v[i + 1];
					}
				}
				if(v[v.length / 2].min() == v[v.length / 2].max()) {
					return v[v.length / 2];
				} else {
					searchVariables = new IntVar[v.length];
					for(int i = 0; i < v.length; i++) {
						searchVariables[i] = v[i];
					}
				}
				return v[v.length / 2];
			case 3:
				int max = 0;
				int maxSizeConstraints = 0;
				for(int i = 0; i < v.length; i++) {
					if(v[i].sizeConstraints() > maxSizeConstraints) {
						maxSizeConstraints = v[i].sizeConstraints();
						max = i;
					}
				}
				
				for(int i = 0; i < v.length - 1; i++) {
					if(i < max) {
						searchVariables[i] = v[i];
					} else {
						searchVariables[i] = v[i + 1];
					}
				}
				if(v[max].min() == v[max].max()) {
					return v[max];
				} else {
					searchVariables = new IntVar[v.length];
					for(int i = 0; i < v.length; i++) {
						searchVariables[i] = v[i];
					}
				}
				return v[max];
			case 4:
				int minDomIndex = 0;
				int minDomSize = Integer.MAX_VALUE;
				for(int i = 0; i < v.length; i++) {
					if(v[i].dom().getSize() < minDomSize) {
						minDomSize = v[i].dom().getSize();
						minDomIndex = i;
					}
				}
				
				for(int i = 0; i < v.length - 1; i++) {
					if(i < minDomIndex) {
						searchVariables[i] = v[i];
					} else {
						searchVariables[i] = v[i + 1];
					}
				}
				if(v[minDomIndex].min() == v[minDomIndex].max()) {
					return v[minDomIndex];
				} else {
					searchVariables = new IntVar[v.length];
					for(int i = 0; i < v.length; i++) {
						searchVariables[i] = v[i];
					}
				}
				return v[minDomIndex];
			}
				return new IntVar(store);
			} else {
				System.err.println("Zero length list of variables for labeling");
				return new IntVar(store);
			}
		}

		/**
		 * Select value for v;
		 * 0 for min
		 * 1 for max
		 * 2 for middle
		 */
		int selectValue(IntVar v) {
			int input = 3;
			
			switch(input) {
			case 0:
				return v.min();
			case 1:
				return v.max();
			case 2:
				return (v.min() + v.max()) / 2; // Use with XlteqC
			case 3: 
				return (v.min() + v.max() + 1) / 2;		// Use with XgteqC
			}
			
			return v.min();
		}

		/**
		 * example constraint assigning a selected value
		 */
		public PrimitiveConstraint getConstraint() {
			int input = 1;	
			
			switch(input) {
				case 0:
					return new XeqC(var, value);
				case 1:
					return new XlteqC(var, value);
				case 2:
					return new XgteqC(var, value);
			}
			return null;
		}
	}
}
