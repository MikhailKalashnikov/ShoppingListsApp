package mikhail.kalashnikov.shoppinglists;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;

import android.support.v7.app.ActionBar;
import android.content.SharedPreferences;
import android.content.res.Configuration;

import android.preference.PreferenceManager;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.view.ActionMode;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import java.util.List;

public class NavigationDrawerFragment extends Fragment {
    private final String TAG = getClass().getSimpleName();
    /**
     * Remember the position of the selected item.
     */
    private static final String STATE_SELECTED_POSITION = "selected_navigation_drawer_position";

    /**
     * Per the design guidelines, you should show the drawer on launch until the user manually
     * expands it. This shared preference tracks this.
     */
    private static final String PREF_USER_LEARNED_DRAWER = "navigation_drawer_learned";

    /**
     * A pointer to the current callbacks instance (the Activity).
     */
    private NavigationDrawerCallbacks mCallbacks;

    /**
     * Helper component that ties the action bar to the navigation drawer.
     */
    private ActionBarDrawerToggle mDrawerToggle;

    private DrawerLayout mDrawerLayout;
    private ListView mDrawerListView;
    private View mFragmentContainerView;

    private int mCurrentSelectedPosition = 0;
    private boolean mFromSavedInstanceState;
    private boolean mUserLearnedDrawer;
    private DrawerAdapter mAdapter;
    private ActionMode mActiveMode = null;

    public NavigationDrawerFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Read in the flag indicating whether or not the user has demonstrated awareness of the
        // drawer. See PREF_USER_LEARNED_DRAWER for details.
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(getActivity());
        mUserLearnedDrawer = sp.getBoolean(PREF_USER_LEARNED_DRAWER, false);

        if (savedInstanceState != null) {
            mCurrentSelectedPosition = savedInstanceState.getInt(STATE_SELECTED_POSITION);
            mFromSavedInstanceState = true;
        }

    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        // Indicate that this fragment would like to influence the set of actions in the action bar.
        setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        mDrawerListView = (ListView) inflater.inflate(
                R.layout.fragment_navigation_drawer, container, false);

        mDrawerListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                selectItem(position);
            }
        });

        mDrawerListView.setLongClickable(true);
        mDrawerListView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                mDrawerListView.clearChoices();
                mDrawerListView.setItemChecked(position, true);
                if (mActiveMode == null) {
                    mActiveMode = ((AppCompatActivity) getActivity()).startSupportActionMode(actionModeCallback);
                }
                return (true);
            }
        });

        View editItemsFooter = inflater.inflate(R.layout.footer_navigation_drawer, null);
        ((TextView)editItemsFooter.findViewById(R.id.footer_text)).setText(getString(R.string.mi_edit_items));
        ((ImageView)editItemsFooter.findViewById(R.id.footer_logo)).setImageResource(R.drawable.ic_edit_items);
        editItemsFooter.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(getActivity(), EditItemFragmentActivity.class);
                startActivity(i);
                //selectItem(mCurrentSelectedPosition);
                if (mDrawerLayout != null) {
                    mDrawerLayout.closeDrawer(mFragmentContainerView);
                }
            }
        });
        mDrawerListView.addFooterView(editItemsFooter);

        View recipeListFooter = inflater.inflate(R.layout.footer_navigation_drawer, null);
        ((TextView)recipeListFooter.findViewById(R.id.footer_text)).setText(getString(R.string.mi_recipe_list));
        ((ImageView)recipeListFooter.findViewById(R.id.footer_logo)).setImageResource(R.drawable.ic_add_recipe);
        recipeListFooter.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(getActivity(), RecipeListFragmentActivity.class);
                startActivity(i);
                //selectItem(mCurrentSelectedPosition);
                if (mDrawerLayout != null) {
                    mDrawerLayout.closeDrawer(mFragmentContainerView);
                }
            }
        });
        mDrawerListView.addFooterView(recipeListFooter);

        View settingsFooter = inflater.inflate(R.layout.footer_navigation_drawer, null);
        ((TextView)settingsFooter.findViewById(R.id.footer_text)).setText(getString(R.string.mi_settings));
        ((ImageView)settingsFooter.findViewById(R.id.footer_logo)).setImageResource(R.drawable.ic_settings);
        settingsFooter.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(getActivity(), SettingsActivity.class));
                //selectItem(mCurrentSelectedPosition);
                if (mDrawerLayout != null) {
                    mDrawerLayout.closeDrawer(mFragmentContainerView);
                }
            }
        });
        mDrawerListView.addFooterView(settingsFooter);

        return mDrawerListView;
    }

    public boolean isDrawerOpen() {
        return mDrawerLayout != null && mDrawerLayout.isDrawerOpen(mFragmentContainerView);
    }

    /**
     * Users of this fragment must call this method to set up the navigation drawer interactions.
     *
     * @param fragmentId   The android:id of this fragment in its activity's layout.
     * @param drawerLayout The DrawerLayout containing this fragment's UI.
     */
    public void setUp(int fragmentId, DrawerLayout drawerLayout) {
        mFragmentContainerView = getActivity().findViewById(fragmentId);
        mDrawerLayout = drawerLayout;

        // set a custom shadow that overlays the main content when the drawer opens
        mDrawerLayout.setDrawerShadow(R.drawable.drawer_shadow, GravityCompat.START);
        // set up the drawer's list view with items and click listener

        ActionBar actionBar = getActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setHomeButtonEnabled(true);

        // ActionBarDrawerToggle ties together the the proper interactions
        // between the navigation drawer and the action bar app icon.
        mDrawerToggle = new ActionBarDrawerToggle(
                getActivity(),                    /* host Activity */
                mDrawerLayout,                    /* DrawerLayout object */
                null,
                R.string.navigation_drawer_open,  /* "open drawer" description for accessibility */
                R.string.navigation_drawer_close  /* "close drawer" description for accessibility */
        ) {
            @Override
            public void onDrawerClosed(View drawerView) {
                super.onDrawerClosed(drawerView);
                if (!isAdded()) {
                    return;
                }

                getActivity().supportInvalidateOptionsMenu(); // calls onPrepareOptionsMenu()
            }

            @Override
            public void onDrawerOpened(View drawerView) {
                super.onDrawerOpened(drawerView);
                if (!isAdded()) {
                    return;
                }

                if (!mUserLearnedDrawer) {
                    // The user manually opened the drawer; store this flag to prevent auto-showing
                    // the navigation drawer automatically in the future.
                    mUserLearnedDrawer = true;
                    SharedPreferences sp = PreferenceManager
                            .getDefaultSharedPreferences(getActivity());
                    sp.edit().putBoolean(PREF_USER_LEARNED_DRAWER, true).commit();
                }

                getActivity().supportInvalidateOptionsMenu(); // calls onPrepareOptionsMenu()
            }
        };

        // If the user hasn't 'learned' about the drawer, open it to introduce them to the drawer,
        // per the navigation drawer design guidelines.
        if (!mUserLearnedDrawer && !mFromSavedInstanceState) {
            mDrawerLayout.openDrawer(mFragmentContainerView);
        }

        // Defer code dependent on restoration of previous instance state.
        mDrawerLayout.post(new Runnable() {
            @Override
            public void run() {
                mDrawerToggle.syncState();
            }
        });

        mDrawerLayout.setDrawerListener(mDrawerToggle);
    }

    public void selectItem(int position) {
        mCurrentSelectedPosition = position;
        if (mDrawerListView != null) {
            mDrawerListView.setItemChecked(position, true);
        }
        if (mDrawerLayout != null) {
            mDrawerLayout.closeDrawer(mFragmentContainerView);
        }
        if (mCallbacks != null) {
            mCallbacks.onNavigationDrawerItemSelected(position, mAdapter.getListId(position), mAdapter.getListName(position));
        }
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            mCallbacks = (NavigationDrawerCallbacks) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException("Activity must implement NavigationDrawerCallbacks.");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mCallbacks = null;
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt(STATE_SELECTED_POSITION, mCurrentSelectedPosition);
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        // Forward the new configuration the drawer toggle component.
        mDrawerToggle.onConfigurationChanged(newConfig);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        // If the drawer is open, show the global app actions in the action bar. See also
        // showGlobalContextActionBar, which controls the top-left area of the action bar.
        if (mDrawerLayout != null && isDrawerOpen()) {
            inflater.inflate(R.menu.global, menu);
            showGlobalContextActionBar();
        }
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        return mDrawerToggle.onOptionsItemSelected(item) || super.onOptionsItemSelected(item);
    }

    /**
     * Per the navigation drawer design guidelines, updates the action bar to show the global app
     * 'context', rather than just what's in the current screen.
     */
    private void showGlobalContextActionBar() {
        ActionBar actionBar = getActionBar();
        actionBar.setDisplayShowTitleEnabled(true);
        actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_STANDARD);
        actionBar.setTitle(R.string.app_name);
    }

    private ActionBar getActionBar() {
        return ((AppCompatActivity) getActivity()).getSupportActionBar();
    }

    /**
     * Callbacks interface that all activities using this fragment must implement.
     */
    public interface NavigationDrawerCallbacks {
        /**
         * Called when an item in the navigation drawer is selected.
         */
        void onNavigationDrawerItemSelected(int position, long id, String name);
        void deleteList(long listId);
        void editList(long listId, String name);
    }

    public void setDrawerAdapter(List<ShoppingList> shoppingList) {
        mAdapter = new DrawerAdapter(getActionBar().getThemedContext(), shoppingList);
        mDrawerListView.setAdapter(mAdapter);
        mDrawerListView.setItemChecked(mCurrentSelectedPosition, true);
        if (mAdapter.getCount() > 0) {
            selectItem(mCurrentSelectedPosition);
        }

    }

    class DrawerAdapter extends ArrayAdapter<ShoppingList> {
        DrawerAdapter(Context context, List<ShoppingList> shoppingList) {
            super(context, android.R.layout.simple_list_item_1,android.R.id.text1, shoppingList);

        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            View row = super.getView(position, convertView, parent);
            ViewHolder holder = (ViewHolder) row.getTag();

            if(holder==null){
                holder = new ViewHolder(row);
                row.setTag(holder);
            }
            ShoppingList sl = getItem(position);
            holder.name.setText(sl.getName());

            return row;
        }

        public long getListId(int position) {
            return getItem(position).getId();
        }

        public String getListName(int position) {
            return getItem(position).getName();
        }
    }

    class ViewHolder{
        TextView name=null;

        ViewHolder(View row){
            name=(TextView) row.findViewById(android.R.id.text1);
        }
    }


    private ActionMode.Callback actionModeCallback = new ActionMode.Callback() {

        @Override
        public boolean onCreateActionMode(ActionMode mode, Menu menu) {
            MenuInflater inflater = mode.getMenuInflater();
            inflater.inflate(R.menu.list_context, menu);
            mode.setTitle(R.string.listitem_context_title);
            return true;
        }

        @Override
        public boolean onActionItemClicked(ActionMode mode, MenuItem item) {

            Log.d(TAG, "onActionItemClicked  item.getItemId()" + item.getItemId());
            switch (item.getItemId()) {
                case R.id.mi_edit_list:
                    editList(mDrawerListView.getCheckedItemPosition());
                    mode.finish();
                    return true;

                case R.id.mi_del_list:
                    deleteList(mDrawerListView.getCheckedItemPosition());
                    mode.finish();
                    return true;

                default:
                    return false;
            }

        }

        @Override
        public void onDestroyActionMode(ActionMode mode) {
            mActiveMode = null;
            mDrawerListView.clearChoices();
            mDrawerListView.requestLayout();

        }

        @Override
        public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
            return false;
        }

    };


    private void deleteList(final int position) {
        DialogFragment dialog = new DialogFragment(){
            @Override
            public Dialog onCreateDialog(Bundle savedInstanceState) {
                AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                builder.setMessage(R.string.delete_list_confirmation)
                        .setInverseBackgroundForced(true)
                        .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                if (mCallbacks != null) {
                                    mCallbacks.deleteList(mAdapter.getListId(position));
                                }
                                if (mAdapter.getCount() > 0) {
                                    selectItem(0);
                                } else {// Special case when delete last list.
                                    if (mDrawerLayout != null) {
                                        mDrawerLayout.closeDrawer(mFragmentContainerView);
                                    }
                                    if (mCallbacks != null) {
                                        mCallbacks.onNavigationDrawerItemSelected(-1, -1, null);
                                    }
                                }
                            }
                        })
                        .setNegativeButton(android.R.string.cancel, null);
                return builder.create();
            }
        };
        dialog.show(getActivity().getSupportFragmentManager(), "DeleteList");

    }

    private void editList(int position) {
        if (mCallbacks != null) {
            mCallbacks.editList(mAdapter.getListId(position), mAdapter.getListName(position));
        }
    }
}
