package mikhail.kalashnikov.shoppinglists.recipeparser;

import java.util.ArrayList;
import java.util.List;

import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

public class RecipeParserFoodCom extends RecipeParserCommon {

	@Override
	public List<Ingredient> parse(Document doc) {
		List<Ingredient> listIng = new ArrayList<>();
        for (Element el : doc.select("li[itemprop=ingredients]")) {
            //System.out.println(el.text());
            listIng.add(extractIngredient(el.text(), null));
        }
		
		return listIng;
	}

}
