package mikhail.kalashnikov.shoppinglists;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

import mikhail.kalashnikov.shoppinglists.recipeparser.Ingredient;
import mikhail.kalashnikov.shoppinglists.recipeparser.RecipeResult;
import mikhail.kalashnikov.shoppinglists.recipeparser.RecipeURLParser;

public class AddRecipe extends AppCompatActivity
        implements SelectRecipeParamsDialog.AddRecipeToListDialogListener,
            ModelFragment.ModelCallbacks{
    private final String TAG = getClass().getSimpleName();
    private Button mAddRecipe;
    private ProgressBar mProgressBar;
    private EditText mRecipeUrl;
    private IngredientListAdapter mAdapter;
    private String mRecipeName;
    private String mDflCategoryName;
    private String mDflListName;
    private boolean mUseDefaultListAndCategory;
    private boolean mUseRecipeName;
    private boolean mUseCayegory;
    private static final String MODEL="model";
    private boolean mFromMainActivity;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Intent intent = getIntent();
        ModelFragment model = null;
        if (getSupportFragmentManager().findFragmentByTag(MODEL)==null) {
            model = new ModelFragment();
            getSupportFragmentManager().beginTransaction()
                    .add(model, MODEL)
                    .commit();
        }else{
            model = (ModelFragment)getSupportFragmentManager().findFragmentByTag(MODEL);
        }

        String url = null;
        if (intent != null && intent.getAction() != null
                && intent.getAction().equals("android.intent.action.SEND")) {
            url = getIntent().getStringExtra(Intent.EXTRA_TEXT);
            mFromMainActivity = false;
        } else {
            mFromMainActivity = true;
        }
        readPreferences();
        setContentView(R.layout.activity_add_recipe);
        Toolbar toolbar = (Toolbar) findViewById(R.id.recipe_toolbar);
        mRecipeUrl = (EditText) findViewById(R.id.recipe_url);
        if (url != null) {
            mRecipeUrl.setText(url);
        }
        Button parseRecipe = (Button) findViewById(R.id.btn_get_recipe);

        parseRecipe.setOnClickListener(new View.OnClickListener() {
                                           @Override
                                           public void onClick(View v) {
                                               if (mRecipeUrl.getText().toString().trim().length() > 0) {
                                                   parseRecipe(mRecipeUrl.getText().toString().trim());
                                               }
                                           }
                                       }
        );

        setSupportActionBar(toolbar);
        mProgressBar = (ProgressBar) findViewById(R.id.progressBar_recipe);
        mAddRecipe = (Button) findViewById(R.id.btn_add_recipe);
        mAddRecipe.setOnClickListener(new View.OnClickListener() {
                                          @Override
                                          public void onClick(View v) {
                                              addRecipe();
                                          }
                                      }
        );

        List<Ingredient> ingredients = new ArrayList<>();
        mAdapter = new IngredientListAdapter(ingredients);
        final ListView listView = (ListView) findViewById(R.id.recipe_list);

        listView.setAdapter(mAdapter);
        listView.setChoiceMode(ListView.CHOICE_MODE_SINGLE);

        SwipeDismissListViewTouchListener touchListener =
                new SwipeDismissListViewTouchListener(
                        listView,
                        new SwipeDismissListViewTouchListener.OnDismissCallback() {
                            @Override
                            public void onDismiss(ListView listView, int[] reverseSortedPositions) {
                                for (int position : reverseSortedPositions) {
                                    mAdapter.remove(mAdapter.getItem(position));

                                }
                            }
                        });
        listView.setOnTouchListener(touchListener);
        // Setting this scroll listener is required to ensure that during ListView scrolling,
        // we don't look for swipes.
        listView.setOnScrollListener(touchListener.makeScrollListener());

    }

    private void readPreferences() {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        mUseDefaultListAndCategory = prefs.getBoolean(SettingsActivity.KEY_PREF_RECIPE_USE_DEFAULT, false);
        mUseRecipeName = prefs.getBoolean(SettingsActivity.KEY_PREF_RECIPE_USE_RECIPE_NAME_FOR_LIST, true);
        mUseCayegory = prefs.getBoolean(SettingsActivity.KEY_PREF_USE_CATEGORY, true);
        mDflCategoryName = prefs.getString(
                SettingsActivity.KEY_PREF_RECIPE_DEFAULT_CATEGORY,
                getString(R.string.pref_recipe_default_category_value));
        mDflListName = prefs.getString(
                SettingsActivity.KEY_PREF_RECIPE_DEFAULT_LIST,
                getString(R.string.pref_recipe_default_list_value));
    }

    private void addRecipe() {
        if (mUseDefaultListAndCategory) {
            if (mUseRecipeName && mRecipeName != null) {
                insertRecipeAndFinish(mRecipeName, mRecipeName, mDflCategoryName);
            } else {
                insertRecipeAndFinish(mRecipeName, mDflListName, mDflCategoryName);
            }

        } else {
            SelectRecipeParamsDialog dialog = SelectRecipeParamsDialog.newInstance(
                    mRecipeName, mDflCategoryName, mUseCayegory);
            dialog.show(getSupportFragmentManager(), "SelectRecipeParamsDialog");
        }
    }

    private void parseRecipe(String url) {
        InputMethodManager imm= (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(mRecipeUrl.getWindowToken(), 0);
        if (url.length() > 5 && url.contains(".") && url.contains("/")) {
            switch (RecipeURLParser.checkUrl(url)) {
                case OK:
                    mAddRecipe.setEnabled(false);
                    mAdapter.clear();
                    mProgressBar.setVisibility(View.VISIBLE);
                    ModelFragment.executeAsyncTask(new ParseRecipeTask(), url);
                    break;
                case URL_NOT_SUPPORTED:
                    Toast.makeText(AddRecipe.this, R.string.parse_recipe_unsupported_url, Toast.LENGTH_LONG).show();
                    Log.w(TAG, "ParseRecipe URL_NOT_SUPPORTED");
                    break;
                case WRONG_URL:
                    Toast.makeText(AddRecipe.this, R.string.parse_recipe_error, Toast.LENGTH_LONG).show();
                    Log.w(TAG, "ParseRecipe WRONG_URL");
                    break;
            }

        } else {
            Toast.makeText(this, R.string.parse_recipe_not_valid_url, Toast.LENGTH_LONG).show();
        }

    }

    @Override
    public void onListAndCategorySelected(String listName, String categoryName, boolean useRecipeName, boolean useAsDefault) {
        if (useAsDefault) {
            SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(this);
            mUseDefaultListAndCategory = true;
            sp.edit().putBoolean(SettingsActivity.KEY_PREF_RECIPE_USE_DEFAULT, true).commit();

            if (useRecipeName != mUseRecipeName) {
                mUseRecipeName = useRecipeName;
                sp.edit().putBoolean(SettingsActivity.KEY_PREF_RECIPE_USE_RECIPE_NAME_FOR_LIST, useRecipeName).commit();
            }

            if (!mUseRecipeName && !mDflListName.equals(listName)) {
                mDflListName = listName;
                sp.edit().putString(SettingsActivity.KEY_PREF_RECIPE_DEFAULT_LIST, listName).commit();
            }
            if (mDflCategoryName.equals(categoryName)) {
                mDflCategoryName = categoryName;
                sp.edit().putString(SettingsActivity.KEY_PREF_RECIPE_DEFAULT_CATEGORY, categoryName).commit();
            }
        }

        insertRecipeAndFinish(mRecipeName, listName, categoryName);

    }

    private void insertRecipeAndFinish(String recipeName, String listName, String categoryName) {
        DataModel.getInstance(getApplicationContext()).insertRecipeAsync(
                recipeName, listName, categoryName, mAdapter.getItems());
        if (!mFromMainActivity) {
            startActivity(new Intent(this, ShoppingListsActivity.class));
        }
        finish();
    }

    @Override
    public void onUploadData(List<ShoppingList> shoppingLists) {

    }

    private class ParseRecipeTask extends AsyncTask<String, Void, RecipeResult> {

        @Override
        protected RecipeResult doInBackground(String... params) {
            RecipeURLParser parser = new RecipeURLParser();
            try {
                return parser.parse(params[0]);
            } catch (Exception e) {
                Log.e(TAG, e.getMessage(), e);
            }
            return null;
        }

        @Override
        protected void onPostExecute(RecipeResult result) {
            if (result == null
                    || result.getIngredients() == null
                    || result.getIngredients().size() == 0) {
                Toast.makeText(AddRecipe.this, R.string.parse_recipe_error, Toast.LENGTH_LONG).show();
            } else {
                for (Ingredient i : result.getIngredients()) {
                    mAdapter.add(i);
                }
                mRecipeName = result.getTitle();

                mAddRecipe.setEnabled(true);
            }
            mProgressBar.setVisibility(View.GONE);
        }

    }

    class IngredientListAdapter extends ArrayAdapter<Ingredient> {
        private final List<Ingredient> mItems;


        IngredientListAdapter(List<Ingredient> ingredients) {
            super(AddRecipe.this, android.R.layout.simple_list_item_1,android.R.id.text1, ingredients);
            mItems = ingredients;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            View row = super.getView(position, convertView, parent);
            ViewHolder holder = (ViewHolder) row.getTag();

            if(holder==null){
                holder = new ViewHolder(row);
                row.setTag(holder);
            }
            Ingredient item = getItem(position);
            holder.name.setText(item.getName());

            return row;
        }

        public List<Ingredient> getItems() {
            return mItems;
        }
    }

    class ViewHolder{
        TextView name=null;
        ViewHolder(View row){
            name=(TextView) row.findViewById(android.R.id.text1);
        }
    }

}
