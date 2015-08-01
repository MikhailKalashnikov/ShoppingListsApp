package mikhail.kalashnikov.shoppinglists.recipeparser;
import java.util.List;
import org.jsoup.nodes.Document;

public interface RecipeParser {
    List<Ingredient> parse(Document doc);
    String getTitle(Document doc);
}
