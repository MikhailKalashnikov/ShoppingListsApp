package mikhail.kalashnikov.shoppinglists.recipeparser;

import java.util.List;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.jsoup.nodes.Document;


public abstract class RecipeParserCommon implements RecipeParser {
	private final static boolean PARSE_FIELDS = false;

	@Override
	public List<Ingredient> parse(Document doc) {
		return null;
	}

    protected static Ingredient extractIngredient(String str, String inqty) {
    	String name;
    	String qty = "1";
    	Ingredient.QtyType qty_type = Ingredient.QtyType.piece;
    	
    	if (PARSE_FIELDS){
	    	if (inqty == null) {
	    		Scanner sc = new Scanner(str);
				if (sc.hasNextInt()) {
					qty = sc.next();
					String tmp = sc.next();
					Ingredient.QtyType tmpQtyType = getQtyType(tmp);
					
					if (tmp.equalsIgnoreCase("x") && sc.hasNextInt()) {
						qty = qty + tmp + sc.next();
						tmp = sc.next();
						tmpQtyType = getQtyType(tmp);
						if (tmpQtyType == null) {
							if (sc.hasNext()) {
								name = (tmp + sc.nextLine()).trim();
							} else {
								name = tmp.trim();
							}
							
						} else { 
							qty_type = tmpQtyType;
							name = sc.nextLine().trim();
						}
					} else if (tmpQtyType == null) {
						if (sc.hasNext()) {
							name = (tmp + sc.nextLine()).trim();
						} else {
							name = tmp.trim();
						}
						
					} else { 
						qty_type = tmpQtyType;
						name = sc.nextLine().trim();
					}
					
				} else {
					name = str;
				}
	    	} else {
	    		Scanner sc = new Scanner(inqty);
	    		name = str;
	
	    		if (sc.hasNextInt()) {
					qty = sc.next();
					String tmp = sc.next();
					Ingredient.QtyType tmpQtyType = getQtyType(tmp);
					
					if (tmp.equalsIgnoreCase("x") && sc.hasNextInt()) {
						qty = qty + tmp + sc.next();
						tmp = sc.next();
						tmpQtyType = getQtyType(tmp);
						if (tmpQtyType == null) {
							if (sc.hasNext()) {
								name = (tmp + sc.nextLine()).trim() + " " +  name;
							} else {
								name = tmp.trim() + " " +  name;
							}
							
						} else { 
							qty_type = tmpQtyType;
						}
					} else if (tmpQtyType == null) {
						if (sc.hasNext()) {
							name = (tmp + sc.nextLine()).trim() + " " +  name;
						} else {
							name = tmp.trim() + " " +  name;
						}
						
					} else { 
						qty_type = tmpQtyType;
					}
					
				} else {
					name = inqty + " " + name;
				}
	    		
	    	}
    	} else {
    		if (inqty == null) {
    			name = str;
    		} else {
    			name =inqty + " " + str;
    		}
    					
    	}
        return new Ingredient(name, qty, qty_type);
    }
    
    private static Ingredient.QtyType getQtyType(String str){
		if (str.equalsIgnoreCase("g") || str.equalsIgnoreCase("gr")) {
			return Ingredient.QtyType.gramm;
			
		} else if (str.equalsIgnoreCase("ml")) {
			return Ingredient.QtyType.ml;
		
		} else if (str.equalsIgnoreCase("pinch")) {
			return Ingredient.QtyType.pinch;
			
		} else if (str.equalsIgnoreCase("teaspoon") || str.equalsIgnoreCase("teaspoons")) {
			return Ingredient.QtyType.teaspoon;
		
		} else if (str.equalsIgnoreCase("tablespoons") || str.equalsIgnoreCase("tbsp") || str.equalsIgnoreCase("tablespoon")) {
			return Ingredient.QtyType.tablespoons;
			
		} else if (str.equalsIgnoreCase("slices")) {
			return Ingredient.QtyType.slices;
			
		} else if (str.equalsIgnoreCase("package")) {
			return Ingredient.QtyType.piece;	
	
		} else if (str.equalsIgnoreCase("cup")) {
			return Ingredient.QtyType.cup;
			
		} else {
			return null;
		}

    }

	@Override
	public String getTitle(Document doc) {
		String title = doc.title();
		title = title
				.replace("recipe", "")
				.replace("Recipe", "")
				.replace("рецепт", "")
				.replace("Рецепт:", "")
				.replace("Рецепт", "")
				.trim();
		String pattern = "(\\w|\\s)*";
		Pattern r = Pattern.compile(pattern);
		Matcher m = r.matcher(title);
		if (m.find()) {
			String tmp = m.group(0).trim();
			if (tmp.length() == 0) {
				String pattern2 = "(\\p{L}|\\s)*";
				r = Pattern.compile(pattern2);
				m = r.matcher(title);
				if (m.find()) {
					return m.group(0).trim();
				} else {
					return title;
				}
			} else {
				return tmp;
			}
		} else {
			return title;
		}

	}


}
