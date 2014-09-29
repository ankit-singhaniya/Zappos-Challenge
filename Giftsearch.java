/* Created for an Entry Level Challlenge for Zappos Summer Internship 2015.
 */
package giftsearch;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Locale;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.json.Json;
import javax.json.JsonArray;
import javax.json.JsonObject;
import javax.json.JsonReader;


/**
 * @author Ankit Singhaniya
 */
public class GiftSearch {
    private final Locale currentLocale = Locale.getDefault();
    private float epsilon = Float.MAX_VALUE;
    private static final String PRICE_REGEX = "^\\$?(([1-9][0-9]{0,2}(,[0-9]{3})*)|[0-9]+)?\\.[0-9]{1,2}$";
    private static final Pattern pattern;

    static {
        pattern = Pattern.compile(PRICE_REGEX);
    }

    /** Main Function (Entry Point to User Application)
     * @param args the command line arguments
     * @throws java.io.IOException
     */
    public static void main(String[] args) throws IOException {
        // TODO code application logic here
        GiftSearch search = new GiftSearch();
        Scanner scan = new Scanner(System.in);
        System.out.print("Enter the Quantity of Items in Gift: ");
        int quantity = scan.nextInt();
        System.out.print("Enter the Total Value of Gift: ");
        float price = scan.nextFloat();
        search.getGiftSuggestions(price, quantity);
    }

    public void getGiftSuggestions(float price, int quantity) throws IOException {
        if (quantity <= 0 || price <= 0) {
            throw new IllegalArgumentException("Quantity and Price should be Positive Value");
        }
        ArrayList products = new ArrayList<>();

        //TODO: Adding Filter for Item Price < The Input Price.
        //TODO: Restrict The Queried Fields only to ProductId, StyleId, Price, Product Name.
        //TODO: Using JSON Objects on the result set.
        //TODO: Sort Addtionally by preference/relevance.
        URL url = new URL("http://api.zappos.com/Search?term=&sort={\"price\":\"desc\"}&key=52ddafbe3ee659bad97fcce7c53592916a6bfd73");
        try (InputStream is = url.openStream();
                JsonReader rdr = Json.createReader(is);) {
            JsonObject obj = rdr.readObject();
            JsonArray results = obj.getJsonArray("results");

            // check if statusCode is 200
            int statusCode = Integer.parseInt(obj.getString("statusCode"));
            if (statusCode == 200) {
                for (JsonObject result : results.getValuesAs(JsonObject.class)) {
                    float cost = getCurrencyValue(result.getString("price"));
                    if (cost < price) {
                        products.add(new Product(result.getString("productId"), result.getString("styleId"), result.getString("productName"), cost));
                    }
                }

                //Find Solution with Minimum Slack
                //Could be done much better given Slack Value (Definite)
                //A variation of approximation Subset Sum Algorithm can be implemented.
                //Present Solution searches brute force for the best possible solution. (Issue with Scalability, Runtime).
                //Dynamic Programming may also not be used unless value of prices are known to be restricted. (Memory Limitaions).
                ArrayList<Product> solution = findSolution(products, quantity, price, new ArrayList<Product>(), 0);
                if (solution != null) {
                    System.out.println("Found Solution as close as " + getCurrency(epsilon) + " from given price");
                    for (int i = 0; i < solution.size(); i++) {
                        System.out.println("Product:\t" + i);
                        System.out.println("ProductId:\t" + solution.get(i).getId());
                        System.out.println("StyleId:\t" + solution.get(i).getStyleId());
                        System.out.println("ProductName:\t" + solution.get(i).getName());
                        System.out.println("Price:\t" + getCurrency(solution.get(i).getPrice()));
                        System.out.print("\n");
                    }
                } else {
                    System.out.println("No Solution exists for given constraints");
                }
            } else {
                System.out.println("Error: " + statusCode + "\nCommunicating with the server failed.");
            }

        }
    }

    private ArrayList<Product> findSolution(ArrayList<Product> products, int quantity, float price, ArrayList<Product> selectedProducts, int x) {
        if (quantity == 0 && Math.abs(price) < epsilon) {
            epsilon = Math.abs(price);
            return (ArrayList<Product>) selectedProducts.clone();
        } else if (quantity == 0) {
            return null;
        } else if (price > 0
                && quantity > 0) {
            for (int i = x; i < products.size(); i++) {
                Product p = products.get(i);
                selectedProducts.add(p);
                ArrayList<Product> solution1 = findSolution(products, quantity - 1, price - p.getPrice(), selectedProducts, i + 1);
                float e1 = this.epsilon;
                selectedProducts.remove(p);
                ArrayList<Product> solution2 = findSolution(products, quantity, price, selectedProducts, i + 1);
                float e2 = this.epsilon;
                if (solution1 != null && solution2 != null) {
                    if (e1 < e2) {
                        return solution1;
                    } else {
                        return solution2;
                    }
                } else {
                    if (solution1 != null) {
                        return solution1;
                    } else if (solution2 != null) {
                        return solution2;
                    } else {
                        return null;
                    }
                }
            }
        }
        return null;
    }

    private float getCurrencyValue(String strPrice) {
        Matcher matcher = pattern.matcher(strPrice);
        while (matcher.find()) {
            strPrice = matcher.group(1).replace(",", "");
        }
        return Float.parseFloat(strPrice);
    }

    private String getCurrency(float amount) {
        NumberFormat currencyFormatter = NumberFormat.getCurrencyInstance(currentLocale);
        return currencyFormatter.format(amount);
    }
}
