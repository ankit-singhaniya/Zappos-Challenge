/* Created for an Entry Level Challlenge for Zappos Summer Internship 2015.
 */
package giftsearch;

/**
 *
 * @author Ankit
 */
public class Product {

    public Product(String id, String styleId, String name, float cost) {
        this.styleId = styleId;
        this.id  = id;
        this.name = name;
        this.cost = cost;        
    }

    public String getStyleId() {
        return styleId;
    }

    public void setStyleId(String styleId) {
        this.styleId = styleId;
    }

    public float getCost() {
        return cost;
    }

    public void setCost(float cost) {
        this.cost = cost;
    }
    
    private String id = null;
    private String name = null;
    private String styleId = null;
    private float cost = 0;

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
    
    
}
