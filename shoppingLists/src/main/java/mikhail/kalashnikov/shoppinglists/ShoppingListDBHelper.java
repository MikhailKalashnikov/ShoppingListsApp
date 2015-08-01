package mikhail.kalashnikov.shoppinglists;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.os.AsyncTask;
import android.util.Log;

public class ShoppingListDBHelper extends SQLiteOpenHelper {
	private final String TAG = getClass().getSimpleName();
	private static final String DATABASE_NAME="ShoppingList.db"; 
	private static final int SCHEMA_VERSION = 2;
	private static ShoppingListDBHelper singleton=null;
	private List<ShoppingList> mShoppingLists;
	private List<ListItem> mListItems;
	private List<Item> mItems; 
	private Map<String, List<Item>> mCategoriesMap = null;
	private Map<Long, Item> mItemsMap = new HashMap<>();
	private Context mContext = null;
	private long mMaxShoppingListId = -1;
	private String mEmptyCategoryName;

    synchronized static ShoppingListDBHelper getInstance(Context ctxt){
		if(singleton==null){
			singleton = new ShoppingListDBHelper(ctxt.getApplicationContext());
		}
		return singleton;
		
	}
	
	private ShoppingListDBHelper(Context ctxt){
		super(ctxt, DATABASE_NAME, null, SCHEMA_VERSION);
		this.mContext  = ctxt;
	}
	
	@Override
	public void onCreate(SQLiteDatabase db) {
		try{
			db.beginTransaction();
			db.execSQL("CREATE TABLE " + ShoppingList.TABLE_NAME + "("
					+ ShoppingList._ID + " INTEGER,"
					+ ShoppingList.COLUMN_NAME + " TEXT"
					+ ");");
			
			ContentValues cv = new ContentValues();
			cv.put(ShoppingList.COLUMN_NAME, mContext .getResources().getString(R.string.shopping_list_default_name));
			cv.put(ShoppingList._ID, 1);
			db.insert(ShoppingList.TABLE_NAME, null, cv);
			
			db.execSQL("CREATE TABLE " + ListItem.TABLE_NAME + "("
                    + ListItem._ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                    + ListItem.COLUMN_LIST_ID + " INTEGER,"
                    + ListItem.COLUMN_ITEM_ID + " INTEGER,"
                    + ListItem.COLUMN_QTY + " TEXT,"
                    + ListItem.COLUMN_DONE + " INTEGER"
                    + ");");
			
			db.execSQL("CREATE TABLE " + Item.TABLE_NAME + "("
					+ Item._ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
					+ Item.COLUMN_NAME + " TEXT,"
					+ Item.COLUMN_QTY_TYPE + " TEXT,"
					+ Item.COLUMN_CATEGORY + " TEXT"
					+ ");");

            // v2
            db.execSQL("CREATE TABLE " + Recipe.TABLE_NAME + "("
                    + Recipe._ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                    + Recipe.COLUMN_NAME + " TEXT"
                    + ");");

            db.execSQL("CREATE TABLE " + RecipeItem.TABLE_NAME + "("
                    + RecipeItem._ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                    + RecipeItem.COLUMN_RECIPE_ID + " INTEGER,"
                    + RecipeItem.COLUMN_ITEM_ID + " INTEGER,"
                    + RecipeItem.COLUMN_QTY + " TEXT"
                    + ");");
			db.setTransactionSuccessful();
		}finally{
			db.endTransaction();
		}

	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		if (LogGuard.isDebug) Log.d(TAG, "onUpgrade from " + oldVersion + " to " + newVersion);
        if (oldVersion == 1 && newVersion >= 2) {
            db.execSQL("CREATE TABLE " + Recipe.TABLE_NAME + "("
                    + Recipe._ID + " INTEGER,"
                    + Recipe.COLUMN_NAME + " TEXT"
                    + ");");

            db.execSQL("CREATE TABLE " + RecipeItem.TABLE_NAME + "("
                    + RecipeItem._ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                    + RecipeItem.COLUMN_RECIPE_ID + " INTEGER,"
                    + RecipeItem.COLUMN_ITEM_ID + " INTEGER,"
                    + RecipeItem.COLUMN_QTY + " TEXT"
                    + ");");
        } else if (oldVersion >= 2 && newVersion == 1){
            db.execSQL("DROP TABLE " + Recipe.TABLE_NAME + ";");
            db.execSQL("DROP TABLE " + RecipeItem.TABLE_NAME + ";");
        } else {
            Log.e(TAG, "Unknown DB update");
        }

	}
	
