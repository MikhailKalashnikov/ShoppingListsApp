package mikhail.kalashnikov.shoppinglists;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.TextView;

import java.util.List;

public class SelectRecipeParamsDialog extends DialogFragment {
    private AddRecipeToListDialogListener mListener;
    private TextView mListName;
    private TextView mCategoryName;
    private CheckBox mCBUseRecipeName;
    private DataModel mModel;
    private String mRecipeName;

    static SelectRecipeParamsDialog newInstance(String recipeName, String categoryName,
                                             boolean useCategory) {
        SelectRecipeParamsDialog f = new SelectRecipeParamsDialog();

        Bundle args = new Bundle();
        args.putString("recipe_name", recipeName);
        args.putString("category_name", categoryName);
        args.putBoolean("use_category", useCategory);
        f.setArguments(args);

        return f;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        mModel = DataModel.getInstance(getActivity().getApplicationContext());

        mRecipeName = getArguments().getString("recipe_name");
        String dflCategoryName = getArguments().getString("category_name");
        boolean useCategory = getArguments().getBoolean("use_category");

        LayoutInflater inflater = getActivity().getLayoutInflater();
        View view = inflater.inflate(R.layout.add_recipe_to_list, null);
        mListName = (TextView) view.findViewById(R.id.list_for_recipe);
        mListName.setText(mRecipeName);

//        mListName.setOnClickListener(new View.OnClickListener() {
//                                         @Override
//                                         public void onClick(View v) {
//                                             getPickListDialog().show(getActivity().getSupportFragmentManager(), "PickListDialog");
//                                         }
//                                     }
//        );
        mListName.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (hasFocus) {
                    getPickListDialog().show(getActivity().getSupportFragmentManager(), "PickListDialog");
                }
            }
        });

        mCategoryName = (TextView) view.findViewById(R.id.category_for_recipe);
        if (useCategory) {
            mCategoryName.setVisibility(View.VISIBLE);
            mCategoryName.setText(dflCategoryName);
//            mCategoryName.setOnClickListener(new View.OnClickListener() {
//                                                 @Override
//                                                 public void onClick(View v) {
//                                                     getPickCategoryDialog().show(getActivity().getSupportFragmentManager(), "PickListDialog");
//                                                 }
//                                             }
//            );
            mCategoryName.setOnFocusChangeListener(new View.OnFocusChangeListener() {
                @Override
                public void onFocusChange(View v, boolean hasFocus) {
                    if (hasFocus) {
                        getPickCategoryDialog().show(getActivity().getSupportFragmentManager(), "PickListDialog");
                    }
                }
            });
        } else {
            mCategoryName.setVisibility(View.GONE);
        }

        final CheckBox useAsDefault = (CheckBox) view.findViewById(R.id.cbx_add_recipe_save_as_default);
        mCBUseRecipeName = (CheckBox) view.findViewById(R.id.cbx_use_recipe_name);
        mCBUseRecipeName.setOnCheckedChangeListener(
                new CompoundButton.OnCheckedChangeListener() {
                    @Override
                    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                        if (isChecked) {
                            mListName.setText(mRecipeName);
                        }
                    }
                }
        );

        builder
                .setTitle(R.string.add_recipe_to_list)
                .setView(view)
                .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if (mListName.length() > 0) {
                            mListener.onListAndCategorySelected(
                                    mListName.getText().toString(),
                                    mCategoryName.getText().toString(),
                                    mCBUseRecipeName.isChecked(),
                                    useAsDefault.isChecked()
                            );
                        }
                    }
                })
                .setNegativeButton(android.R.string.cancel, null);

        return builder.create();
    }

    public interface AddRecipeToListDialogListener {
        void onListAndCategorySelected(String listName, String categoryName,
                                       boolean useRecipeName,
                                       boolean useAsDefault);
    }


    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        // Verify that the host activity implements the callback interface
        try {
            // Instantiate the NoticeDialogListener so we can send events to the host
            mListener = (AddRecipeToListDialogListener) activity;
        } catch (ClassCastException e) {
            // The activity doesn't implement the interface, throw exception
            throw new ClassCastException(activity.toString()
                    + " must implement AddRecipeToListDialogListener");
        }
    }

    private DialogFragment getPickListDialog() {
        List<ShoppingList> shoppingLists = mModel.getShoppingList();
        final CharSequence[] listNames = new String[shoppingLists.size() + 2];
        listNames[0] = getActivity().getString(R.string.add_recipe_to_list_new_name);
        listNames[1] = getActivity().getString(R.string.cbx_use_recipe_name);
        for (int i = 0; i < shoppingLists.size(); i++) {
            listNames[i + 2] = shoppingLists.get(i).getName();
        }

        return new DialogFragment(){
            @Override
            public Dialog onCreateDialog(Bundle savedInstanceState) {
                android.support.v7.app.AlertDialog.Builder builder = new android.support.v7.app.AlertDialog.Builder(getActivity());
                builder.setTitle(R.string.pick_a_list)
                        .setItems(listNames, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                mCBUseRecipeName.setChecked(which == 1);
                                if (which == 0) {
                                    mListName.setText("");
                                } else if (which == 1) {
                                    mListName.setText(mRecipeName);
                                } else {
                                    mListName.setText(listNames[which]);
                                }
                            }
                        });
                return builder.create();
            }
        };
    }

    private DialogFragment getPickCategoryDialog() {
        List<String> categoryList = mModel.getCategoryList();
        final CharSequence[] categoryNames = new String[categoryList.size() + 1];
        categoryNames[0] = getActivity().getString(R.string.add_recipe_to_list_new_name);
        for (int i = 0; i < categoryList.size(); i++) {
            categoryNames[i + 1] = categoryList.get(i);
        }

        return new DialogFragment(){
            @Override
            public Dialog onCreateDialog(Bundle savedInstanceState) {
                android.support.v7.app.AlertDialog.Builder builder = new android.support.v7.app.AlertDialog.Builder(getActivity());
                builder.setTitle(R.string.pick_a_category)
                        .setItems(categoryNames, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                if (which == 0) {
                                    mCategoryName.setText("");
                                } else {
                                    mCategoryName.setText(categoryNames[which]);
                                }
                            }
                        });
                return builder.create();
            }
        };
    }
}
