package mikhail.kalashnikov.shoppinglists.recipeparser;

import java.util.ArrayList;
import java.util.List;

import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

public class RecipeParserRecipecom extends RecipeParserCommon {

	@Override
	public List<Ingredient> parse(Document doc) {
		List<Ingredient> listIng = new ArrayList<>();
		for (Element el : doc.select("div.gs_item_rowPrint div.floatleft.ingredient")) {
			//			System.out.println(el.text());
			listIng.add(extractIngredient(el.text(), null));
		}

		return listIng;
	}

}
