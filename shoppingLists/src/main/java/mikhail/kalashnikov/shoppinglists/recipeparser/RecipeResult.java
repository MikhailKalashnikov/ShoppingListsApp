package mikhail.kalashnikov.shoppinglists.recipeparser;

import java.util.List;

public class RecipeResult {
    private List<Ingredient> ingredients;
    private String title;

    public RecipeResult(List<Ingredient> ingredients, String title) {
        this.ingredients = ingredients;
        this.title = title;
    }

    public List<Ingredient> getIngredients() {
        return ingredients;
    }

    public String getTitle() {
        return title;
    }

    @Override
    public String toString() {
        return "RecipeResult {" +
                "ingredients=" + ingredients +
                ", title='" + title + '\'' +
                '}';
    }
}