	void getShoppingDataAsync(ShoppingDataListener listener) { 
		ModelFragment.executeAsyncTask(new GetShoppingDataTask(listener));
	}
	
	long getNextShoppingListId(){
		mMaxShoppingListId++;
		return mMaxShoppingListId;
	}

    interface ShoppingDataListener {
		void updateShoppingData(List<ShoppingList> shoppingLists, 
								List<ListItem> listItems, 
								Map<Long, Item> itemsMap,
                                Map<Recipe,List<RecipeItem>> recipeItemsMap);
	}
	
	private class GetShoppingDataTask extends AsyncTask<Void, Void, Void>{
		private ShoppingDataListener listener;
        private Map<Recipe,List<RecipeItem>> mRecipeItemMap;
        private Map<Long, Recipe> mRecipeMap;

		GetShoppingDataTask(ShoppingDataListener listener){
			this.listener = listener;
		}
		
		@Override
		protected Void doInBackground(Void... params) {
			try{
				Cursor c = getReadableDatabase().rawQuery(
							"SELECT " +
								ShoppingList._ID + "," +
								ShoppingList.COLUMN_NAME + 
							" FROM " + ShoppingList.TABLE_NAME, 
							null);
				
				mShoppingLists = new ArrayList<>();
				while (c.moveToNext()) { 
					long id=c.getLong(0);
					if(id> mMaxShoppingListId){
						mMaxShoppingListId =id;
					}
					String name=c.getString(1); 
					mShoppingLists.add(new ShoppingList(name, id));
				}
				
				c.close();
				if(LogGuard.isDebug) {
					for(ShoppingList s: mShoppingLists){
						Log.d(TAG, s.toString());
					}
				}
				
				c = getReadableDatabase().rawQuery(
						"SELECT " +
							Item._ID + "," +
							Item.COLUMN_NAME + "," +
							Item.COLUMN_QTY_TYPE + "," +
							Item.COLUMN_CATEGORY +
						" FROM " + Item.TABLE_NAME, 
						null);
			
				mItems = new ArrayList<>();
				while (c.moveToNext()) { 
					long id=c.getLong(0);
					String name=c.getString(1);
					String qty_type=c.getString(2);
					String category=c.getString(3);
					Item item = new Item(id, name, qty_type,category);
					mItemsMap.put(id, item);
					mItems.add(item);
				}
				Collections.sort(mItems);
				c.close();
				if(LogGuard.isDebug) {
					
					for(Item s: mItemsMap.values()){
						Log.d(TAG, s.toStringFull());
					}
				}
				
				c = getReadableDatabase().rawQuery(
						"SELECT " +
							ListItem._ID + "," +
							ListItem.COLUMN_LIST_ID + "," +
							ListItem.COLUMN_ITEM_ID + "," +
							ListItem.COLUMN_QTY + "," +
							ListItem.COLUMN_DONE +
						" FROM " + ListItem.TABLE_NAME, 
						null);
			
				mListItems = new ArrayList<>();
				while (c.moveToNext()) { 
					long id=c.getLong(0);
					long list_id=c.getLong(1);
					long item_id=c.getLong(2);
					String qty=c.getString(3);
					int isDone=c.getInt(4);
					mListItems.add(new ListItem(id, list_id, mItemsMap.get(item_id), qty, isDone));
				}
				
				c.close();
				if(LogGuard.isDebug) {
					for(ListItem s: mListItems){
						Log.d(TAG, s.toString());
					}
				}
				
				mEmptyCategoryName = mContext .getApplicationContext().getResources()
						.getString(R.string.empty_category);


                getRecipes();
                getRecipeItems();
			}catch (Exception e) {
				Log.e(TAG, "GetShoppingDataTask", e);
			}
			return null;
			
		}

        private void getRecipes() {
            mRecipeItemMap = new HashMap<>();
            mRecipeMap = new HashMap<>();

            Cursor c = getReadableDatabase().rawQuery(
                    "SELECT " +
                            Recipe._ID + "," +
                            Recipe.COLUMN_NAME +
                            " FROM " + Recipe.TABLE_NAME,
                    null);

            while (c.moveToNext()) {
                long id = c.getLong(0);
                String name = c.getString(1);
                Recipe r = new Recipe(name, id);
                mRecipeItemMap.put(r, new ArrayList<RecipeItem>());
                mRecipeMap.put(id, r);
            }

            c.close();
            if(LogGuard.isDebug) {
                for(Recipe s: mRecipeItemMap.keySet()){
                    Log.d(TAG, s.toString());
                }
            }

        }

