package hu.joti.tuduu;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.support.v4.content.res.ResourcesCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.Calendar;
import java.util.List;

import hu.joti.tuduu.model.DBModel;
import hu.joti.tuduu.model.Task;

public class TaskAdapter extends ArrayAdapter<Task> {

  private int resource;
  private DBModel model;

  public TaskAdapter(Context context, int resource, List<Task> objects, DBModel model) {
    super(context, resource, objects);
    this.resource = resource;
    this.model = model;
  }

  @Override
  public View getView(int position, View convertView, ViewGroup parent) {
    Task task = getItem(position);

    if (convertView == null){
      convertView = LayoutInflater.from(getContext()).inflate(resource, null);
    }

    TextView tvName = convertView.findViewById(R.id.tvTaskName);
    TextView tvDesc = convertView.findViewById(R.id.tvTaskDesc);
    TextView tvDeadline = convertView.findViewById(R.id.tvTaskDeadline);

    int priority = task.getPriority();
    if (priority == 1){
      tvName.setBackgroundColor(Color.rgb(245,250,210));
    } else if  (priority == 2){
      tvName.setBackgroundColor(Color.rgb(249,225,205));
    } else if (priority == 3) {
      tvName.setBackgroundColor(Color.rgb(255,180,190));
    } else {
      tvName.setBackgroundColor(Color.rgb(200,200,200));
    }

    tvName.setText(task.getName());

    Drawable check = null;
    if (!task.getDoneDate().isEmpty()) {
      Resources res = getContext().getResources();
      check = res.getDrawable(R.drawable.check);
    }
    tvName.setCompoundDrawablesWithIntrinsicBounds(null, null, check, null);

    tvDesc.setText(task.getDescription());

    Calendar calendar = model.getCalendarFromString(task.getDeadline());
    tvDeadline.setText(TaskActivity.formatDate(calendar));

    return convertView;
  }

}
