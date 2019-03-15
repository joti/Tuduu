package hu.joti.tuduu;

import android.support.v7.app.ActionBar;
import android.content.Intent;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.ContextMenu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import java.util.Collections;
import java.util.List;

import hu.joti.tuduu.model.Category;
import hu.joti.tuduu.model.DBModel;
import hu.joti.tuduu.model.IModel;
import hu.joti.tuduu.model.Task;

public class CtgsActivity extends AppCompatActivity implements AdapterView.OnItemClickListener {

  private static final int RQC_EDIT = 2;

  private List<Category> categories;
  private CtgAdapter adapter;
  private Intent intent;
  private IModel model;

  private EditText etCtgName;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_ctgs);

    // Back gomb megjelenítése, újabb verziókban már enélkül is megy, csak a ParentActivityName beállításával
    ActionBar actionBar = getSupportActionBar();
    if (actionBar != null) {
      actionBar.setDisplayHomeAsUpEnabled(true);
    }

    etCtgName = findViewById(R.id.etCtgName);

    intent = getIntent();

    // Kategóriák betöltése
    model = new DBModel(this);
    categories = model.getAllCategories();
    Collections.sort(categories);

    adapter = new CtgAdapter(this, R.layout.ctglistitem, categories);
    ListView lv = findViewById(R.id.lvCategories);
    lv.setAdapter(adapter);

    lv.setOnItemClickListener(this);
    registerForContextMenu(lv);
  }

  @Override
  public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
    getMenuInflater().inflate(R.menu.menu_context_ctg, menu);
  }

  @Override
  public boolean onContextItemSelected(MenuItem item) {
    int id = item.getItemId();
    AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();
    int index = info.position;

    if (id == R.id.miDeleteCtg) {
      // Nem törölhetünk olyan kategóriát, amihez feladat tartozik
      Category category = categories.get(index);
      List<Task> tasks = model.getAllTasks(category);
      if (tasks.size() > 0) {
        Toast.makeText(this, getString(R.string.category_cant_delete), Toast.LENGTH_LONG).show();
        return true;
      }

      model.deleteCategory(category);
      categories.remove(index);
      adapter.notifyDataSetChanged();

      Toast.makeText(this, getString(R.string.ctg_removed), Toast.LENGTH_LONG).show();
      return true;
    } else if (id == R.id.miRenameCtg){
      Intent intent = new Intent(getApplicationContext(), CategoryActivity.class);
      intent.putExtra("name", categories.get(index).getName());
      intent.putExtra("index", index);

      startActivityForResult(intent, RQC_EDIT);
      return true;
    }
    return false;
  }

  public void btnAddCtgOnClick(View view) {
    Category category = new Category(etCtgName.getText().toString());
    model.saveCategory(category);
    categories.add(category);
    adapter.notifyDataSetChanged();

    etCtgName.setText("");
    etCtgName.clearFocus();
  }

  @Override
  public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
    Intent intent = new Intent(getApplicationContext(), CategoryActivity.class);
    intent.putExtra("name", categories.get(position).getName());
    intent.putExtra("index", position);

    startActivityForResult(intent, RQC_EDIT);
  }

  @Override
  protected void onActivityResult(int requestCode, int resultCode, Intent data) {
    if (resultCode == RESULT_OK) {
      if (requestCode == RQC_EDIT) {
        String name = data.getStringExtra("name");
        int index = data.getIntExtra("index", -1);
        Category category = categories.get(index);
        category.setName(name);
        model.saveCategory(category);
        adapter.notifyDataSetChanged();
      }
    }
  }

  @Override
  public boolean onOptionsItemSelected(MenuItem item) {
    switch (item.getItemId()) {
      case android.R.id.home:
        setResult(RESULT_CANCELED);
        finish();
        break;
    }
    return true;
  }

}
