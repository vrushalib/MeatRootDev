import java.util.Arrays;

public class Demo {
	
	public static void main(String[] args) throws Exception {
	    int[] set = {1,2,3,4,5};
	    processSubsets(set, 3);
	}
	
	
	static void process(int[] subset) {
	    System.out.println(Arrays.toString(subset));
	}


	static void processSubsets(int[] set, int k) {
	    int[] subset = new int[k];
	    processLargerSubsets(set, subset, 0, 0);
	}

	static void processLargerSubsets(int[] set, int[] subset, int subsetSize, int nextIndex) {
	    if (subsetSize == subset.length) {
	        process(subset);
	    } else {
	        for (int j = nextIndex; j < set.length; j++) {
	            subset[subsetSize] = set[j];
	            processLargerSubsets(set, subset, subsetSize + 1, j + 1);
	        }
	    }
	}
}