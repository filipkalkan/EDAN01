
public class DataSet1 {

	// Delay for add and mul [clk cycles]
	public int del_add = 1;
	public int del_mul = 2;

	// Number of adding and mul machines
	public int number_add = 1;
	public int number_mul = 1;

	// Number of operations
	public int n = 28;

	// Last operations
	public int[] last = { 27, 28 };

	public int[] add = { 9, 10, 11, 12, 13, 14, 19, 20, 25, 26, 27, 28 };

	public int[] mul = { 1, 2, 3, 4, 5, 6, 7, 8, 15, 16, 17, 18, 21, 22, 23, 24 };

	//One tuple for each operation. n = nbr of operations and dependancies.
	public int[][] dependencies = { { 9 }, { 9 }, { 10 }, { 10 }, { 11 }, { 11 }, { 12 }, { 12 }, { 27 }, { 28 },
			{ 13 }, { 14 }, { 16, 17 }, { 15, 18 }, { 19 }, { 19 }, { 20 }, { 20 }, { 22, 23 }, { 21, 24 }, { 25 },
			{ 25 }, { 26 }, { 26 }, { 27 }, { 28 }, {}, {}, };
}