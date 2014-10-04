/* Created for an Entry Level Challlenge for Zappos Summer Internship 2015.
 */
package giftsearch;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Locale;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.json.Json;
import javax.json.JsonArray;
import javax.json.JsonObject;
import javax.json.JsonReader;

/**
 *
 * @author Ankit Singhaniya
 * @University of Southern California
 */
public class GiftSearch {

    //Increasing these parameters increases runtime, but also increases probability of getting better solutions.
    //Total Nmber of Random Attemps.
    private static final int NUMBER_OF_SOLUTIONS = 20000;

    //Total Number of API Calls (Number of Pages) with each call getting 100 products. (Should be maximum as possible)
    private static final int MAX_API_CALLS = 200;

    //For Larger Sets, we define a subset size for permutations based on the quantiy of items in gift.
    private static final double MIN_SET_SIZE_MULTIPLIER = 1;

    //Subset Size of Random Products defined on this Max Size Parameter.
    private static final int SOLUTION_PEEK_SET_SIZE = 100;

    //Other Configurable Parameters:
    //Number of Optimal Items required at output (If Available/Exists).
    private static final int NUMBER_OF_OPTIMAL_SOLUTIONS = 10;

    //Trim the products in the interested range. (Eliminate Relative Trailing Data)
    private static final int THRESHOLD_SET_SIZE = 5000;

    //Triming Data if the Price < Average Gift Price / Divisor.
    private static final double ACCEPTABLE_PRICE_DIVISOR = 5;

    //REST API CONSTANTS
    private static final String API_KEY = "52ddafbe3ee659bad97fcce7c53592916a6bfd73";
    private static final String SEARCH_API_URL = "http://api.zappos.com/Search?term=&limit=100&key=" + API_KEY;

    //Only Member Variable. Holds all products on which computations is performed.
    private ArrayList<Product> allProducts = new ArrayList<>();

    /**
     * Main Function (Entry Point to User Application)
     *
     * @param args the command line arguments Command Line Arguments: Quantity:
     * **Number of Items in the Gift. **Price: Total Value of the Gift
     *
     */
    public static void main(String[] args) {
        long startTime = getStartTime();
        GiftSearch search = new GiftSearch();
        //Defaulting to 10 Gifts, Total Value of $1,000.00
        int quantity = 20;
        float price = 5000;
        if (args != null && args.length > 0) {
            quantity = Integer.parseInt(args[0]);
            if (args.length > 1) {
                price = Float.parseFloat(args[1]);
            }
        }
        try {
            search.getGiftSuggestions(price, quantity);
        } catch (IOException ex) {
            Logger.getLogger(CostSearch.class.getName()).log(Level.SEVERE, null, ex);
        }
        printTimeElapsed("Total Execution Time: ", startTime);
    }

