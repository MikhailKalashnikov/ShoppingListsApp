package mikhail.kalashnikov.shoppinglists.recipeparser;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

public class RecipeParserAllrecipes extends RecipeParserCommon {

	@Override
	public List<Ingredient> parse(Document doc) {
		List<Ingredient> listIng = new ArrayList<>();
		
		Iterator<Element> itEl = doc.select("li#liIngredient .fl-ing").iterator();
		while (itEl.hasNext()) {
			Element el = itEl.next();
			//System.out.println(el.getElementById("lblIngAmount").text() + " - " + el.getElementById("lblIngName").text());
			listIng.add(extractIngredient(el.getElementById("lblIngName").text(), el.getElementById("lblIngAmount").text()));
		}
		
		return listIng;
	}

}
