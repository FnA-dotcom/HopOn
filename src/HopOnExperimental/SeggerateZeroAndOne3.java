package HopOnExperimental;

public class SeggerateZeroAndOne3 {

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

    // function to print segregated array
    static void print(int arr[], int n) {
        System.out.print("Array after segregation is ");
        for (int i = 0; i < n; i++)
            System.out.print(arr[i] + " ");
    }

    static int findMinSwaps(int arr[], int n) {
        // Array to store count of zeroes
        int noOfZeroes[] = new int[n];
        int i, count = 0;

        // Count number of zeroes
        // on right side of every one.
        //noOfZeroes[n - 1] = 1 - arr[n - 1];
        for (i = n - 2; i >= 0; i--) {
            noOfZeroes[i] = noOfZeroes[i + 1];
            if (arr[i] == 0)
                noOfZeroes[i]++;
        }

        // Count total number of swaps by adding number
        // of zeroes on right side of every one.
        for (i = 0; i < n; i++) {
            if (arr[i] == 1)
                count += noOfZeroes[i];
        }
        return count;
    }

    // driver function
    public static void main(String[] args) {
        int ar[] = {1, 1, 1, 1, 0, 0, 0, 0};
        System.out.println(findMinSwaps(ar, ar.length));

/*        int arr[] = new int[]{ 0, 1, 0, 1, 1, 1 };
        int n = arr.length;

        segregate0and1(arr, n);
        print(arr, n);*/

    }
}


/*
    int  size = arr.size();
    int x = 0;
    int moves = 0;

        for(int i =0; i < size ; i++){
        if(arr.get(i) == 0)
        x++;
        }

        for(int i = 0; i < x; i++){
        arr.set(i, 0);
        moves++;
        }

        for(int i = x; i < size;i++){
        arr.set(i, 1);
        moves++;
        }
        return moves;*/
