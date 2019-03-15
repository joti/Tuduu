package hu.joti.tuduu;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.List;

import hu.joti.tuduu.R;
import hu.joti.tuduu.model.Category;

public class CtgAdapter extends ArrayAdapter<Category> {

  private int resource;

  public CtgAdapter(Context context, int resource, List<Category> objects) {
    super(context, resource, objects);
    this.resource = resource;
  }

  @Override
  public View getView(int position, View convertView, ViewGroup parent) {
    Category ctg = getItem(position);

    if (convertView == null){
      convertView = LayoutInflater.from(getContext()).inflate(resource, null);
    }

    TextView tvCtgName = convertView.findViewById(R.id.tvCtgName);
    tvCtgName.setText(ctg.getName());

    return convertView;
  }

}
