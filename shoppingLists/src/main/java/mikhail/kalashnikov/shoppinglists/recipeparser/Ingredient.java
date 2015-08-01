package mikhail.kalashnikov.shoppinglists.recipeparser;

public class Ingredient {
	public enum QtyType {
		gramm, piece, ml, pinch,
		teaspoon,
		tablespoons,
		slices,cup
	}
	private String name;
	private String qty;
	private QtyType qty_type;
	
	public Ingredient(String name, String qty, QtyType qty_type) {
		super();
		this.name = name;
		this.qty = qty;
		this.qty_type = qty_type;
	}

	@Override
	public String toString() {
		return "Ingredient [name=" + name + ", qty=" + qty + ", qty_type="
				+ qty_type + "]" + System.getProperty("line.separator");
	}

	public String getName() {
		return name;
	}

	public String getQty() {
		return qty;
	}

	public QtyType getQty_type() {
		return qty_type;
	}
}
