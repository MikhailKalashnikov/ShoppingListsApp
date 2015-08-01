package mikhail.kalashnikov.shoppinglists;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import java.util.List;

public class AddRecipeDialog extends DialogFragment{

    private AddRecipeDialogListener mListener;

    interface AddRecipeDialogListener {
        void onAddRecipeToList(Recipe recipe);
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        setStyle(STYLE_NORMAL, R.style.AppBaseTheme);
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        LayoutInflater inflater = getActivity().getLayoutInflater();
        View view = inflater.inflate(R.layout.add_item_to_list, null);
        ListView listView = (ListView) view.findViewById(R.id.item_list);
        listView.setVisibility(View.VISIBLE);
        view.findViewById(R.id.item_list_with_category).setVisibility(View.GONE);
        View headerView = inflater.inflate(R.layout.header_add_new_item, null);
        ((TextView)headerView.findViewById(R.id.add_new_item)).setText(R.string.add_new_recipe);
        headerView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(getActivity(), AddRecipe.class));
                dismiss();
            }
        });

        final List<Recipe> recipeLists = (DataModel.getInstance(getActivity().getApplicationContext())).getRecipeList();
        ArrayAdapter<Recipe> adapter = new ArrayAdapter<Recipe>(getActivity(),
                android.R.layout.simple_list_item_1, recipeLists);
        listView.addHeaderView(headerView);
        listView.setAdapter(adapter);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapter, View view,
                                    int position, long id) {
                mListener.onAddRecipeToList(recipeLists.get(position - 1));
                dismiss();
            }

        });

        builder.setTitle(R.string.pick_a_recipe).setView(view);
        return builder.create();
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            mListener = (AddRecipeDialogListener) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString()
                    + " must implement AddRecipeDialogListener");
        }
    }
}
