package hu.joti.tuduu;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.ContextMenu;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import hu.joti.tuduu.model.Category;
import hu.joti.tuduu.model.DBModel;
import hu.joti.tuduu.model.IModel;
import hu.joti.tuduu.model.Task;
import hu.joti.tuduu.model.TaskComparator;

public class MainActivity extends AppCompatActivity implements AdapterView.OnItemClickListener, AdapterView.OnItemSelectedListener {

  private static final int RQC_NEWTASK = 1;
  private static final int RQC_EDITTASK = 2;
  private static final int RQC_EDITCTGS = 3;
  private static final int RQC_SETTINGS = 4;

  private List<Task> tasks;
  private List<Category> categories;
  private Category ctgFilter;
  private TaskAdapter adapter;
  private DBModel model;
  private Spinner spCtgFilter;

  private ArrayAdapter<String> cadapter;
  private List<String> categoryNames;
  private int orderBy;
  private boolean showCompleted;
  private boolean showUnprioritized;
  private boolean showConfirmAlert;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main);

    loadPreferences();

    Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
    setSupportActionBar(toolbar);

    // feladatok betöltése
    model = new DBModel(this);
    tasks = model.getAllTasks();
    sortTasks();

    // kategóriák spinnerbe töltése
    ctgFilter = null;
    categories = new ArrayList<>();
    categoryNames = new ArrayList<>();
    loadCategories();

    spCtgFilter = findViewById(R.id.spCtgFilter);
    cadapter = new ArrayAdapter<String>(this, R.layout.spinner_primary, categoryNames);
    spCtgFilter.setAdapter(cadapter);
    spCtgFilter.setSelection(0);
    spCtgFilter.setOnItemSelectedListener(this);

    // Adapter létrehozása a feladatok listájához
    adapter = new TaskAdapter(this, R.layout.tasklistitem, tasks, model);
    ListView lv = findViewById(R.id.lvTasks);
    lv.setAdapter(adapter);

    lv.setOnItemClickListener(this);
    registerForContextMenu(lv);

    // "+" gombhoz eseménykezelő
    FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
    fab.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View view) {
        // Az üres kategória miatt kell legalább 2-nek lennie
        if (categories.size() < 2) {
          AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
          builder.setMessage(R.string.no_category_found)
              .setNeutralButton("OK", null);
          AlertDialog alert = builder.create();
          alert.show();
        } else {
          Intent intent = new Intent(getApplicationContext(), TaskActivity.class);
          startActivityForResult(intent, RQC_NEWTASK);
        }
      }
    });
  }

  @Override
  public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
    getMenuInflater().inflate(R.menu.menu_context, menu);

    AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) menuInfo;

    boolean isCompleted = !tasks.get(info.position).getDoneDate().isEmpty();

    menu.findItem(R.id.miClearDone).setVisible(isCompleted);
    menu.findItem(R.id.miSetDone).setVisible(!isCompleted);
  }

  @Override
  public boolean onContextItemSelected(MenuItem item) {
    int id = item.getItemId();
    AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();
    final int index = info.position;

    if (id == R.id.miEditTask) {
      Intent intent = new Intent(getApplicationContext(), TaskActivity.class);
      intent.putExtra("task", tasks.get(index));
      intent.putExtra("index", index);
      startActivityForResult(intent, RQC_EDITTASK);
      return true;
    }
    if (id == R.id.miSetDone) {
      Task task = tasks.get(index);
      if (task.getDoneDate().isEmpty()) {
        Calendar calendar = Calendar.getInstance();
        task.setDoneDate(model.getStringFromCalendar(calendar));
        model.saveTask(task);
        sortTasks();
        adapter.notifyDataSetChanged();
        return true;
      } else {
        Toast.makeText(this, getString(R.string.task_already_done), Toast.LENGTH_SHORT).show();
        return true;
      }
    }
    if (id == R.id.miClearDone) {
      Task task = tasks.get(index);
      if (!task.getDoneDate().isEmpty()) {
        task.setDoneDate("");
        model.saveTask(task);
        adapter.notifyDataSetChanged();
        return true;
      } else {
        Toast.makeText(this, getString(R.string.task_already_done), Toast.LENGTH_SHORT).show();
        return true;
      }
    }
    if (id == R.id.miDeleteTask) {
      if (showConfirmAlert) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(R.string.delete_proceed)
            .setPositiveButton(getString(R.string.ok), new DialogInterface.OnClickListener() {
              @Override
              public void onClick(DialogInterface dialog, int which) {
                deleteTask(index);
              }
            })
            .setNegativeButton(getString(R.string.cancel), null);
        AlertDialog alert = builder.create();
        alert.show();
      } else {
        deleteTask(index);
      }
      return true;
    }
    return false;
  }

  @Override
  public boolean onPrepareOptionsMenu(Menu menu) {
    boolean hasCompleted = false;
    for (Task task : tasks) {
      if (!task.getDoneDate().isEmpty()) {
        hasCompleted = true;
        break;
      }
    }

    menu.findItem(R.id.miDeleteDone).setVisible(hasCompleted);
    menu.findItem(R.id.miDeleteAll).setVisible(tasks.size() > 0);

    return super.onPrepareOptionsMenu(menu);
  }

  @Override
  public boolean onCreateOptionsMenu(Menu menu) {
    getMenuInflater().inflate(R.menu.menu_main, menu);
    return true;
  }

  @Override
  public boolean onOptionsItemSelected(MenuItem item) {
    int id = item.getItemId();

    if (id == R.id.miSettings) {
      Intent intent = new Intent(getApplicationContext(), SettingsActivity.class);
      startActivityForResult(intent, RQC_SETTINGS);
    }

    if (id == R.id.miCategories) {
      Intent intent = new Intent(getApplicationContext(), CtgsActivity.class);
      startActivityForResult(intent, RQC_EDITCTGS);
    }

    if (id == R.id.miDeleteDone || id == R.id.miDeleteAll) {
      final boolean deleteAll = (id == R.id.miDeleteAll);

      AlertDialog.Builder builder = new AlertDialog.Builder(this);
      builder.setMessage(R.string.delete_proceed)
          .setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
              Log.i("TUDUU", "DeleteAll2: " + deleteAll);
              deleteTasks(deleteAll);
            }
          })
          .setNegativeButton("Cancel", null);
      AlertDialog alert = builder.create();
      alert.show();
    }

    return super.onOptionsItemSelected(item);
  }

  @Override
  protected void onActivityResult(int requestCode, int resultCode, Intent data) {
    if (resultCode == RESULT_OK) {
      if (requestCode == RQC_NEWTASK) {
        Task task = (Task) (data.getSerializableExtra("task"));

        model.saveTask(task);
        tasks.add(task);
        sortTasks();
        adapter.notifyDataSetChanged();
      } else if (requestCode == RQC_EDITTASK) {
        Task task = (Task) (data.getSerializableExtra("task"));
        int index = data.getIntExtra("index", -1);

        if (index >= 0) {
          model.saveTask(task);
          tasks.set(index, task);
          sortTasks();
          adapter.notifyDataSetChanged();
        }
      }
    }

    if (requestCode == RQC_EDITCTGS) {
      // újra kell töltenünk a kategóriákat
      loadCategories();
      cadapter.notifyDataSetChanged();

      // Eddig a ctgFilter kategória volt kiválasztva.
      // Id alapján ismét kiválasztjuk, ha még létezik. Ha törölve lett, akkor az összeset mutatjuk.
      int filterIndex = 0;
      if (ctgFilter != null)
        filterIndex = categoryNames.indexOf(ctgFilter.getName());
      spCtgFilter.setSelection(filterIndex);
    }

    if (requestCode == RQC_SETTINGS) {
      loadPreferences();
      reloadTasks();
    }
  }

  private void loadPreferences() {
    SharedPreferences shpref = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
    orderBy = Integer.parseInt(shpref.getString("orderby", "1"));
    showCompleted = shpref.getBoolean("showcompleted", true);
    showUnprioritized = shpref.getBoolean("showunprioritized", true);
    showConfirmAlert = shpref.getBoolean("showconfirmalert", true);
  }

  private void loadCategories() {
    categories.clear();
    categories.addAll(model.getAllCategories());

    // Ábécésorrendbe rendezzük
    Collections.sort(categories);

    // üres kategória hozzáadása
    categories.add(0, new Category(getString(R.string.all_tasks)));

    // A neveket külön listába tesszük, ezt használja a spinner adaptere
    Category ctgFilterNew = null;
    categoryNames.clear();
    for (Category category : categories) {
      categoryNames.add(category.getName());
      if (ctgFilter != null) {
        if (ctgFilter.getId() == category.getId())
          ctgFilterNew = category;
      }
    }
    ctgFilter = ctgFilterNew;
  }

  private void sortTasks() {
    Iterator<Task> iterator = tasks.iterator();
    while (iterator.hasNext()) {
      Task task = iterator.next();
      if ((!task.getDoneDate().isEmpty() && !showCompleted) || (task.getPriority() == 0 && !showUnprioritized))
        iterator.remove();
    }

    if (tasks.size() > 0) {
      TaskComparator comparator = new TaskComparator(orderBy);
      Collections.sort(tasks, comparator);
    }

    this.invalidateOptionsMenu();
  }

  private void deleteTask(int index) {
    model.deleteTask(tasks.get(index));
    tasks.remove(index);
    adapter.notifyDataSetChanged();
    Toast.makeText(MainActivity.this, getString(R.string.task_removed), Toast.LENGTH_LONG).show();
  }

  private void deleteTasks(boolean deleteAll) {
    List<Task> tasksToRemove = new ArrayList<>();
    for (Task task : tasks) {
      if (!task.getDoneDate().isEmpty() || deleteAll) {
        model.deleteTask(task);
        tasksToRemove.add(task);
      }
    }
    tasks.removeAll(tasksToRemove);

    adapter.notifyDataSetChanged();
    Toast.makeText(MainActivity.this, getString(R.string.tasks_removed), Toast.LENGTH_LONG).show();
  }

  private void reloadTasks() {
    List<Task> newtasks = model.getAllTasks(ctgFilter);

    tasks.clear();
    tasks.addAll(newtasks);

    sortTasks();
    adapter.notifyDataSetChanged();
  }

  @Override
  public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
    Intent intent = new Intent(getApplicationContext(), TaskActivity.class);
    intent.putExtra("task", tasks.get(position));
    intent.putExtra("index", position);

    startActivityForResult(intent, RQC_EDITTASK);
  }

  @Override
  public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
    if (parent.getId() == R.id.spCtgFilter) {
      if (position == 0) {
        ctgFilter = null;
      } else {
        ctgFilter = categories.get(position);
      }
      reloadTasks();
    }
  }

  @Override
  public void onNothingSelected(AdapterView<?> parent) {
  }

}