        private void getRecipeItems() {

            Cursor c = getReadableDatabase().rawQuery(
                    "SELECT " +
                            RecipeItem._ID + "," +
                            RecipeItem.COLUMN_RECIPE_ID + "," +
                            RecipeItem.COLUMN_ITEM_ID + "," +
                            RecipeItem.COLUMN_QTY  +
                            " FROM " + RecipeItem.TABLE_NAME,
                    null);

            while (c.moveToNext()) {
                long id = c.getLong(0);
                long recipe_id = c.getLong(1);
                long item_id=c.getLong(2);
                String qty=c.getString(3);
                if (mRecipeMap.containsKey(recipe_id)) {
                    Recipe r = mRecipeMap.get(recipe_id);
                    mRecipeItemMap.get(r)
                            .add(new RecipeItem(id, r, mItemsMap.get(item_id), qty));
                }

            }

            c.close();
            if(LogGuard.isDebug) {
                for(Recipe r: mRecipeItemMap.keySet()) {
                    for (RecipeItem s : mRecipeItemMap.get(r)) {
                        Log.d(TAG, s.toString());
                    }
                }
            }
        }

		@Override
		protected void onPostExecute(Void v) {
			if(LogGuard.isDebug) Log.d(TAG, "GetShoppingDataTask.onPostExecute" + (listener==null));
			listener.updateShoppingData(mShoppingLists, mListItems, mItemsMap, mRecipeItemMap);
		}
		
	}

    void addItemToIntList(Item item) {
		mItems.add(item);
		Collections.sort(mItems);
		if(mCategoriesMap != null){
			String category = item.getCategory(); 
			if( category == null || category.length() == 0){
				category = mEmptyCategoryName;
			}
			if(mCategoriesMap.containsKey(category)){
				mCategoriesMap.get(category).add(item);
			}else{
				List<Item> itemList = new ArrayList<>();
				itemList.add(item);
				mCategoriesMap.put(category, itemList);
			}
			
		}
	}
	
	private void buildCategoryItemMap(boolean rebuild){
		if (mCategoriesMap == null || rebuild){
			mCategoriesMap = new HashMap<>();
			for(Item i: mItems){
				String category = i.getCategory(); 
				if( category == null || category.length() == 0){
					category = mEmptyCategoryName;
				}
				if(mCategoriesMap.containsKey(category)){
					mCategoriesMap.get(category).add(i);
				}else{
					List<Item> itemList = new ArrayList<>();
					itemList.add(i);
					mCategoriesMap.put(category, itemList);
				}
			}
		}
	}
	List<Item> getItems() {
		return mItems;
	}
	
	List<String> getCategoryList() {
		buildCategoryItemMap(false);
		return new ArrayList<>(mCategoriesMap.keySet());
	}
	
	Map<String, List<Item>> getCategoryItemMap(){
		buildCategoryItemMap(false);
		return mCategoriesMap;
	}

	long insertListItem(ListItem listItem){
        if(LogGuard.isDebug) Log.d(TAG, "insertListItem:" + listItem);
		ContentValues values = new ContentValues();
		values.put(ListItem.COLUMN_LIST_ID, listItem.getList_id());
		values.put(ListItem.COLUMN_ITEM_ID, listItem.getItem().getId());
		values.put(ListItem.COLUMN_QTY, listItem.getQty());
		return getWritableDatabase().insert(ListItem.TABLE_NAME, null, values);
	}
	
	long insertItem(Item item){
        if(LogGuard.isDebug) Log.d(TAG, "insertItem:" + item);
		ContentValues values = new ContentValues();
		values.put(Item.COLUMN_NAME, item.getName());
		values.put(Item.COLUMN_QTY_TYPE, item.getQty_type());
		values.put(Item.COLUMN_CATEGORY, item.getCategory());
		return getWritableDatabase().insert(Item.TABLE_NAME, null, values);
	}
	
	long insertShoppingList(ShoppingList shoppingList){
		ContentValues values = new ContentValues();
		values.put(ShoppingList.COLUMN_NAME, shoppingList.getName());
		values.put(ShoppingList._ID, shoppingList.getId());
		return getWritableDatabase().insert(ShoppingList.TABLE_NAME, null, values);
	}

