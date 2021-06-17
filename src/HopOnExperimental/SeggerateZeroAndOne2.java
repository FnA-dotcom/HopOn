package HopOnExperimental;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

public class SeggerateZeroAndOne2 {

    static void segregate0and1(int arr[], int n) {
        int count = 0; // counts the no of zeros in arr

        for (int i = 0; i < n; i++) {
            if (arr[i] == 0)
                count++;
        }

        // loop fills the arr with 0 until count
        for (int i = 0; i < count; i++)
            arr[i] = 0;

        // loop fills remaining arr space with 1
        for (int i = count; i < n; i++)
            arr[i] = 1;
    }

    public static int minMoves(List<Integer> arr) {
        // Write your code here
        int size = arr.size();
        int x = 0;
        int moves = 0;

        for (int i = 0; i < size; i++) {
            if (arr.get(i) == 0)
                x++;
        }

        for (int i = 0; i < x; i++) {
            arr.set(i, 0);
            moves++;
        }

        for (int i = x; i < size; i++) {
            arr.set(i, 1);
            moves++;
        }
        return moves;
    }

    // function to print segregated array
    static void print(int arr[], int n) {
        System.out.print("Array after segregation is ");
        for (int i = 0; i < n; i++)
            System.out.print(arr[i] + " ");
    }

    // driver function
    public static void main(String[] args) throws IOException {
        BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(System.in));
//        BufferedWriter bufferedWriter = new BufferedWriter(new FileWriter(System.getenv("OUTPUT_PATH")));

        int arrCount = Integer.parseInt(bufferedReader.readLine().trim());
//
        List<Integer> arr1 = new ArrayList<>();

        for (int i = 0; i < arrCount; i++) {
            int arrItem = Integer.parseInt(bufferedReader.readLine().trim());
            arr1.add(arrItem);
        }


        int result = SeggerateZeroAndOne2.minMoves(arr1);
        System.out.println(result);

        //bufferedWriter.write(String.valueOf(result));
        //bufferedWriter.newLine();

        bufferedReader.close();
        //bufferedWriter.close();

//        int res = SeggerateZeroAndOne2.minMoves(arr1);
//        System.out.println(res);
        // segregate0and1(arr, n);
        // print(arr, n);

    }
}
