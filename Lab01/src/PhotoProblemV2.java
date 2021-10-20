import org.jacop.constraints.*;
import org.jacop.core.*;
import org.jacop.search.*;

public class PhotoProblemV2 {

	public static void main(String[] args) {
		int n = 9;
		int n_prefs = 17;
		int[][] prefs = {{1,3}, {1,5}, {1,8},
		{2,5}, {2,9}, {3,4}, {3,5}, {4,1},
		{4,5}, {5,6}, {5,1}, {6,1}, {6,9},
		{7,3}, {7,8}, {8,9}, {8,7}};
		
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
		
		//Samma person kan inte stå på flera ställen i bilden
		store.impose(new Alldifferent(persons));
		
		store.impose(new Max(distances, cost));
		
		Search<IntVar> search = new DepthFirstSearch<IntVar>();
		search.setPrintInfo(true);
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
