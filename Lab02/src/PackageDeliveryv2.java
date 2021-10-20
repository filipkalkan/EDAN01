import org.jacop.constraints.*;
import org.jacop.constraints.netflow.*;
import org.jacop.constraints.netflow.simplex.Arc;
import org.jacop.constraints.netflow.simplex.Node;
import org.jacop.core.*;
import org.jacop.search.*;

public class PackageDeliveryv2 {

	public static void main(String[] args) {
		
		//TODO: Spara alla kostnader i en vektor som mappas till arcUsed för att räkna ut totCost
		
		//Input data
		int graph_size = 6;
		int start = 1;
		int n_dests = 2;
		int[] dest = {5,6};
		int n_edges = 7;
		int[] from = {1,1,2, 2,3,4, 4};
		int[] to = {2,3,3, 4,5,5, 6};
		int[] cost = {4,2,5,10,3,4,11};


		
		Store store = new Store();
		NetworkBuilder builder = new NetworkBuilder();
		
		IntVar[] arcTimesUsed = new IntVar[2 * n_edges + n_dests];	//Stores usage data of bidirectional edges. Edges dest -> sink at end of vector.
		Node[] nodes = new Node[graph_size + 1];
		IntVar[] arcUsed = new IntVar[arcTimesUsed.length];
		IntVar totCost = new IntVar(store, "Total cost: ", 0, 1000);
		int[] arcCost = new int[arcUsed.length];
		IntVar[] actCost = new IntVar[arcUsed.length];
		
		
		//Instatiate all nodes in the network. Balance = 0
		for(int i = 0; i <= graph_size; i++) {
			nodes[i] = builder.addNode("Node " + i, 0);
		}
		
		for(int i = 0; i < n_edges; i++) {
			actCost[i] = new IntVar(store, 0, 0);
			arcUsed[i] = new IntVar(store, 0, 0);
			actCost[i + n_edges] = new IntVar(store, 0, 0);
			arcUsed[i + n_edges] = new IntVar(store, 0, 0);
		}
		
		for(int i = 0; i < n_dests; i++) {
			actCost[2 * n_edges + i] = new IntVar(store, 0, 0);
		}
		
		for(int i = 0; i < n_edges; i++) {
			//Instantiate variables containing information about usage of arc
			arcTimesUsed[i] = new IntVar(store, 0, n_dests);
			arcTimesUsed[i + n_edges] = new IntVar(store, 0, n_dests);
			
			//Instatiate variables containing cost for each edge
			arcCost[i] = cost[i];
			arcCost[i + n_edges] = cost[i];
			
			//Add bidirectional arcs to network
			builder.addArc(nodes[from[i]], nodes[to[i]], cost[i], arcTimesUsed[i]);
			builder.addArc(nodes[to[i]], nodes[from[i]], cost[i], arcTimesUsed[i + n_edges]);
			
			//Instatiate arcUsed
			arcUsed[i] = new IntVar(store, "From " + from[i] + " to " + to[i] , 0, 1);
			arcUsed[i + n_edges] = new IntVar(store, "From " + to[i] + " to " + from[i] , 0, 1);
			
		}
		
		//Add source and sink to network
		Node source = builder.addNode("Source", n_dests);
		Node sink = builder.addNode("Sink", -n_dests);
		
		//Add arc from source to start node
		builder.addArc(source, nodes[start], 0);
		
		//Add arcs from destinations to sink node
		for(int i = 0; i < n_dests; i++) {
			arcTimesUsed[2 * n_edges + i] = new IntVar(store, "From destination " + i + "to sink", 0, 1);
			builder.addArc(nodes[dest[i]], sink, 0, arcTimesUsed[2 * n_edges + i]);
			
			//Add constraint: edge dest -> sink must be used 1 time
			store.impose(new XeqC(arcTimesUsed[2 * n_edges + i], 1));
		}
		
		//If arcTimesUsed[i] >= 1, set arcUsed[i] to 1.
		for(int i = 0; i < n_edges; i++) {
			store.impose(new Reified(new XgteqC(arcTimesUsed[i], 1), arcUsed[i]));
			store.impose(new Reified(new XgteqC(arcTimesUsed[i + n_edges], 1), arcUsed[i + n_edges]));
		}
		
		//Continue doing this for dest -> sink
		for(int i = 0; i < n_dests; i++) {
			arcUsed[2 * n_edges + i] = new IntVar(store, "From destination " + i + " to sink", 0, 1);
			store.impose(new Reified(new XgteqC(arcTimesUsed[2 * n_edges + i], 1), arcUsed[2 * n_edges + i]));
		}
		
		builder.setCostVariable(new IntVar(store, 0, 1000));
		
		store.impose(new LinearInt(arcUsed, arcCost, "==", totCost));
		
		//Create and impose the network
		store.impose(new NetworkFlow(builder));
		
		//Solve and search
		Search<IntVar> search = new DepthFirstSearch<IntVar>();
		search.setPrintInfo(true);
		SelectChoicePoint<IntVar> select = new SimpleSelect<IntVar>(arcUsed, null, new IndomainMin<IntVar>());
		
		boolean result = search.labeling(store, select, totCost);
		
		//Print result
		if(result) {
			for(int i = 0; i < arcUsed.length; i++) {
				System.out.println(arcUsed[i]);
			}
		} else {
			System.out.println("No solution.");
		}
	}
}
