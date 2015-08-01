package mikhail.kalashnikov.shoppinglists.recipeparser;

import java.util.ArrayList;
import java.util.List;

import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

public class RecipeParserAndychefRu extends RecipeParserCommon {

	@Override
	public List<Ingredient> parse(Document doc) {
		List<Ingredient> listIng = new ArrayList<>();

		for (Element el : doc.select("ul.ox_list_animated li")) {
			//			System.out.println(el.text());
			listIng.add(extractIngredient(el.text(), null));
		}
		
		
		return listIng;
	}

}
