import org.jacop.constraints.*;
import org.jacop.core.*;
import org.jacop.search.*;

public class PhotoProblemV1 {

	public static void main(String[] args) {
		int n = 11;
		int n_prefs = 20;
		int[][] prefs = {{1,3}, {1,5}, {2,5},
		{2,8}, {2,9}, {3,4}, {3,5}, {4,1},
		{4,5}, {4,6}, {5,1}, {6,1}, {6,9},
		{7,3}, {7,5}, {8,9}, {8,7}, {8,10},
		{9, 11}, {10, 11}};

		
		Store store = new Store();
		
		IntVar[] persons = new IntVar[n];
		IntVar[] distances = new IntVar[prefs.length];
		IntVar[] failedPrefs = new IntVar[prefs.length];
		IntVar cost = new IntVar(store, "Cost ", 0, prefs.length);
		
		//Varje person instantieras
		for(int i = 0; i < n; i++) {
			persons[i] = new IntVar(store, "person " + i, 1, n);
		}
		
		for(int i = 0; i < prefs.length; i++) {
			distances[i] = new IntVar(store, "Distance to pref ", 1 , n);
			store.impose(new Distance(persons[prefs[i][0] - 1], persons[prefs[i][1] - 1], distances[i]));
		}
		
		for(int i = 0; i < prefs.length; i++) {
			failedPrefs[i] = new IntVar(store, "Failed pref " + i + ": ", 0, 1);
		}
		
		for(int i = 0; i < distances.length; i++) {
			store.impose(new Reified(new XneqC(distances[i], 1), failedPrefs[i]));
		}
		
		//Samma person kan inte stå på flera ställen i bilden
		store.impose(new Alldifferent(persons));
		store.impose(new SumInt(failedPrefs, "==", cost));
		
		Search<IntVar> search = new DepthFirstSearch<IntVar>();
		//search.setPrintInfo(true);
		//search.setSolutionListener(new PrintOutListener());
		SelectChoicePoint<IntVar> select = new SimpleSelect<IntVar>(persons, null, new IndomainMin<IntVar>());
		
		boolean result = search.labeling(store, select, cost);
		
		if(result) {
			for(int i = 0; i < persons.length; i++) {
				System.out.println(persons[i]);
			}
			System.out.println(cost);
		} else {
			System.out.println("No solution.");
		}

	}

}
