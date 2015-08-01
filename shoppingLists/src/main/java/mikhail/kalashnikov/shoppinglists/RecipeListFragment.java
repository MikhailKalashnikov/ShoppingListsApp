package mikhail.kalashnikov.shoppinglists;

import android.os.Bundle;
import android.support.v4.app.Fragment;
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
import android.widget.ExpandableListView;
import android.widget.ListView;
import android.widget.SimpleExpandableListAdapter;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class RecipeListFragment extends Fragment implements AdapterView.OnItemLongClickListener {
    private static final String ATTR_GROUP_NAME = "groupName";
    private static final String ATTR_ITEM_NAME = "itemName";
    private DataModel mModel;
    private ExpandableListView mListView;
    private SimpleExpandableListAdapter mExpandableListAdapter;
    private List<Map<String, Recipe>> mGroupData;
    private List<List<Map<String, RecipeItem>>> mChildData;
    private ActionMode mActiveMode = null;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mModel = DataModel.getInstance(getActivity().getApplicationContext());
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.add_item_to_list, container, false);

        mListView = (ExpandableListView) view.findViewById(R.id.item_list_with_category);
        mListView.setVisibility(View.VISIBLE);
        view.findViewById(R.id.item_list).setVisibility(View.GONE);
        
        Map<Recipe, List<RecipeItem>> recipeItemMap = mModel.getRecipeItemMap();
        mGroupData = new ArrayList<Map<String, Recipe>>();
        for(Recipe r: recipeItemMap.keySet()){
            Map<String, Recipe> m = new HashMap<>();
            m.put(ATTR_GROUP_NAME, r);
            mGroupData.add(m);
        }

        final String[] groupFrom = new String[]{ATTR_GROUP_NAME};
        final int[] groupTo = new int[]{android.R.id.text1};

        mChildData = new ArrayList<List<Map<String,RecipeItem>>>();
        for(Recipe r: recipeItemMap.keySet()){
            List<Map<String,RecipeItem>> childDataItem = new ArrayList<Map<String,RecipeItem>>();
            for(RecipeItem i: recipeItemMap.get(r)){
                Map<String, RecipeItem> m = new HashMap<String, RecipeItem>();
                m.put(ATTR_ITEM_NAME, i);
                childDataItem.add(m);
            }

            mChildData.add(childDataItem);
        }

        final String[] childFrom  = new String[]{ATTR_ITEM_NAME};
        final int[] childTo = new int[]{android.R.id.text1};

        mExpandableListAdapter = new SimpleExpandableListAdapter(
                getActivity(),
                mGroupData,
                android.R.layout.simple_expandable_list_item_1,
                groupFrom,
                groupTo,
                mChildData,
                android.R.layout.simple_list_item_1,
                childFrom,
                childTo){
            @Override
            public View getChildView(int groupPosition, int childPosition,
                                     boolean isLastChild, View convertView, ViewGroup parent) {

                View v;
                if (convertView == null) {
                    v = newChildView(isLastChild, parent);
                } else {
                    v = convertView;
                }
                bindView(v, mChildData.get(groupPosition).get(childPosition), childFrom, childTo);
                return v;
            }

            private void bindView(View view, Map<String, RecipeItem> data, String[] from, int[] to) {
                int len = to.length;

                for (int i = 0; i < len; i++) {
                    TextView v = (TextView)view.findViewById(to[i]);
                    if (v != null) {
                        v.setText(data.get(from[i]).getItem().getName());
                    }
                }
            }

            @Override
            public View getGroupView(int groupPosition, boolean isExpanded, View convertView, ViewGroup parent) {
                View v;
                if (convertView == null) {
                    v = newGroupView(isExpanded, parent);
                } else {
                    v = convertView;
                }
                bindGroupView(v, mGroupData.get(groupPosition), groupFrom, groupTo);
                return v;
            }

            private void bindGroupView(View view, Map<String, Recipe> data, String[] from, int[] to) {
                int len = to.length;

                for (int i = 0; i < len; i++) {
                    TextView v = (TextView)view.findViewById(to[i]);
                    if (v != null) {
                        v.setText(data.get(from[i]).getName());
                    }
                }
            }
        };

        mListView.setAdapter(mExpandableListAdapter);
