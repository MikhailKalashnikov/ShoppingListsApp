package mikhail.kalashnikov.shoppinglists;

import java.util.Comparator;
import java.util.List;

import com.hb.views.PinnedSectionListView.PinnedSectionListAdapter;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.ListFragment;
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
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

public class ListItemsFragment extends ListFragment
		implements AddItemToListDialog.AddItemToListDialogListener, AddNewItemDialog.AddNewItemDialogListener, 
			AddNewItemDialog.EditListItemDialogListener,
			OnItemLongClickListener, OnSharedPreferenceChangeListener, View.OnClickListener {
	private final String TAG = getClass().getSimpleName();
	private ArrayAdapter<ListItem> mAdapter;
	private DataModel dataModel;
	private ActionMode activeMode = null;
	private ListView listView=null;
	private Snackbar mMessageBar;
    private View coordinatorLayout;
	private static final int[] COLORS = new int[] {
			R.color.green_light2, R.color.blue_light, R.color.green_light, R.color.purple,
			R.color.red_light, R.color.papaya_whip, R.color.sky_blue, R.color.light_golden,
         R.color.rosy_brown, R.color.pale_turquoise,
        R.color.grey_light,R.color.teal_light};
	
	
    public static ListItemsFragment newInstance(long listId) {
    	if(LogGuard.isDebug) Log.d("ListItemsFragment", "newInstance listId" + listId);
    	ListItemsFragment f = new ListItemsFragment();

        Bundle args = new Bundle();
        args.putLong("listId", listId);
        f.setArguments(args);

        return f;
    }
    
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
    		Bundle savedInstanceState) {
    	View view = inflater.inflate(R.layout.list_items, container, false);
        coordinatorLayout = view.findViewById(R.id.coordinatorLayout);
        FloatingActionButton fab = (FloatingActionButton) view.findViewById(R.id.fab);
        fab.setOnClickListener(this);
    	return view;
    }
    
	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		Log.d(TAG, "onActivityCreated");
		super.onActivityCreated(savedInstanceState);
		setHasOptionsMenu(true);

		String strDeleted = getActivity().getString(R.string.msg_list_item_deleted);
		String strUndo = getActivity().getString(R.string.msg_btn_undo);
        
		dataModel = DataModel.getInstance(getActivity().getApplicationContext());
		
		Log.d(TAG, "getShownListId()=" + getShownListId()  + ", dataModel.isDataUploaded()=" + dataModel.isDataUploaded());
		if(dataModel.isDataUploaded()){
			mAdapter = new ShoppingListAdapter(dataModel.getListItems(getShownListId()));

			setListAdapter(mAdapter);
			listView = getListView();
			listView.setLongClickable(true);
			listView.setChoiceMode(ListView.CHOICE_MODE_SINGLE);
			listView.setOnItemLongClickListener(this);
			
	        mMessageBar = Snackbar.make(coordinatorLayout, strDeleted, Snackbar.LENGTH_SHORT)
                    .setAction(strUndo, new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            ((ShoppingListAdapter) mAdapter).undoRemove();
                            //mMessageBar.dismiss();
                        }
                    });
	        
			SwipeDismissListViewTouchListener touchListener =
	            new SwipeDismissListViewTouchListener(
	                    listView,
	                    new SwipeDismissListViewTouchListener.OnDismissCallback() {
	                        @Override
	                        public void onDismiss(ListView listView, int[] reverseSortedPositions) {
	                            for (int position : reverseSortedPositions) {
	                                mAdapter.remove(mAdapter.getItem(position));
	                            }
	                           // mAdapter.notifyDataSetChanged();
	                            mMessageBar.show();
	                            
	                        }
	                    });
			listView.setOnTouchListener(touchListener);
			// Setting this scroll listener is required to ensure that during ListView scrolling,
			// we don't look for swipes.
			listView.setOnScrollListener(touchListener.makeScrollListener());
		}
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getActivity());
		prefs.registerOnSharedPreferenceChangeListener(this);
		
	}
    
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
    	switch(item.getItemId()){
    		case R.id.mi_clear_list:
    			DialogFragment dialog = new DialogFragment(){
    				@Override
    				public Dialog onCreateDialog(Bundle savedInstanceState) {
    					AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
    					builder.setMessage(R.string.clear_list_confirmation)
								.setInverseBackgroundForced(true)
								.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {

									@Override
									public void onClick(DialogInterface dialog, int which) {
										dataModel.deleteAllListItemFromListAsync(getShownListId(), false);
										mAdapter.notifyDataSetChanged();
									
								}
							})
    						.setNegativeButton(android.R.string.cancel, null);
    					return builder.create();
    				}
    			};
    			dialog.show(getActivity().getSupportFragmentManager(), "ClearList");
    			return true;
    	}
    	return super.onOptionsItemSelected(item);
    }
    
    @Override
    public void onResume() {
    	super.onResume();
    	Log.d(TAG, "onResume");
    	if(dataModel.isDataUploaded()){
    		mAdapter.notifyDataSetChanged();
    	}

	}

	private long getShownListId() {
        return getArguments().getLong("listId", -1);
    }
	
	private ListItem getModel(int position) {
		return(((ShoppingListAdapter)getListAdapter()).getItem(position));
	}

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.fab) {
            DialogFragment addItemToListDialog = new AddItemToListDialog();
            addItemToListDialog.setTargetFragment(this, 0);
            addItemToListDialog.show(getActivity().getSupportFragmentManager(), "AddListItemDialog");
        }
    }

    class ShoppingListAdapter extends ArrayAdapter<ListItem>
		implements PinnedSectionListAdapter {
		ListItem removedListItem= null;
        
		ShoppingListAdapter(List<ListItem> listItems) {
			super(getActivity(), R.layout.list_item_row, R.id.item_name, listItems);
		}
		
		@Override
		public void remove(ListItem listItem) {
			removedListItem = listItem;
			dataModel.deleteListItemAsync(listItem.getId(), getShownListId());
			notifyDataSetChanged();
		}
		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			View row = super.getView(position, convertView, parent);
			ViewHolder holder = (ViewHolder) row.getTag();
			
			if(holder==null){
				holder = new ViewHolder(row);
				row.setTag(holder);
			}
			
			ListItem listItem = getModel(position);
			if(listItem.getId() < 0){
//				Log.d(TAG, "getView position="+position + ", id="+listItem.getId()
//						+ ", listItem =" + listItem.toString());
				holder.name.setText(listItem.getQty());
				row.setBackgroundColor(parent.getResources().getColor(COLORS[- (int) listItem.getId() % COLORS.length]));
				holder.qty.setVisibility(View.GONE);
				holder.name.setPadding(0, 0, 0, 0);
			}else{
				holder.qty.setText(listItem.getQty() + " - " + listItem.getItem().getQty_type());//TODO change format?
				holder.name.setText(listItem.getItem().getName()); 
				if(listItem.getIsDone()==1){
					holder.qty.setPaintFlags(holder.qty.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
					holder.qty.setBackgroundResource(R.color.doneTextViewColor);
					holder.qty.setTypeface(null, Typeface.ITALIC);
					holder.name.setPaintFlags(holder.name.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
					holder.name.setBackgroundResource(R.color.doneTextViewColor);
					holder.name.setTypeface(null, Typeface.ITALIC);
				}else{
					holder.qty.setPaintFlags(holder.qty.getPaintFlags() & (~Paint.STRIKE_THRU_TEXT_FLAG));
					holder.qty.setTypeface(null, Typeface.NORMAL);
					holder.qty.setBackgroundResource(android.R.color.transparent);
					holder.name.setPaintFlags(holder.name.getPaintFlags() & (~Paint.STRIKE_THRU_TEXT_FLAG));
					holder.name.setTypeface(null, Typeface.BOLD);
					holder.name.setBackgroundResource(android.R.color.transparent);
				}
			}
			return (row);
		}
		
		@Override
		public void notifyDataSetChanged() {
			setNotifyOnChange(false);
			sort(new Comparator<ListItem>() {
	
				@Override
				public int compare(ListItem lhs, ListItem rhs) {
					if(lhs.getIsDone()==1 && rhs.getIsDone()==0){
						return 1;
					}else if(lhs.getIsDone()==0 && rhs.getIsDone()==1){
						return -1;
					}
					return 0;
				}
			});
			super.notifyDataSetChanged();
		}

		public void undoRemove() {
			if (removedListItem!=null){
				dataModel.insertListItemAsync(removedListItem.getList_id(), removedListItem.getItem().getId(), removedListItem.getQty()); 
				notifyDataSetChanged();
			}
			
		}

		@Override 
		public int getViewTypeCount() {
            return 2;
        }

        @Override 
        public int getItemViewType(int position) {
            return getModel(position).getId() < 0? 1:0;
        }
        
		@Override
		public boolean isItemViewTypePinned(int viewType) {
			return viewType==1;
		}
	}
	
	class ViewHolder{
		TextView qty=null;
		TextView name=null;
		
		ViewHolder(View row){
			qty=(TextView) row.findViewById(R.id.item_qty);
			name=(TextView) row.findViewById(R.id.item_name);
		}
	}

	@Override
	public void onItemAddedToList(Item item) {
		dataModel.insertListItemAsync(getShownListId(), item.getId(), "1"); // TODO set qty
		mAdapter.notifyDataSetChanged();
	}

	@Override
	public void onItemAdded(String name, String qty_type, String category) {
		dataModel.insertItemAndAddToListAsync(name, qty_type, category, getShownListId(), "1"); // TODO set qty
		mAdapter.notifyDataSetChanged();
		
	}

	@Override
	public boolean onItemLongClick(AdapterView<?> view, View row,
			int position, long id) {
		if(((ListItem)listView.getAdapter().getItem(position)).getId()<0){
			return true;
		}
		listView.clearChoices();
		listView.setItemChecked(position, true);
		if (activeMode == null) {
			activeMode= ((AppCompatActivity)getActivity()).startSupportActionMode(actionModeCallback);
		}
		return(true);
	}

	private void editListItem(int position) {
		AddNewItemDialog dialog = AddNewItemDialog.newInstance(mAdapter.getItem(position));
		dialog.setTargetFragment(this, 0);
		dialog.show(getActivity().getSupportFragmentManager(), "EditListItemDialog");
		
	}

	private void deleteItemFromList(int position) {
		dataModel.deleteListItemAsync(mAdapter.getItem(position).getId(), getShownListId());
		mAdapter.notifyDataSetChanged();
		
	}
	
	private ActionMode.Callback actionModeCallback = new ActionMode.Callback() {

		@Override
		public boolean onCreateActionMode(ActionMode mode, Menu menu) {
			MenuInflater inflater = mode.getMenuInflater();
			inflater.inflate(R.menu.list_item_context, menu);
			mode.setTitle(R.string.listitem_context_title);
			return true;
		}
		
		@Override
		public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
			switch (item.getItemId()) {
				case R.id.mi_edit_item:
					editListItem(listView.getCheckedItemPosition());
					mode.finish();
					return true;
				
				case R.id.mi_del_item:
					deleteItemFromList(listView.getCheckedItemPosition());
					mode.finish();
					return true;
	
				default:
					return false;
			}

		}

		@Override
		public void onDestroyActionMode(ActionMode mode) {
			activeMode = null;
			listView.clearChoices();
			listView.requestLayout();
			
		}

		@Override
		public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
			return false;
		}
		
	};

	@Override
	public void onListItemEdited(String qty, long listItem_id, long list_id, int isDone) {
		dataModel.updateListItemAsync(qty, listItem_id, list_id, isDone); 
		mAdapter.notifyDataSetChanged();
	}

	@Override
	public void onListItemWithItemEdited(String qty, long listItem_id, long list_id, int isDone, 
			String name, String qty_type, String category, long item_id) {
		dataModel.updateListItemWithItemAsync(qty, listItem_id, list_id, isDone, name, qty_type, category, item_id);
		mAdapter.notifyDataSetChanged();
	}

	@Override
	public void onSharedPreferenceChanged(SharedPreferences sharedPreferences,
			String key) {
		if(key.equals(SettingsActivity.KEY_PREF_USE_CATEGORY_ON_MAIN_SCREEN)){
			Boolean showCategoryOnMainScreen = sharedPreferences.getBoolean(SettingsActivity.KEY_PREF_USE_CATEGORY_ON_MAIN_SCREEN, true);
			dataModel.switchShowCategoryOnMainScreen(showCategoryOnMainScreen);
			mAdapter.notifyDataSetChanged();
		}
		
	}


}
