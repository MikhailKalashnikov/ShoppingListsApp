package mikhail.kalashnikov.shoppinglists.recipeparser;

import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class RecipeParserGastronomRu extends RecipeParserCommon {
    @Override
    public List<Ingredient> parse(Document doc) {
        Iterator<Element> itEl = doc.select("div.ingridients li").iterator();
        List<Ingredient> listIng = new ArrayList<>();
        while (itEl.hasNext()) {
            Element el = itEl.next();
            //System.out.println(el.text());
            listIng.add(extractIngredient(el.text(), null));
        }


        return listIng;
    }
}