//        mListView.setOnChildClickListener(new ExpandableListView.OnChildClickListener() {
//
//            @Override
//            public boolean onChildClick(ExpandableListView parent, View v,
//                                        int groupPosition, int childPosition, long id) {
//                editItem(mChildData.get(groupPosition).get(childPosition).get(ATTR_ITEM_NAME));
//                return false;
//            }
//        });

        mListView.setLongClickable(true);
        mListView.setChoiceMode(ListView.CHOICE_MODE_SINGLE);
        mListView.setOnItemLongClickListener(this);

        return view;
    }

    @Override
    public boolean onItemLongClick(AdapterView<?> view, View row,
                                   int position, long id) {
        mListView.clearChoices();
        mListView.setItemChecked(position, true);
        if (mActiveMode == null) {
            mActiveMode= ((AppCompatActivity)getActivity()).startSupportActionMode(actionModeCallback);
        }
        return(true);
    }

    private ActionMode.Callback actionModeCallback = new ActionMode.Callback() {
        @Override
        public boolean onCreateActionMode(ActionMode mode, Menu menu) {
            MenuInflater inflater = mode.getMenuInflater();
            inflater.inflate(R.menu.recipe_item_context, menu);
            mode.setTitle(R.string.listitem_context_title);

            return true;
        }

        @Override
        public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
            switch (item.getItemId()) {
                case R.id.mi_edit_item:
                    int position = mListView.getCheckedItemPosition();
                    ExpandableListPosition pos = getExpandableListPosition(position);
                    if(pos.type == ExpandableListPosition.TYPE_CHILD){
                        //editRecipeItem(mChildData.get(pos.group).get(pos.child_position).get(ATTR_ITEM_NAME));
                        Log.d("MK", mChildData.get(pos.group).get(pos.child_position).get(ATTR_ITEM_NAME).toString());
                    } else {
                        //editRecipe
                        Log.d("MK", mGroupData.get(pos.group).get(ATTR_GROUP_NAME).toString());
                    }

                    mode.finish();
                    return true;

                case R.id.mi_del_item:
                    position = mListView.getCheckedItemPosition();
                    pos = getExpandableListPosition(position);
                    if(pos.type == ExpandableListPosition.TYPE_CHILD) {
                        deleteRecipeItem(mChildData.get(pos.group).get(pos.child_position).get(ATTR_ITEM_NAME), pos);
                    } else {
                        deleteRecipe(mGroupData.get(pos.group).get(ATTR_GROUP_NAME), pos);
                    }

                    mode.finish();
                    return true;

                default:
                    return false;
            }

        }

        @Override
        public void onDestroyActionMode(ActionMode mode) {
            mActiveMode = null;
            mListView.clearChoices();
            mListView.requestLayout();

        }

        @Override
        public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
            return false;
        }

    };

    private void deleteRecipeItem(RecipeItem recipeItem, ExpandableListPosition expandableListPosition) {
        mModel.deleteRecipeItem(recipeItem.getId(), recipeItem.getRecipe());
        mChildData.get(expandableListPosition.group).remove(expandableListPosition.child_position);
        mExpandableListAdapter.notifyDataSetChanged();
    }

    private void deleteRecipe(Recipe recipe, ExpandableListPosition expandableListPosition) {
        mModel.deleteRecipe(recipe);
        mChildData.remove(expandableListPosition.group);
        mGroupData.remove(expandableListPosition.group);
        mExpandableListAdapter.notifyDataSetChanged();
    }

    private ExpandableListPosition getExpandableListPosition(int position){
        ExpandableListPosition pos = new ExpandableListPosition();
        pos.type = ExpandableListPosition.TYPE_GROUP;
        int current_pos = -1;
        for(int i=0; i< mChildData.size(); i++){
            current_pos++;
            if(current_pos == position){
                pos.type = ExpandableListPosition.TYPE_GROUP;
                pos.group = i;
                return pos;
            }

            if(position <= current_pos + mChildData.get(i).size()){
                pos.type = ExpandableListPosition.TYPE_CHILD;
                pos.group = i;
                pos.child_position = position - current_pos - 1;
                return pos;
            }

            current_pos = current_pos + mChildData.get(i).size();
        }

        return pos;
    }

    private class ExpandableListPosition{
        private static final int TYPE_GROUP = 1;
        private static final int TYPE_CHILD = 2;
        int type;
        int group;
        int child_position;
        @Override
        public String toString() {
            return "ExpandableListPosition [type=" + type + ", group=" + group
                    + ", child_position=" + child_position + "]";
        }

    }
}