    public void getGiftSuggestions(float giftPrice, int quantity) throws IOException {
        if (quantity <= 0 || giftPrice <= 0) {
            throw new IllegalArgumentException("Quantity and Price should be Positive Value");
        }
        System.out.println("-- Getting Popular Products with price less than given gift price (API Calls)");
        long startTime = getStartTime();

        boolean moreData = true;
        int page = 0;
        while (moreData && page < MAX_API_CALLS) {
            URL url = new URL(SEARCH_API_URL + "&sort={\"productPopularity\":\"desc\"}&excludes=[\"originalPrice\", \"colorId\", \"brandName\", \"thumbnailImageUrl\", \"percentOff\", \"productUrl\"]&page=" + page);
            page++;
            try {
                InputStream is = url.openStream();
                JsonReader rdr = Json.createReader(is);
                JsonObject obj = rdr.readObject();
                JsonArray results = obj.getJsonArray("results");

                // check if statusCode is 200
                int statusCode = Integer.parseInt(obj.getString("statusCode"));
                if (statusCode == 200 && results != null && results.size() > 0) {
                    for (JsonObject result : results.getValuesAs(JsonObject.class)) {
                        float price = getCurrencyValue(result.getString("price"));
                        if (price <= giftPrice) {
                            allProducts.add(new Product(result.getString("productId"), result.getString("styleId"), result.getString("productName"), price));
                        }
                    }
                } else if (statusCode != 200) {
                    System.out.println("Error: " + statusCode + "\nCommunicating with the server failed.");
                    break;
                } else {
                    moreData = false;
                }
            } catch (IOException e) {
                System.out.println("Error: " + e.getLocalizedMessage() + "\nCommunicating with the server failed.");
            }
        }
        printTimeElapsed("-- Getting product prices finished in ", startTime);

        //Processing the prices to form gift sets
        System.out.println("-- Processing: " + allProducts.size() + " products to form gifts recommendations");
        int prevSize = allProducts.size();
        trimData(giftPrice, quantity);
        if (allProducts.size() != prevSize) {
            System.out.println("-- Trimmed Products to : " + allProducts.size() + " size\n");
        }

        if (allProducts.size() < quantity) {
            System.out.println("Error: Unable to find products with Given Constraints.");
            System.out.println("Exiting...");
        } else {
            int solutionSize = 0;
            ArrayList<ResultTuple> solutions = new ArrayList<>();
            startTime = getStartTime();

            //Only if it is computationally heavy to take subsets of this set.
            if (allProducts.size() > SOLUTION_PEEK_SET_SIZE) {
                int peekSetSize = SOLUTION_PEEK_SET_SIZE;
                peekSetSize = peekSetSize / quantity;
                if (peekSetSize <= quantity) {
                    peekSetSize = (int) (MIN_SET_SIZE_MULTIPLIER * quantity);
                }
                for (int i = 0; i < NUMBER_OF_SOLUTIONS; i++) {
                    Collections.shuffle(allProducts);
                    ResultTuple res = findSolution(peekSetSize, quantity, giftPrice);
                    if (res != null && !solutions.contains(res)) {
                        solutions.add(res);
                    }
                }
                solutionSize = NUMBER_OF_OPTIMAL_SOLUTIONS;
                if (solutions.size() < solutionSize) {
                    solutionSize = solutions.size();
                }
            } else {
                ResultTuple res = findSolution(allProducts.size(), quantity, giftPrice);
                if (res != null && !solutions.contains(res)) {
                    solutions.add(res);
                }
                solutionSize = 1;
            }

            if (solutions.isEmpty()) {
                System.out.println("Error: Insufficient resource to construct set of products with Given Constraints.");
                System.out.println("Exiting...");
            } else {
                System.out.println("-- Processing " + solutions.size() + " solutions complete");
                printTimeElapsed("-- Gift Set Generation took ", startTime);

                //Getting Products Associated with each Cost.
                solutions.trimToSize();

                //Print Solution on Terminal.
                printSolutions(solutions, solutionSize);
            }
        }
    }

    /*
     * Function to get the Subset of Prices that sums as close as possible to the given sum.
     * Restrictions on Sub-Set: Size o subset is fixed = Quantity
     * Total Cost To be As close as possible to price.
     */
    private ResultTuple findSolution(int problemSize, int quantity, float price) {
        ArrayList<Product> solution = new ArrayList<>();
        float minPrice = Float.MAX_VALUE;
        int[] permutation = initializePermutations(problemSize, quantity);
        do {
            int j = 0;
            float sum = 0;
            int i = 0;
            ArrayList<Product> prods = new ArrayList<>();
            for (; i < quantity; i++) {
                while (permutation[j] == 0) {
                    j++;
                }
                prods.add(allProducts.get(j));
                sum += allProducts.get(j).getPrice();
                j++;
                if (sum > price) {
                    break;
                }
            }
            if (i != quantity) {
                continue;
            }
            if (Math.abs(sum - price) < minPrice) {
                minPrice = Math.abs(sum - price);
                solution = (ArrayList<Product>) prods.clone();
            }
        } while (nextPermutation(permutation));
        if (minPrice != Float.MAX_VALUE) {
            return new ResultTuple(solution, minPrice);
        }
        return null;
    }

