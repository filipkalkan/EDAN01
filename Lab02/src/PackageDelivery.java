import org.jacop.constraints.*;
import org.jacop.core.*;
import org.jacop.search.*;

public class PackageDelivery {

	public static void main(String[] args) {
		
		//Input data
		int graph_size = 6;
		int start = 1;
		int n_dests = 1;
		int[] dest = {6};
		int n_edges = 7;
		int[] from = {1,1,2,2,3,4,4};
		int[] to =   {2,3,3,4,5,5,6};
		int[] cost = {4,2,5,10,3,4,11};
		
		Store store = new Store();
		IntVar[][] optDest = new IntVar[n_dests][graph_size];
		ElementInteger[][] nodes = new ElementInteger[n_dests][graph_size];
		IntVar[][] actCost = new IntVar[n_dests][graph_size];
		
		
		//Lägg destinationer till from-vektorn
		int[] temp1 = new int[from.length + n_dests];
		for(int i = 0; i < from.length + n_dests; i++) {
			if(i < from.length) {
				temp1[i] = from[i];
			} else {
				temp1[i] = dest[i - from.length];
			}
		}
		from = temp1;
		
		//Lägg start till to-vektorn
		int[] temp2 = new int[to.length + n_dests];
		for(int i = 0; i < to.length + n_dests; i++) {
			if(i < from.length) {
				temp2[i] = to[i];
			} else {
				temp2[i] = start;
			}
		}
		to = temp2;
		
		//Lägg till 0-kostnad till cost-vektorn
		int[] temp3 = new int[cost.length + n_dests];
		for(int i = 0; i < cost.length + n_dests; i++) {
			if(i < cost.length) {
				temp3[i] = cost[i];
			} else {
				temp3[i] = 0;
			}
		}
		cost = temp3;
		
		for(int i = 0; i < n_dests; i++) {
			for(int j = 0; j < graph_size; j++) {
				optDest[i][j] = new IntVar(store, "Node " + j);
			}
		}
		
		for(int i = 0; i < to.length; i++) {
			for(int j = 0; j < n_dests; j++) {
				optDest[j][from[i]].addDom(to[i], to[i]);
			}
		}
		
		for(int i = 0; i < graph_size; i++) {
			for(int j = 0; j < n_dests; j++) {
				nodes[j][i] = new ElementInteger(optDest[j][i], cost, actCost[j][i], 0);
				store.impose(nodes[j][i]);
			}
		}
		
		for(int i = 0; i < n_dests; i++) {
			store.impose(new Subcircuit(optDest[i]));
		}
		
		for(int i = 0; i < graph_size; i++) {
			for(int j = 0; j < n_dests; j++) {
				store.impose(new IfThen(new XeqY(optDest[j][i], optDest[j+1][i])));
			}
		}
		
		Search<IntVar> search = new DepthFirstSearch<IntVar>();
		search.setPrintInfo(true);
		SelectChoicePoint<IntVar> select = new SimpleSelect<IntVar>(optDest[0], null, new IndomainMin<IntVar>());
		
		boolean result = search.labeling(store, select, cost);
		
		if(result) {
			for(int i = 0; i < optDest[0].length; i++) {
				System.out.println(optDest[0][i]);
			}
			System.out.println(cost);
		} else {
			System.out.println("No solution.");
		}
		

	}

}
