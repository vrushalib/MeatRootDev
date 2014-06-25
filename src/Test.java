import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

public class Test {
	public static void main(String args[]) {
		String[] elemArray = { "a", "b", "c", "d", "e" };
		int size = 3;
	//	generateSubsets(elemArray, size);
		ArrayList<Integer> list = new ArrayList();
		list.add(1); list.add(2); list.add(3); list.add(4); list.add(5);
		System.out.println(getSubsets2(list));
	}
	
	static ArrayList<ArrayList<Integer>> getSubsets2(ArrayList<Integer> set) {
	    ArrayList<ArrayList<Integer>> allsubsets =
	    new ArrayList<ArrayList<Integer>>();
	    int max = 1 << set.size();
	    for (int i = 0; i < max; i++) {
	        ArrayList<Integer> subset = new ArrayList<Integer>();
	        int k = i;
	        int index = 0;
	        while (k > 0) {
	            if ((k & 1) > 0) {
	                subset.add(set.get(index));
	            }
	            k >>= 1;
	            index++;
	        }
	        allsubsets.add(subset);
	    }
	    return allsubsets;
	}
	
	
	static ArrayList<ArrayList<Integer>> getSubsets(ArrayList<Integer> set, int index) {
	    ArrayList<ArrayList<Integer>> allsubsets;
	    if (set.size() == index) {
	        allsubsets = new ArrayList<ArrayList<Integer>>();
	        allsubsets.add(new ArrayList<Integer>()); // Empty set
	    } else {
	        allsubsets = getSubsets(set, index + 1);
	        int item = set.get(index);
	        ArrayList<ArrayList<Integer>> moresubsets =
	        new ArrayList<ArrayList<Integer>>();
	        for (ArrayList<Integer> subset : allsubsets) {
	            ArrayList<Integer> newsubset = new ArrayList<Integer>();
	            newsubset.addAll(subset); //
	            newsubset.add(item);
	            moresubsets.add(newsubset);
	        }
	        allsubsets.addAll(moresubsets);
	    }
	    return allsubsets;
	}

	private static void generateSubsets(String[] elemArray, int size) {
		int elementLength = elemArray.length;
		Set<String> setObj = null;
		String[] subElemArray = null;
		String frstElem = null;
		List<Set<String>> lstSet = new ArrayList<Set<String>>();
		int counter = 0;
		for (int i = 0; i <= (elementLength - size); i++) {
			frstElem = elemArray[i];
			counter = 0;
			for (int j = (i + 1); j <= (elementLength - (size - 1)); j++) {
				counter = 0;
				setObj = new TreeSet<String>();
				setObj.add(frstElem);
				while (counter < (size - 1)) {
					setObj.add(elemArray[j + counter]);
					counter++;
				}
				System.out.println("Set is::" + setObj);
				lstSet.add(setObj);
			}
		}
		System.out.println("Unique combination is::" + lstSet);
	}
}