    long insertRecipe(Recipe recipe){
        if(LogGuard.isDebug) Log.d(TAG, "insertRecipe:" + recipe);
        ContentValues values = new ContentValues();
        values.put(Recipe.COLUMN_NAME, recipe.getName());
        return getWritableDatabase().insert(Recipe.TABLE_NAME, null, values);
    }

    long insertRecipeItem(RecipeItem recipeItem){
        if(LogGuard.isDebug) Log.d(TAG, "insertRecipeItem:" + recipeItem);
        ContentValues values = new ContentValues();
        values.put(RecipeItem.COLUMN_RECIPE_ID, recipeItem.getRecipe().getId());
        values.put(RecipeItem.COLUMN_ITEM_ID, recipeItem.getItem().getId());
        values.put(RecipeItem.COLUMN_QTY, recipeItem.getQty());
        return getWritableDatabase().insert(RecipeItem.TABLE_NAME, null, values);
    }

	void deleteListItem(long id){
		String whereClause = ListItem._ID + " = ?";
		String[] args = {String.valueOf(id)};
		getWritableDatabase().delete(ListItem.TABLE_NAME, whereClause, args);
	}

	private void deleteItem(long id){
		String whereClause = Item._ID + " = ?";
		String[] args = {String.valueOf(id)};
		getWritableDatabase().delete(Item.TABLE_NAME, whereClause, args);
	}
	
	void deleteShoppingList(long id){
		if(LogGuard.isDebug) Log.d(TAG, "deleteShoppingList: list_id" + id);
		String whereClause = ShoppingList._ID + " = ?";
		String[] args = {String.valueOf(id)};
		getWritableDatabase().delete(ShoppingList.TABLE_NAME, whereClause, args);
	}

    void deleteRecipe(long id){
        if(LogGuard.isDebug) Log.d(TAG, "deleteRecipe: id" + id);
        String whereClause = Recipe._ID + " = ?";
        String[] args = {String.valueOf(id)};
        getWritableDatabase().delete(Recipe.TABLE_NAME, whereClause, args);
    }

    void deleteRecipeItem(long id){
        if(LogGuard.isDebug) Log.d(TAG, "deleteRecipeItem: id" + id);
        String whereClause = RecipeItem._ID + " = ?";
        String[] args = {String.valueOf(id)};
        getWritableDatabase().delete(RecipeItem.TABLE_NAME, whereClause, args);
    }

    void deleteAllRecipeItemsForRecipe(long recipe_id){
        if(LogGuard.isDebug) Log.d(TAG, "deleteAllRecipeItemsForRecipe: recipe_id" + recipe_id);
        String whereClause = RecipeItem.COLUMN_RECIPE_ID + " = ?";
        String[] args = {String.valueOf(recipe_id)};
        getWritableDatabase().delete(RecipeItem.TABLE_NAME, whereClause, args);
    }
	
	void deleteItemsFromShoppingList(long list_id){
		if(LogGuard.isDebug) Log.d(TAG, "deleteItemsFromShoppingList: list_id" + list_id);
		String whereClause = ListItem.COLUMN_LIST_ID + " = ?";
		String[] args = {String.valueOf(list_id)};
		getWritableDatabase().delete(ListItem.TABLE_NAME, whereClause, args);
	}
	
	void deleteDoneItemsFromShoppingList(long list_id){
		if(LogGuard.isDebug) Log.d(TAG, "deleteDoneItemsFromShoppingList: list_id" + list_id);
		String whereClause = ListItem.COLUMN_LIST_ID + " = ?" + " and done=1";
		String[] args = {String.valueOf(list_id)};
		getWritableDatabase().delete(ListItem.TABLE_NAME, whereClause, args);
	}
	
	void updateShoppingList(ShoppingList list){
		if(LogGuard.isDebug) Log.d(TAG, "updateShoppingList: " + list);
		String whereClause = ShoppingList._ID + " = ?";
		String[] args = {String.valueOf(list.getId())};
		ContentValues values = new ContentValues();
		values.put(ShoppingList.COLUMN_NAME, list.getName());
		getWritableDatabase().update(ShoppingList.TABLE_NAME, values, whereClause, args);
	}

    void updateRecipe(Recipe recipe){
        if(LogGuard.isDebug) Log.d(TAG, "updateRecipe: " + recipe);
        String whereClause = Recipe._ID + " = ?";
        String[] args = {String.valueOf(recipe.getId())};
        ContentValues values = new ContentValues();
        values.put(Recipe.COLUMN_NAME, recipe.getName());
        getWritableDatabase().update(Recipe.TABLE_NAME, values, whereClause, args);
    }
	
