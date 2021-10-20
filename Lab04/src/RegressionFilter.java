import java.util.ArrayList;
import java.util.Iterator;

import org.jacop.*;
import org.jacop.constraints.Cumulative;
import org.jacop.constraints.Diff2;
import org.jacop.constraints.Max;
import org.jacop.constraints.XgteqY;
import org.jacop.constraints.XltY;
import org.jacop.constraints.XplusYeqZ;
import org.jacop.constraints.XplusYlteqZ;
import org.jacop.constraints.diffn.Diffn;
import org.jacop.core.IntVar;
import org.jacop.core.Store;
import org.jacop.search.DepthFirstSearch;
import org.jacop.search.IndomainMin;
import org.jacop.search.MostConstrainedStatic;
import org.jacop.search.Search;
import org.jacop.search.SelectChoicePoint;
import org.jacop.search.SimpleSelect;

public class RegressionFilter {

	public static void main(String[] args) {

		// Vi behöver en cumulative för add och en för mul

		// a list of tasks’ starts O, 
		// a list of tasks’ durations D, 
		// a list of amount
		// 	of resources AR required by each task, and 
		// the upper limit of the amount of
		// 	used resources Limit.
		double tid = System.currentTimeMillis();
		
		DataSet6 data = new DataSet6();
		Store store = new Store();
		
		System.out.println("Number of adds: " + data.add.length);
		System.out.println("Number of muls: " + data.mul.length);
		System.out.println("Number of adders: " + data.number_add);
		System.out.println("Number of mulers: " + data.number_mul);
		
		IntVar[] addStart = new IntVar[data.add.length];
		IntVar[] mulStart = new IntVar[data.mul.length];
		IntVar[] allStart = new IntVar[data.add.length + data.mul.length];
		IntVar[] allEnd	  = new IntVar[data.add.length + data.mul.length];
		
		IntVar[] addDelay = new IntVar[data.add.length];
		IntVar[] mulDelay = new IntVar[data.mul.length];
		IntVar[] allDelay = new IntVar[data.add.length + data.mul.length];
		
		IntVar[] addProcNeed = new IntVar[data.add.length];
		IntVar[] mulProcNeed = new IntVar[data.mul.length];
		
		IntVar[] addProcUsed = new IntVar[data.add.length];
		IntVar[] mulProcUsed = new IntVar[data.mul.length];
		IntVar[] allProcUsed = new IntVar[data.add.length + data.mul.length];
		
		IntVar[][] addRects = new IntVar[data.add.length][4];	//first index for rectangle, second index: 0- start x, 1- start y, 2- length x, 3- length y
		IntVar[][] mulRects = new IntVar[data.mul.length][4];

		
		IntVar addLimit = new IntVar(store, "Limit", data.number_add, data.number_add);
		IntVar mulLimit = new IntVar(store, "Limit", data.number_mul, data.number_mul);
		
		IntVar cost = new IntVar(store, "Cost", 0, 100);
		
		for(int i = 0; i < data.n; i++) {
			allStart[i] = new IntVar(store, "Task " + (i+1) + " start", 0, 50);
			allEnd[i] = new IntVar(store, "Task " + (i+1) + " end", 0, 50);
		}
		
		//Initializing add vars
		for(int i = 0; i < data.add.length; i++) {
			addStart[i] = allStart[data.add[i] - 1];
			addDelay[i] = new IntVar(store, data.del_add, data.del_add);
			addProcNeed[i] = new IntVar(store, 1, 1);
			
			addProcUsed[i] = new IntVar(store, "Adding task " + (data.add[i]) + " used processor ", 1, data.number_add);
			
			allDelay[data.add[i] - 1] = addDelay[i];
			allProcUsed[data.add[i] - 1] = addProcUsed[i];
			
		}
		
		//Initializing mul vars
		for(int i = 0; i < data.mul.length; i++) {
			mulStart[i] = allStart[data.mul[i] - 1];
			mulDelay[i] = new IntVar(store, data.del_mul, data.del_mul);
			mulProcNeed[i] = new IntVar(store, 1, 1);
			
			mulProcUsed[i] = new IntVar(store, "Mulling task " + (data.mul[i]) + " used processor ", 1, data.number_mul);
			
			allDelay[data.mul[i] - 1] = mulDelay[i];
			allProcUsed[data.mul[i] - 1] = mulProcUsed[i];
		}
		
		for(int i = 0; i < allEnd.length; i++) {
			store.impose(new XplusYeqZ(allStart[i], allDelay[i], allEnd[i]));
		}
		
		//Initializing precedence constraints
		for(int i = 0; i < data.dependencies.length; i++) {
			for(int j = 0; j < data.dependencies[i].length; j++) {
				//Check if the active operation is an add or mul
				//IntVar delay = allDelay[data.dependencies[i][j] - 1];
				IntVar delay = allDelay[i];
				
				
				//task oAll[i] must be executed after its preceders (oAll[i] >= precederVar + operationTime)
				//IntVar z = new IntVar(store, 0, 50);
				//store.impose(new XplusYeqZ(delay, allStart[i], z));
				//store.impose(new XltY(z, allStart[data.dependencies[i][j] - 1]));
				
				store.impose(new XplusYlteqZ(delay, allStart[i], allStart[data.dependencies[i][j] - 1]));
				System.out.println(data.dependencies[i][j] + " has to be executed after task " + allStart[i]);
			}
		}
		
//		boolean isLast = false;
//		for(int i = 0; i < allStart.length; i++) {
//			for(int j = 0; j < data.last.length; j++) {
//				if(allStart[i] == allStart[data.last[j] - 1]) {
//					isLast = true;
//				}
//			}
//			
//			for(int j = 0; j < data.last.length; j++) {
//				if(!isLast) {
//					//store.impose(new XgteqY(allStart[data.last[j] - 1], allStart[i]));
//					store.impose(new XplusYlteqZ(allDelay[i], allStart[i], allStart[data.last[j] - 1]));
//					System.out.println("Execution start time for " + allStart[data.last[j] - 1] + " is after " + allStart[i]);
//				} else {
//					System.out.println(allStart[i] + " is one of the last operations.");
//				}
//			}
//			isLast = false;
//		}
		
		//Impose cumulative constr for add
		store.impose(new Cumulative(addStart, addDelay, addProcNeed, addLimit));
		
		//Impose cumulative constr for mul
		store.impose(new Cumulative(mulStart, mulDelay, mulProcNeed, mulLimit));
		
		//Implementera diff ctr
		
		//Create diff rectangles for adding
		for(int i = 0; i < data.add.length; i++) {
			addRects[i][0] = addStart[i];
			addRects[i][1] = addProcUsed[i];
			addRects[i][2] = addDelay[i];
			addRects[i][3] = new IntVar(store, 1, 1);	//Each operation only needs one processor. Height of rect is always 1.
		}
		
		//Create diff rectangles for mulling
		for(int i = 0; i < data.mul.length; i++) {
			mulRects[i][0] = mulStart[i];
			mulRects[i][1] = mulProcUsed[i];
			mulRects[i][2] = mulDelay[i];
			mulRects[i][3] = new IntVar(store, 1, 1);	//Each operation only needs one processor. Height of rect is always 1.
		}
		
		store.impose(new Diffn(addRects));
		store.impose(new Diffn(mulRects));
		
		store.impose(new Max(allEnd, cost));
		
		//Solve and search
		Search<IntVar> slave = new DepthFirstSearch<IntVar>();
		SelectChoicePoint slaveSelect = new SimpleSelect<IntVar>(allProcUsed, new MostConstrainedStatic<IntVar>(), new IndomainMin<IntVar>());
		slave.setSelectChoicePoint(slaveSelect);
		
		Search<IntVar> search = new DepthFirstSearch<IntVar>();
		search.addChildSearch(slave);
		search.setPrintInfo(true);
		SelectChoicePoint<IntVar> select = new SimpleSelect<IntVar>(allStart, new MostConstrainedStatic<IntVar>(), new IndomainMin<IntVar>());
		
		boolean result = search.labeling(store, select, cost);
		double tid2 = System.currentTimeMillis();
		//Print result
		if(result) {
			for(int i = 0; i < allStart.length; i++) {
				System.out.println(allStart[i] + " ----- " + allProcUsed[i]);
			}
			System.out.println(tid2-tid);
            System.out.println("Nodes visited: " + search.getNodes());
            System.out.println("Wrong decisions: " + search.getWrongDecisions());
		} else {
			System.out.println("No solution.");
		}

	}
}
