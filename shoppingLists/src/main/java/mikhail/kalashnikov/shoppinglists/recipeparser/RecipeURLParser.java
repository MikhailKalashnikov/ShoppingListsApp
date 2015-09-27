package mikhail.kalashnikov.shoppinglists.recipeparser;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

public class RecipeURLParser {
	public enum CheckUrlResult {
		OK, URL_NOT_SUPPORTED, WRONG_URL
	}

	public static void main(String[] args) throws IOException,UnsupportedWebSite {
        RecipeURLParser h = new RecipeURLParser();
		h.parse("www.jamieoliver.com/recipes/vegetables-recipes/bbq-baked-beans/");
		h.parse("http://allrecipes.com/Recipe/Creamy-Veggie-Fondue/Detail.aspx?evt19=1&referringHubId=433");
		h.parse("http://www.recipe.com/homemade-queso-fresco");
		h.parse("http://www.food.com/recipe/kofta-bi-tahini-kofta-with-tahini-90416");
		h.parse("http://www.gotovim.ru/recepts/salad/kalmary/1286.shtml"); 
		h.parse("http://gotovim-doma.ru/view.php?r=983-recept-Pasta-s-tuntsom");
		h.parse("http://eda.ru/salad/recipe16922/salat-iz-krasnoj-fasoli-s-tvorozhnim-sirom-krasnim-lukom-sezonnim-salatom");
		h.parse("andychef.ru/recipes/lasagna/");
		System.out.println("End!");
	}

	public static CheckUrlResult checkUrl(String urlStr) {
		String resUrl = urlStr;
		if (resUrl.length() < 6 || !resUrl.substring(0, 5).equalsIgnoreCase("http:")) {
			resUrl = "http://" + resUrl;
		}
		URL url;
		String host;
		try {
			url = new URL(resUrl);
			host = url.getHost();
		} catch (MalformedURLException e) {
			return CheckUrlResult.WRONG_URL;
		}

		if (host.substring(0, 3).equalsIgnoreCase("www")) {
			host = host.substring(4);
		}

		if (host.equalsIgnoreCase("andychef.ru")
                || host.equalsIgnoreCase("jamieoliver.com")
			    || host.equalsIgnoreCase("allrecipes.com")
                || host.equalsIgnoreCase("recipe.com")
                || host.equalsIgnoreCase("gotovim.ru")
                || host.equalsIgnoreCase("gotovim-doma.ru")
                || host.equalsIgnoreCase("eda.ru")
                || host.equalsIgnoreCase("food.com")
                || host.equalsIgnoreCase("gastronom.ru")) {
			return CheckUrlResult.OK;
		} else {
			return CheckUrlResult.URL_NOT_SUPPORTED;
		}
	}


	public RecipeResult parse(String urlStr) throws IOException, UnsupportedWebSite {
		String resUrl = urlStr;
		if (resUrl.length() < 6 || !resUrl.substring(0, 5).equalsIgnoreCase("http:")) {
			resUrl = "http://" + resUrl;
		}
		URL url = new URL(resUrl);
		RecipeParser parser;
		String host = url.getHost();
		if (host.substring(0, 3).equalsIgnoreCase("www")) {
			host = host.substring(4);
		}

		if (host.equalsIgnoreCase("andychef.ru")) {
			parser = new RecipeParserAndychefRu();
		} else if (host.equalsIgnoreCase("jamieoliver.com")) {
			parser = new RecipeParserJamieoliver();
		} else if (host.equalsIgnoreCase("allrecipes.com")) {
			parser = new RecipeParserAllrecipes();
		} else if (host.equalsIgnoreCase("recipe.com")) {
			parser = new RecipeParserRecipecom();
		} else if (host.equalsIgnoreCase("gotovim.ru")) {
			parser = new RecipeParserGotovimRu();
		} else if (host.equalsIgnoreCase("gotovim-doma.ru")) {
			parser = new RecipeParserGotovimDomaRu();
		} else if (host.equalsIgnoreCase("eda.ru")) {
			parser = new RecipeParserEdaRu();
		} else if (host.equalsIgnoreCase("food.com")) {
			if (!urlStr.contains("?mode=")) {
				resUrl = urlStr + "?mode=metric";
			}
			parser = new RecipeParserFoodCom();
		} else if (host.equalsIgnoreCase("gastronom.ru")) {
            parser = new RecipeParserGastronomRu();
		} else {
			throw new UnsupportedWebSite("unsupported web site " + host);
		}
		Document doc = Jsoup.connect(resUrl).timeout(10000).get();

		return new RecipeResult(parser.parse(doc), parser.getTitle(doc));
	}

}
