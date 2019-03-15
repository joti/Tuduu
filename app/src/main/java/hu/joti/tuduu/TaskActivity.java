package hu.joti.tuduu;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.List;

import hu.joti.tuduu.model.Category;
import hu.joti.tuduu.model.DBModel;
import hu.joti.tuduu.model.IModel;
import hu.joti.tuduu.model.Task;

public class TaskActivity extends AppCompatActivity {

  private DBModel model;
  private List<Category> categories;

  private Spinner spCategory;
  private Spinner spPriority;
  private EditText etName;
  private EditText etDesc;
  private CheckBox cbHasDeadline;
  private CheckBox cbIsDone;
  private EditText tvDeadline;
  private TextView tvIsDoneLabel;
  private DatePickerDialog.OnDateSetListener mDateSetListener;

  private Intent intent;
  private Task task;
  private int index;
  private Calendar deadline;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_task);

    getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);

    Log.d("TUDUU", "Task - OnCreate");

    intent = getIntent();
    task = (Task) intent.getSerializableExtra("task");
    if (task != null) {
      index = intent.getIntExtra("index", -1);
      Log.i("TUDUU", index + "");
    }

    spCategory = findViewById(R.id.spCategory);
    spPriority = findViewById(R.id.spPriority);
    etName = findViewById(R.id.etName);
    etDesc = findViewById(R.id.etDesc);
    cbHasDeadline = findViewById(R.id.cbHasDeadline);
    cbIsDone = findViewById(R.id.cbIsDone);
    tvDeadline = findViewById(R.id.tvDeadline);
    tvIsDoneLabel = findViewById(R.id.tvIsDoneLabel);

    model = new DBModel(this);

    // kategóriák spinnerbe töltése
    categories = model.getAllCategories();
    List<String> categoryNames = new ArrayList<>();

    // üres kategória hozzáadása
    categories.add(new Category("---"));
    Collections.sort(categories);

    for (Category category : categories) {
      categoryNames.add(category.getName());
    }
    ArrayAdapter<String> cadapter = new ArrayAdapter<String>(this, R.layout.spinneritem, categoryNames);
    spCategory.setAdapter(cadapter);

    // prioritások spinnerbe töltése
    List<String> priorityNames = new ArrayList<>();

    Resources res = getResources();
    for (String priority : res.getStringArray(R.array.priorities)) {
      priorityNames.add(priority);
    }
    ArrayAdapter<String> padapter = new ArrayAdapter<String>(this, R.layout.spinneritem, priorityNames);
    spPriority.setAdapter(padapter);

    // vezérlőelemek feltöltése taskbeli értékekkel
    if (task != null) {
      etName.setText(task.getName());
      etDesc.setText(task.getDescription());
      spPriority.setSelection(task.getPriority());

      deadline = model.getCalendarFromString(task.getDeadline());
      tvDeadline.setText(formatDate(deadline));
      cbHasDeadline.setChecked(deadline != null);

      cbIsDone.setChecked(!task.getDoneDate().isEmpty());
      if (cbIsDone.isChecked()) {
        Calendar doneDate = model.getCalendarFromString(task.getDoneDate());
        StringBuilder label = new StringBuilder(getString(R.string.completed));
        label.append(" (" + formatDate(doneDate) + ")");
        tvIsDoneLabel.setText(label);
      }

      int categoryIndex = -1;
      for (int i = 0; i < categories.size(); i++) {
        if (categories.get(i).getId() == task.getCategory().getId()) {
          categoryIndex = i;
          break;
        }
      }
      spCategory.setSelection(categoryIndex);
    } else {
      spPriority.setSelection(0);
    }

    cbHasDeadline.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        if (!((CheckBox) v).isChecked()) {
          deadline = null;
          tvDeadline.setText(formatDate(deadline));
        } else {
          cbHasDeadline.setChecked(false);
          onDeadlineClick();
        }
      }
    });

    tvDeadline.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        onDeadlineClick();
      }
    });

    mDateSetListener = new DatePickerDialog.OnDateSetListener() {
      @Override
      public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
        if (deadline == null) {
          deadline = Calendar.getInstance();
        }
        deadline.set(Calendar.YEAR, year);
        deadline.set(Calendar.MONTH, month);
        deadline.set(Calendar.DAY_OF_MONTH, dayOfMonth);

        tvDeadline.setText(formatDate(deadline));
        if (!cbHasDeadline.isChecked())
          cbHasDeadline.setChecked(true);
      }
    };

  }

  public void btnOKOnClick(View view) {
    if (etName.getText().toString().isEmpty()) {
      Toast.makeText(this, getString(R.string.name_empty), Toast.LENGTH_SHORT).show();
      return;
    }
    if (spCategory.getSelectedItemPosition() == 0) {
      Toast.makeText(this, getString(R.string.select_category), Toast.LENGTH_SHORT).show();
      return;
    }

    boolean isNew = (task == null);
    if (task == null) {
      task = new Task();
    }

    task.setCategory(categories.get(spCategory.getSelectedItemPosition()));
    task.setName(etName.getText().toString());
    task.setDescription(etDesc.getText().toString());
    task.setPriority(spPriority.getSelectedItemPosition());
    task.setDeadline(model.getStringFromCalendar(deadline));

    if (task.getDoneDate().isEmpty() && cbIsDone.isChecked()) {
      task.setDoneDate(model.getStringFromCalendar(Calendar.getInstance()));
    } else if (!task.getDoneDate().isEmpty() && !cbIsDone.isChecked()) {
      task.setDoneDate("");
    }

    intent.putExtra("task", task);
    if (isNew)
      intent.putExtra("index", index);

    setResult(RESULT_OK, intent);
    finish();
  }

  public void btnCancelOnClick(View view) {
    setResult(RESULT_CANCELED);
    finish();
  }

  public static String formatDate(Calendar deadline) {
    if (deadline == null) {
      return "";
    } else {
      // SimpleDateFormat helyett Locale-nak megfelelő formázás
      return DateFormat.getDateInstance().format(deadline.getTime());
    }
  }

  private void onDeadlineClick() {
    Calendar cal = Calendar.getInstance();
    if (deadline != null) {
      cal.setTime(deadline.getTime());
    }

    int year, month, day;
    year = cal.get(Calendar.YEAR);
    month = cal.get(Calendar.MONTH);
    day = cal.get(Calendar.DAY_OF_MONTH);

    DatePickerDialog dialog = new DatePickerDialog(
        TaskActivity.this,
        android.R.style.Theme_Holo_Light_Dialog_MinWidth,
        mDateSetListener,
        year, month, day);
    dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
    dialog.show();
  }

}
