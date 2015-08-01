package mikhail.kalashnikov.shoppinglists;

import android.provider.BaseColumns;

public class RecipeItem implements BaseColumns {
    public static final String TABLE_NAME = "recipe_item";
    public static final String COLUMN_RECIPE_ID = "recipe_id";
    public static final String COLUMN_ITEM_ID = "item_id";
    public static final String COLUMN_QTY = "qty";

    private long id;
    private Recipe recipe;
    private String qty;
    private Item item;

    public RecipeItem(long id, Recipe recipe, Item item, String qty) {
        super();
        this.id = id;
        this.recipe = recipe;
        this.item = item;
        if (item != null) {
            this.item.useItem();
        }
        this.qty = qty;
    }

    public RecipeItem(Recipe recipe, Item item, String qty) {
        super();
        this.recipe = recipe;
        this.item = item;
        if (item != null) {
            this.item.useItem();
        }
        this.qty = qty;
    }

    public void setId(long id){
        this.id=id;
    }
    public long getId() {
        return id;
    }
    public Item getItem() {
        return item;
    }

    public Recipe getRecipe() {
        return recipe;
    }

    public String getQty() {
        return qty;
    }
    public void setQty(String qty) {
        this.qty = qty;
    }
    @Override
    public String toString() {
        return "RecipeItem [id=" + id + ", recipe=" + recipe + ", item="
                + item + ", qty=" + qty + "]";
    }
}
