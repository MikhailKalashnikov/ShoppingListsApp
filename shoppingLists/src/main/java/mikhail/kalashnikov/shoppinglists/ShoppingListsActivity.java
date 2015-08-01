package mikhail.kalashnikov.shoppinglists;

import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.preference.PreferenceManager;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.ActionBar;
import android.support.v4.app.FragmentManager;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.support.v4.widget.DrawerLayout;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import java.util.List;

public class ShoppingListsActivity extends AppCompatActivity
        implements NavigationDrawerFragment.NavigationDrawerCallbacks, ModelFragment.ShoppingListModelCallbacks,
            AddNewListDialog.AddNewListDialogListener, AddRecipeDialog.AddRecipeDialogListener{
    private final String TAG = getClass().getSimpleName();
    /**
     * Fragment managing the behaviors, interactions and presentation of the navigation drawer.
     */
    private NavigationDrawerFragment mNavigationDrawerFragment;

    /**
     * Used to store the last screen title. For use in {@link #restoreActionBar()}.
     */
    private CharSequence mTitle;

    private static final String MODEL="model";
    private ModelFragment mModel=null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if(LogGuard.isDebug) Log.d(TAG, "onCreate getSupportFragmentManager().findFragmentByTag(MODEL)==null " + (getSupportFragmentManager().findFragmentByTag(MODEL)==null));
        PreferenceManager.setDefaultValues(this, R.xml.preferences, false);

        if (getSupportFragmentManager().findFragmentByTag(MODEL)==null) {
            mModel = new ModelFragment();
            getSupportFragmentManager().beginTransaction()
                    .add(mModel, MODEL)
                    .commit();
        }else{
            mModel = (ModelFragment)getSupportFragmentManager().findFragmentByTag(MODEL);
        }

        setContentView(R.layout.main);

        mNavigationDrawerFragment = (NavigationDrawerFragment)
                getSupportFragmentManager().findFragmentById(R.id.navigation_drawer);
        mTitle = getTitle();

        // Set up the drawer.
        mNavigationDrawerFragment.setUp(
                R.id.navigation_drawer,
                (DrawerLayout) findViewById(R.id.drawer_layout));

        if(LogGuard.isDebug) Log.d(TAG, "onCreate End");
    }


    @Override
    public void onNavigationDrawerItemSelected(int position, long id, String name) {
        if(LogGuard.isDebug) Log.d(TAG, "onNavigationDrawerItemSelected position=" + position + ", id=" + id + ", name=" + name);
        if (position >= 0) {
            // update the main content by replacing fragments
            FragmentManager fragmentManager = getSupportFragmentManager();
            fragmentManager
                    .beginTransaction()
                    .replace(R.id.container, ListItemsFragment.newInstance(id), "ListItemsFragment")
                    .commit();
            mTitle = name;
        } else {
            FragmentManager fragmentManager = getSupportFragmentManager();
            fragmentManager.beginTransaction().remove(fragmentManager.findFragmentByTag("ListItemsFragment")).commit();

            mTitle = getTitle();
        }
    }

    private void restoreActionBar() {
        ActionBar actionBar = getSupportActionBar();
        actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_STANDARD);
        actionBar.setDisplayShowTitleEnabled(true);
        actionBar.setTitle(mTitle);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        if (!mNavigationDrawerFragment.isDrawerOpen()) {
            // Only show items in the action bar relevant to this screen
            // if the drawer is not showing. Otherwise, let the drawer
            // decide what to show in the action bar.
            getMenuInflater().inflate(R.menu.actions, menu);
            restoreActionBar();
            return true;
        }
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch(item.getItemId()){
            case R.id.mi_add_list:
                AddNewListDialog addNewlListDialog = AddNewListDialog.newInstance(null, AddNewListDialog.NEW_LIST_ID);
                addNewlListDialog.show(getSupportFragmentManager(), "AddNewListDialog");
                return true;
            case R.id.mi_add_recipe:
                openPickRecipeDialog();
                return true;
            case R.id.mi_settings:
                startActivity(new Intent(this, SettingsActivity.class));
                return true;
            case R.id.mi_about:
                getAboutDialog().show(getSupportFragmentManager(), "AboutDialog");
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onUploadData(List<ShoppingList> shoppingLists) {
        if(LogGuard.isDebug) Log.d(TAG, "onUploadData model.isDataUploaded()=" + mModel.getModel().isDataUploaded());
        findViewById(R.id.progressBar1).setVisibility(View.GONE);
        findViewById(R.id.container).setVisibility(View.VISIBLE);
        mNavigationDrawerFragment.setDrawerAdapter(shoppingLists);

    }

    @Override
    protected void onStart() {
        if(LogGuard.isDebug) Log.d(TAG, "onStart");
        super.onStart();
    }

    @Override
    protected void onResume() {
        if(LogGuard.isDebug) Log.d(TAG, "onResume");
        super.onResume();
    }

    @Override
    public void onListAdded(String name) {
        mModel.getModel().insertShoppingListAsync(name);
        mNavigationDrawerFragment.selectItem(mModel.getModel().getShoppingList().size() - 1);
    }

    @Override
    public void onListEdited(String name, long id) {
        mModel.getModel().updateShoppingListAsync(name, id);
        mTitle = name;
    }

    public void deleteList(long listId) {
        mModel.getModel().deleteShoppingListWithItemsAsync(listId);
    }

    public void editList(long listId, String name) {
        AddNewListDialog addNewlListDialog = AddNewListDialog.newInstance(
                name, listId);
        addNewlListDialog.show(getSupportFragmentManager(), "AddNewListDialog");
    }


    private void openPickRecipeDialog() {
        final List<Recipe> recipeLists = mModel.getModel().getRecipeList();

        if (recipeLists.size() == 0) {
            startActivity(new Intent(ShoppingListsActivity.this, AddRecipe.class));
        } else {
            DialogFragment d = new AddRecipeDialog();
            d.show(this.getSupportFragmentManager(), "PickRecipeDialog");
        }
    }

    @Override
    public void onAddRecipeToList(Recipe recipe) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        if (prefs.getBoolean(SettingsActivity.KEY_PREF_RECIPE_USE_RECIPE_NAME_FOR_LIST, true)) {
            mModel.getModel().addExistingRecipeToList(recipe, recipe.getName());
        } else {
            mModel.getModel().addExistingRecipeToList(recipe,
                    prefs.getString(
                            SettingsActivity.KEY_PREF_RECIPE_DEFAULT_LIST,
                            getString(R.string.pref_recipe_default_list_value)));
        }
    }

    private DialogFragment getAboutDialog() {
        return new DialogFragment() {
            @Override
            public Dialog onCreateDialog(Bundle savedInstanceState) {
                android.support.v7.app.AlertDialog.Builder builder = new android.support.v7.app.AlertDialog.Builder(getActivity());
                PackageInfo pInfo = null;
                try {
                    pInfo = getPackageManager().getPackageInfo(getPackageName(), 0);
                } catch (PackageManager.NameNotFoundException e) {
                    e.printStackTrace();
                }
                builder.setTitle(R.string.about_title)
                        .setMessage(getString(R.string.app_name) + " : "
                                + pInfo.versionName + "\n\n"
                                + getString(R.string.about_text
                        ))
                        .setNegativeButton(R.string.send_email, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                Intent intent = new Intent(Intent.ACTION_SEND);
                                intent.setType("text/html");
                                intent.putExtra(Intent.EXTRA_EMAIL, "mikkalashnikov@gmail.com");
                                intent.putExtra(Intent.EXTRA_SUBJECT, "ShoppingList App");

                                if (intent.resolveActivity(getPackageManager()) != null) {
                                    startActivity(Intent.createChooser(intent, "Send Email"));
                                }

                            }
                        })
                        .setPositiveButton(android.R.string.ok, null);

                return builder.create();
            }
        };
    }
}
