public class DataSet3 {
	int del_add = 1;
	int del_mul = 2;

	int number_add = 1;
	int number_mul = 3;
	int n = 28;

	int[] last = { 27, 28 };

	int[] add = { 9, 10, 11, 12, 13, 14, 19, 20, 25, 26, 27, 28 };

	int[] mul = { 1, 2, 3, 4, 5, 6, 7, 8, 15, 16, 17, 18, 21, 22, 23, 24 };

	int[][] dependencies = { 
			{ 9 }, { 9 }, { 10 }, { 10 }, { 11 },
			{ 11 }, { 12 }, { 12 }, { 27 }, { 28 }, 
			{ 13 }, { 14 }, { 16, 17 }, { 15, 18 }, { 19 },
			{ 19 }, { 20 }, { 20 }, { 22, 23 }, { 21, 24 }, 
			{ 25 }, { 25 }, { 26 }, { 26 }, { 27 },
			{ 28 }, {}, {}, };
}
