import org.jacop.*;
import org.jacop.constraints.*;
import org.jacop.core.IntVar;
import org.jacop.core.Store;
import org.jacop.search.*;
 
public class UrbanPlanning {
 
    public static void main(String[] args) {
        // DATASET 1
      int n = 5;
      int n_commercial = 13;
      int n_residential = 12;
      int[] point_distribution = { -5, -4, -3, 3, 4, 5 };
 
//        // DATASET 2
//      int n = 5;
//      int n_commercial = 7;
//      int n_residential = 18;
//      int[] point_distribution =
//      {-5, -4, -3, 3, 4, 5};
 
//        // DATASET 3
//        int n = 7;
//        int n_commercial = 20;
//        int n_residential = 29;
//        int[] point_distribution = { -7, -6, -5, -4, 4, 5, 6, 7 };
 
        Store store = new Store();
 
        IntVar cost = new IntVar(store, 1, 59);
 
        IntVar[][] grid = new IntVar[n][n];
        IntVar[] cols = new IntVar[n];
        IntVar[] rows = new IntVar[n];
 
        IntVar[] allRowsHouses = new IntVar[n];
        IntVar[] allColsHouses = new IntVar[n];
 
        IntVar[] rowHouseIndex = new IntVar[n];
        IntVar[] colHouseIndex = new IntVar[n];
 
        IntVar[] rowsScore = new IntVar[n];
        IntVar[] colsScore = new IntVar[n];
 
        IntVar totRowScore = new IntVar(store, "sumRows:", -100, 100);
        IntVar totColScore = new IntVar(store, "sumRows:", -100, 100);
 
        IntVar totNbrHouses = new IntVar(store, "total nbr houses: ", 0, 100);
 
        IntVar negCost = new IntVar(store, "negative cost to minimize", -100, 100);
 
        // 0 represents store -- 1 represents house
        // Init all IntVars in grid
        for (int r = 0; r < n; r++) {
            for (int c = 0; c < n; c++) {
                grid[r][c] = new IntVar(store, "row:" + (r + 1) + ", column:" + (c + 1), 0, 1);
            }
 
            // Init all IntVar[] with n elements
            allRowsHouses[r] = new IntVar(store, "sum rows:", -n, n);
            allColsHouses[r] = new IntVar(store, "sum cols:", -n, n);
            
            rows[r] = new IntVar(store, "", -n, n);
            cols[r] = new IntVar(store, "", -n, n);
 
            rowsScore[r] = new IntVar(store, " rows score:", -n, n);
            colsScore[r] = new IntVar(store, "cols scores:", -n, n);
 
            rowHouseIndex[r] = new IntVar(store, " from point_distrubution get value :", 0, n);
        }
 
        // Summarize all elements in each row and put the sum in allRowsHouses
        for (int r = 0; r < n; r++) {
            store.impose(new SumInt(grid[r], "==", allRowsHouses[r]));
        }
 
        // Summarize all elements in each row and put the sum in allRowsHouses
        for (int c = 0; c < n; c++) {
            store.impose(new SumInt(getCol(grid, c, n), "==", allColsHouses[c]));
        }
 
        store.impose(new SumInt(allRowsHouses, "==", totNbrHouses));
        store.impose(new XeqC(totNbrHouses, n_residential));
 
        // Number of houses represents the index to fetch from point_distrubution
        rowHouseIndex = allRowsHouses;
        colHouseIndex = allColsHouses;
 
        for (int i = 0; i < n; i++) {
            store.impose(new ElementInteger(rowHouseIndex[i], point_distribution, rowsScore[i], -1));
            store.impose(new ElementInteger(colHouseIndex[i], point_distribution, colsScore[i], -1));
        }
 
        store.impose(new SumInt(rowsScore, "==", totRowScore));
        store.impose(new SumInt(colsScore, "==", totColScore));
 
        store.impose(new XplusYeqZ(totRowScore, totColScore, cost));
 
        store.impose(new XmulCeqZ(cost, -1, negCost));
 
        //Breaking symmetries - reduces search nodes significantly
        for (int i = 1; i < n - 1; i++) {
            store.impose(new LexOrder(getCol(grid, i, n), getCol(grid, i+1, n)));
            store.impose(new LexOrder(grid[i], grid[i + 1]));
        }
 
        // Prune and find solutions
        Search<IntVar> search = new DepthFirstSearch<IntVar>();
        SelectChoicePoint<IntVar> select = new SimpleMatrixSelect<IntVar>(grid, null, new IndomainMin<IntVar>());
 
        boolean result = search.labeling(store, select, negCost);
        if (result) {
            System.out.println("Total score: " + cost);
            StringBuilder sb = new StringBuilder();
            System.out.println("-----------------------");
            for (int r = 0; r < n; r++) {
                for (int c = 0; c < n; c++) {
                    sb.append(grid[r][c].value() + " ");
                }
                sb.append("\n");
            }
 
            System.out.println(sb);
            
            System.out.println("Nodes visited: " + search.getNodes());
            System.out.println("Wrong decisions: " + search.getWrongDecisions());
        } else {
            System.out.println("No solution found.");
        }
 
    }
 
    public static IntVar[] getCol(IntVar[][] grid, int i, int n) {
        IntVar[] col = new IntVar[n];

        for (int r = 0; r < n; r++) {
            col[r] = grid[r][i];
        }
        return col;
    }
 
}