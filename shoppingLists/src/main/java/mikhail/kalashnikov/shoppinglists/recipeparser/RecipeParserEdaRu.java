package mikhail.kalashnikov.shoppinglists.recipeparser;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

public class RecipeParserEdaRu extends RecipeParserCommon {

	@Override
	public List<Ingredient> parse(Document doc) {
		List<Ingredient> listIng = new ArrayList<>();

        for (Element el : doc.select("tr.ingredient")) {
            //System.out.println(el.getElementsByClass("amount").text() + " - " + el.getElementsByClass("name").text());
            listIng.add(extractIngredient(el.getElementsByClass("name").text(), el.getElementsByClass("amount").text()));
        }
		
		return listIng;
	}

	@Override
	public String getTitle(Document doc) {
		String title = doc.title();
		String pattern = "«.*»";
		Pattern r = Pattern.compile(pattern);
		Matcher m = r.matcher(title);
		if (m.find()) {
			String tmp = m.group(0).trim();
			return tmp.substring(1, tmp.length()-1);
		} else {
			return super.getTitle(doc);
		}
	}
}