	void updateListItem(ListItem listItem){
		if(LogGuard.isDebug) Log.d(TAG, "updateListItem: " + listItem);
		String whereClause = ListItem._ID + " = ?";
		String[] args = {String.valueOf(listItem.getId())};
		ContentValues values = new ContentValues();
		values.put(ListItem.COLUMN_QTY, listItem.getQty());
		values.put(ListItem.COLUMN_DONE, listItem.getIsDone());
		getWritableDatabase().update(ListItem.TABLE_NAME, values, whereClause, args);
	}


    void updateRecipeItem(RecipeItem recipeItem){
        if(LogGuard.isDebug) Log.d(TAG, "updateRecipeItem: " + recipeItem);
        String whereClause = RecipeItem._ID + " = ?";
        String[] args = {String.valueOf(recipeItem.getId())};
        ContentValues values = new ContentValues();
        values.put(RecipeItem.COLUMN_QTY, recipeItem.getQty());
        getWritableDatabase().update(RecipeItem.TABLE_NAME, values, whereClause, args);
    }

	private void updateItem(Item item){
		if(LogGuard.isDebug) Log.d(TAG, "updateItem: " + item);
		String whereClause = Item._ID + " = ?";
		String[] args = {String.valueOf(item.getId())};
		ContentValues values = new ContentValues();
		values.put(Item.COLUMN_NAME, item.getName());
		values.put(Item.COLUMN_QTY_TYPE, item.getQty_type());
		values.put(Item.COLUMN_CATEGORY, item.getCategory());
		getWritableDatabase().update(Item.TABLE_NAME, values, whereClause, args);
	}
	
	void updateItemAsync(String name, String qty_type, long item_id, String category) {
		if(LogGuard.isDebug) Log.d(TAG, "updateItemAsync: item_id="+item_id + ", name="+name
				+ ", qty_type="+qty_type
				+ ", category="+category);
		String oldCategory = mItemsMap.get(item_id).getCategory();
		mItemsMap.get(item_id).setName(name);
		mItemsMap.get(item_id).setQty_type(qty_type);
		mItemsMap.get(item_id).setCategory(category);
		Collections.sort(mItems);
		if(!category.equals(oldCategory)){
			//rebuild CategoryItemMap
			buildCategoryItemMap(true);
		}
		ModelFragment.executeAsyncTask(new UpdateItemTask(mItemsMap.get(item_id)));
		
	}
	
	private class UpdateItemTask extends AsyncTask<Void, Void, Void>{
		private Item item;
		
		UpdateItemTask(Item item){
			this.item=item;
		}
		
		@Override
		protected Void doInBackground(Void... params) {
			updateItem(item);
			return null;
		}
		
	}
	
	void deleteItemAsync(long id) {
		if(LogGuard.isDebug) Log.d(TAG, "deleteItemAsync: id="+id);
		Item item = mItemsMap.get(id);
		mItems.remove(item);
		
		if (mCategoriesMap !=null){
			String category = item.getCategory();
			if( category == null || category.length() == 0){
				category = mEmptyCategoryName;
			}
			mCategoriesMap.get(category).remove(item);
			if(mCategoriesMap.get(category).size() == 0){
				mCategoriesMap.remove(category);
			}
		}
		mItemsMap.remove(id);
		ModelFragment.executeAsyncTask(new DeleteItemTask(id));
	}
	
	private class DeleteItemTask extends AsyncTask<Void, Void, Void>{
		private long id;
		
		DeleteItemTask(long id){
			this.id=id;
		}
		
		@Override
		protected Void doInBackground(Void... params) {
			deleteItem(id);
			return null;
		}

	}	
	
	void insertItemAsync(String name, String qty_type, String category) {
		if(LogGuard.isDebug) Log.d(TAG, "insertItemAsync: name="+name + ", qty_type=" + qty_type);
		Item item = new Item(name, qty_type, category);
		ModelFragment.executeAsyncTask(new InsertItemTask(item));
	}
	
	private class InsertItemTask extends AsyncTask<Void, Void, Void>{
		private Item item;
		private long newRowId;
		
		InsertItemTask(Item item){
			this.item=item;
		}
		
		@Override
		protected Void doInBackground(Void... params) {
			newRowId = insertItem(item);
			return null;
		}
		
		@Override
		protected void onPostExecute(Void result) {
			item.setId(newRowId);
			mItemsMap.put(newRowId, item);
			addItemToIntList(item);
		}
		
	}

}
