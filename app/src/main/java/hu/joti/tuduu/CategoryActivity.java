package hu.joti.tuduu;

import android.app.Activity;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.EditText;
import android.widget.Toast;

public class CategoryActivity extends AppCompatActivity {

  private EditText etCtgName;
  private Intent intent;
  private int index;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    requestWindowFeature(Window.FEATURE_NO_TITLE);
    setContentView(R.layout.activity_category);

    intent = getIntent();
    index = intent.getIntExtra("index", -1);
    String name = intent.getStringExtra("name").toString();

    etCtgName = findViewById(R.id.etCtgName);
    etCtgName.setText(name);
  }

  public void btnCancelOnClick(View view) {
    setResult(RESULT_CANCELED);
    finish();
  }

  public void btnOKOnClick(View view) {
    if (etCtgName.getText().toString().isEmpty()){
      Toast.makeText(this, getString(R.string.name_empty), Toast.LENGTH_SHORT).show();
      return;
    }
    intent.putExtra("name",etCtgName.getText().toString());
    intent.putExtra("index", index);

    setResult(RESULT_OK, intent);
    finish();
  }

}
