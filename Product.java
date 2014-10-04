/* Created for an Entry Level Challenge for Zappos Summer Internship 2015.
 */

import java.io.Serializable;

/**
 *
 * @author Ankit Singhaniya
 * @University of Southern California
 */
public class Product implements Serializable {

    private String id = null;
    private String name = null;
    private String styleId = null;
    private float cost = 0;

    public Product(String id, String styleId, String name, float cost) {
        this.styleId = styleId;
        this.id = id;
        this.name = name;
        this.cost = cost;
    }

    public String getStyleId() {
        return styleId;
    }

    public void setStyleId(String styleId) {
        this.styleId = styleId;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public float getPrice() {
        return cost;
    }

    public void setPrice(float price) {
        this.cost = price;
    }

    @Override
    public boolean equals(Object r) {
        return (((Product) r).getId() == this.getId() && ((Product) r).getStyleId() == this.getStyleId());
    }

    @Override
    public String toString() {
        return new StringBuffer(" Cost : ")
                .append(this.cost)
                .append(" ProductId : ")
                .append(this.id)
                .append(" StyleId : ")
                .append(this.styleId)
                .append(" Name : ")
                .append(this.name).toString();
    }
}
