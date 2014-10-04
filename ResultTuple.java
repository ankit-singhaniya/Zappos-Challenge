/* Created for an Entry Level Challenge for Zappos Summer Internship 2015.
 */

import java.util.ArrayList;

/**
 *
 * @author Ankit Singhaniya
 * @University of Southern California
 */
public class ResultTuple implements Comparable {

    public ResultTuple(ArrayList<Product> products, float priceDifference) {
        if (Float.isNaN(priceDifference)) {
            priceDifference = Float.MAX_VALUE;
        }
        this.priceDifference = priceDifference;
        this.products = products;
    }
    private float priceDifference = 0;

    public float getPriceDifference() {
        return priceDifference;
    }

    public void setPriceDifference(float priceDifference) {
        this.priceDifference = priceDifference;
    }

    public ArrayList<Product> getProducts() {
        return products;
    }

    public void setProducts(ArrayList<Product> products) {
        this.products = products;
    }
    private ArrayList<Product> products = null;

    @Override
    public int compareTo(Object t) {
        return Double.compare(this.priceDifference, ((ResultTuple) t).getPriceDifference());
    }

    @Override
    public boolean equals(Object r) {
        boolean eq = (((ResultTuple) r).getPriceDifference() == this.getPriceDifference());
        if (eq) {
            eq = (((((ResultTuple) r).getProducts()).containsAll(this.getProducts())) && (this.getProducts().containsAll(((ResultTuple) r).getProducts())));
        }
        return eq;
    }
}
