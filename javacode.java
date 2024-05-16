/*

--------CreateAdvertActivity.java---------

package com.example.task71p;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.util.Calendar;

public class CreateAdvertActivity extends AppCompatActivity {

    private RadioGroup radioGroup;
    private RadioButton radioLost;
    private RadioButton radioFound;
    private EditText editTextName;
    private EditText editTextPhone;
    private EditText editTextDescription;
    private EditText editTextDate;
    private EditText editTextLocation;
    private Button buttonSave;
    private DBHelper dbHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create);

        radioGroup = findViewById(R.id.radioGroup);
        radioLost = findViewById(R.id.radio_lost);
        radioFound = findViewById(R.id.radio_found);
        editTextName = findViewById(R.id.editText_name);
        editTextPhone = findViewById(R.id.editText_phone);
        editTextDescription = findViewById(R.id.editText_description);
        editTextDate = findViewById(R.id.editText_date);
        editTextLocation = findViewById(R.id.editText_location);
        buttonSave = findViewById(R.id.button_save);

        dbHelper = new DBHelper(this);

        editTextDate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showDatePickerDialog();
            }
        });

        buttonSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                saveAdvert();
            }
        });
    }

    private void showDatePickerDialog() {
        final Calendar calendar = Calendar.getInstance();
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);
        int day = calendar.get(Calendar.DAY_OF_MONTH);

        DatePickerDialog datePickerDialog = new DatePickerDialog(
                CreateAdvertActivity.this,
                new DatePickerDialog.OnDateSetListener() {
                    @Override
                    public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
                        editTextDate.setText(dayOfMonth + "/" + (month + 1) + "/" + year);
                    }
                },
                year, month, day
        );
        datePickerDialog.show();
    }

    private void saveAdvert() {
        String postType = radioLost.isChecked() ? "Lost" : radioFound.isChecked() ? "Found" : "";
        String name = editTextName.getText().toString();
        String phone = editTextPhone.getText().toString();
        String description = editTextDescription.getText().toString();
        String date = editTextDate.getText().toString();
        String location = editTextLocation.getText().toString();

        if (TextUtils.isEmpty(postType) || TextUtils.isEmpty(name) || TextUtils.isEmpty(phone) || TextUtils.isEmpty(description) || TextUtils.isEmpty(date) || TextUtils.isEmpty(location)) {
            Toast.makeText(this, "You must fill all fields", Toast.LENGTH_SHORT).show();
            return;
        }

        dbHelper.insertAdvert(postType, name, phone, description, date, location);

        Toast.makeText(this, "Advert Created!", Toast.LENGTH_SHORT).show();

        finish();
    }
}




--------DBHelper.java---------
package com.example.task71p;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class DBHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "lost_found.db";
    private static final int DATABASE_VERSION = 1;

    public static final String TABLE_NAME = "adverts";
    public static final String COLUMN_ID = "id";
    public static final String COLUMN_POST_TYPE = "post_type";
    public static final String COLUMN_NAME = "name";
    public static final String COLUMN_PHONE = "phone";
    public static final String COLUMN_DESCRIPTION = "description";
    public static final String COLUMN_DATE = "date";
    public static final String COLUMN_LOCATION = "location";

    public DBHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String CREATE_TABLE = "CREATE TABLE " + TABLE_NAME + "("
                + COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                + COLUMN_POST_TYPE + " TEXT,"
                + COLUMN_NAME + " TEXT,"
                + COLUMN_PHONE + " TEXT,"
                + COLUMN_DESCRIPTION + " TEXT,"
                + COLUMN_DATE + " TEXT,"
                + COLUMN_LOCATION + " TEXT" + ")";
        db.execSQL(CREATE_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME);
        onCreate(db);
    }

    public void insertAdvert(String postType, String name, String phone, String description, String date, String location) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_POST_TYPE, postType);
        values.put(COLUMN_NAME, name);
        values.put(COLUMN_PHONE, phone);
        values.put(COLUMN_DESCRIPTION, description);
        values.put(COLUMN_DATE, date);
        values.put(COLUMN_LOCATION, location);

        db.insert(TABLE_NAME, null, values);
        db.close();
    }

    public Cursor getAllItems() {
        SQLiteDatabase db = this.getReadableDatabase();
        return db.rawQuery("SELECT * FROM " + TABLE_NAME, null);
    }

    public Cursor getItemDetails(String name, String postType) {
        SQLiteDatabase db = this.getReadableDatabase();
        return db.rawQuery("SELECT * FROM " + TABLE_NAME + " WHERE " + COLUMN_NAME + "=? AND " + COLUMN_POST_TYPE + "=?", new String[]{name, postType});
    }

    public void deleteItem(String name, String postType) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(TABLE_NAME, COLUMN_NAME + "=? AND " + COLUMN_POST_TYPE + "=?", new String[]{name, postType});
        db.close();
    }
}



--------ItemDetailActivity.java---------
package com.example.task71p;

import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

public class ItemDetailActivity extends AppCompatActivity {

    private TextView textViewPostType;
    private TextView textViewName;
    private TextView textViewPhone;
    private TextView textViewDescription;
    private TextView textViewDate;
    private TextView textViewLocation;
    private Button buttonDelete;
    private DBHelper dbHelper;
    private String postType;
    private String name;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_item_detail);

        textViewPostType = findViewById(R.id.textView_post_type);
        textViewPhone = findViewById(R.id.textView_phone);
        textViewDescription = findViewById(R.id.textView_description);
        textViewDate = findViewById(R.id.textView_date);
        textViewLocation = findViewById(R.id.textView_location);
        buttonDelete = findViewById(R.id.button_delete);

        dbHelper = new DBHelper(this);

        Intent intent = getIntent();
        postType = intent.getStringExtra("postType");
        name = intent.getStringExtra("name");

        loadItemDetails();

        buttonDelete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                deleteItem();
            }
        });
    }

    private void loadItemDetails() {
        Cursor cursor = dbHelper.getItemDetails(name, postType);
        if (cursor.moveToFirst()) {
            String postType = cursor.getString(cursor.getColumnIndex(DBHelper.COLUMN_POST_TYPE));
            String name = cursor.getString(cursor.getColumnIndex(DBHelper.COLUMN_NAME));
            String phone = cursor.getString(cursor.getColumnIndex(DBHelper.COLUMN_PHONE));
            String description = cursor.getString(cursor.getColumnIndex(DBHelper.COLUMN_DESCRIPTION));
            String date = cursor.getString(cursor.getColumnIndex(DBHelper.COLUMN_DATE));
            String location = cursor.getString(cursor.getColumnIndex(DBHelper.COLUMN_LOCATION));

            textViewPostType.setText(postType + ": " + name);
            textViewPhone.setText("Contact number: " + phone);
            textViewDescription.setText(description);
            textViewDate.setText(postType + formatDate(date));
            textViewLocation.setText("At " + location);
        }
        cursor.close();
    }

    private void deleteItem() {
        dbHelper.deleteItem(name, postType);
        setResult(RESULT_OK);
        finish();
    }

    private String formatDate(String dateString) {
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy");
        try {
            Date date = dateFormat.parse(dateString);
            Calendar calendar = Calendar.getInstance();
            long currentTime = calendar.getTimeInMillis();
            calendar.setTime(date);
            long itemTime = calendar.getTimeInMillis();
            long difference = currentTime - itemTime;
            long days = difference / (1000 * 60 * 60 * 24);

            if (days <= 3) {
                return days + " days ago";
            } else {
                return dateString;
            }
        } catch (ParseException e) {
            e.printStackTrace();
            return dateString;
        }
    }
}




--------MainActivity.java---------
package com.example.task71p;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {

    private Button buttonCreate;
    private Button buttonViewList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        buttonCreate = findViewById(R.id.button_create);
        buttonViewList = findViewById(R.id.button_view_list);

        buttonCreate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, CreateAdvertActivity.class);
                startActivity(intent);
            }
        });

        buttonViewList.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, ViewListActivity.class);
                startActivity(intent);
            }
        });
    }
}




--------ViewListActivity.java---------
package com.example.task71p;

import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import java.util.ArrayList;

public class ViewListActivity extends AppCompatActivity {
    private static final String TAG = "ViewListActivity";

    private ListView listViewItems;
    private DBHelper dbHelper;
    private ArrayList<String> itemList;
    private ArrayAdapter<String> adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_list);

        listViewItems = findViewById(R.id.listView_items);
        dbHelper = new DBHelper(this);
        itemList = new ArrayList<>();

        adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, itemList);
        listViewItems.setAdapter(adapter);

        listViewItems.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                String item = ((TextView) view).getText().toString();
                String[] parts = item.split(" - ");
                String postType = parts[0];
                String name = parts[1];

                Intent intent = new Intent(ViewListActivity.this, ItemDetailActivity.class);
                intent.putExtra("postType", postType);
                intent.putExtra("name", name);
                startActivityForResult(intent, 1);
            }
        });

        loadItems();
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadItems();
    }

    private void loadItems() {
        itemList.clear();
        Cursor cursor = dbHelper.getAllItems();
        if (cursor.moveToFirst()) {
            do {
                Log.d(TAG, "Columns: " + cursor.getColumnNames());
                String postType = cursor.getString(cursor.getColumnIndex(DBHelper.COLUMN_POST_TYPE));
                String name = cursor.getString(cursor.getColumnIndex(DBHelper.COLUMN_NAME));
                itemList.add(postType + " - " + name);
            } while (cursor.moveToNext());
        }
        cursor.close();
        adapter.notifyDataSetChanged();
    }
}



*/