    private void trimData(float price, int quantity) {
        if (allProducts.size() > THRESHOLD_SET_SIZE) {
            float avgPrice = price / quantity;
            double acceptableMin = avgPrice / ACCEPTABLE_PRICE_DIVISOR;
            allProducts.removeIf(a -> a.getPrice() < acceptableMin);
        }
    }

    private void printSolutions(ArrayList<ResultTuple> solutions, int solutionSize) {
        if (solutions != null) {
            Collections.sort(solutions);
            for (int i = 0; i < solutionSize; i++) {
                System.out.println("GIFT SET No. " + (i + 1) + ", as close as " + getCurrency(solutions.get(i).getPriceDifference()) + " from given price");
                ArrayList<Product> products = solutions.get(i).getProducts();
                int k = 0;
                for (Product product : products) {
                    System.out.println("Gift Option: " + (i + 1) + "\tProduct No.: " + ++k);
                    System.out.println("ProductId:\t" + product.getId());
                    System.out.println("StyleId:\t" + product.getStyleId());
                    System.out.println("ProductName:\t" + product.getName());
                    System.out.println("Price:\t" + getCurrency(product.getPrice()));
                    System.out.print("\n");
                }
            }
        }
    }

    private int[] initializePermutations(int problemSize, int quantity) {
        int[] permutation = new int[problemSize];
        for (int i = 0; i < problemSize; i++) {
            permutation[i] = 0;
        }
        for (int i = 0; i < quantity; i++) {
            permutation[problemSize - 1 - i] = 1;
        }
        return permutation;
    }

    /**
     * Note that this method is not written by me. Source:
     * http://nayuki.eigenstate.org/page/next-lexicographical-permutation-algorithm
     * Computes the next lexicographical permutation of the specified array of
     * integers in place, returning whether a next permutation existed. (Returns
     * {@code false} when the argument is already the last possible
     * permutation.)
     *
     * @param array the array of integers to permute
     * @return whether the array was permuted to the next permutation
     */
    public static boolean nextPermutation(int[] array) {
        // Find non-increasing suffix
        int i = array.length - 1;
        while (i > 0 && array[i - 1] >= array[i]) {
            i--;
        }
        if (i <= 0) {
            return false;
        }

        // Find successor to pivot
        int j = array.length - 1;
        while (array[j] <= array[i - 1]) {
            j--;
        }
        int temp = array[i - 1];
        array[i - 1] = array[j];
        array[j] = temp;

        // Reverse suffix
        j = array.length - 1;
        while (i < j) {
            temp = array[i];
            array[i] = array[j];
            array[j] = temp;
            i++;
            j--;
        }
        return true;
    }

    private static float getCurrencyValue(String strPrice) {
        strPrice = strPrice.replaceAll("[$,]", "");
        float price = Float.parseFloat(strPrice);
        return price;
    }

    private static String getCurrency(float amount) {
        NumberFormat currencyFormatter = NumberFormat.getCurrencyInstance(Locale.US);
        return currencyFormatter.format(amount);
    }

    private static long getStartTime() {
        return System.currentTimeMillis();
    }

    private static void printTimeElapsed(String msg, long startTime) {
        long endTime = System.currentTimeMillis();
        long millis = endTime - startTime;
        String hms = String.format("%02d Hrs %02d Mins %02d Secs", TimeUnit.MILLISECONDS.toHours(millis),
                TimeUnit.MILLISECONDS.toMinutes(millis) - TimeUnit.HOURS.toMinutes(TimeUnit.MILLISECONDS.toHours(millis)),
                TimeUnit.MILLISECONDS.toSeconds(millis) - TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(millis)));
        System.out.println(msg + hms);
        System.out.println();
    }
}
