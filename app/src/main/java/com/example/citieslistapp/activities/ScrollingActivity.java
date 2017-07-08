package com.example.citieslistapp.activities;

import android.app.ProgressDialog;
import android.content.Context;
import android.database.Cursor;
import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.SimpleCursorAdapter;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Spinner;

import com.example.citieslistapp.R;
import com.example.citieslistapp.adapters.RecycleCitiesAdapter;
import com.example.citieslistapp.adapters.TabsPagerFragmentAdapter;
import com.example.citieslistapp.database.DB;
import com.example.citieslistapp.network.GetCountries;
import com.example.citieslistapp.network.MainSettings;

import java.util.concurrent.TimeUnit;

public class ScrollingActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor> {

    DB db;
    SimpleCursorAdapter adapter;
    ProgressDialog progressDialog;

    ViewPager viewPager;

    MainSettings mainSettings = new MainSettings();

    private static final String TAG = "myLogs";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_scrolling);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        progressDialog = new ProgressDialog(this);

        new GetCurrentCountries(progressDialog, this).execute(MainSettings.API_LINK_GET_DATA);
        initTabs();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_scrolling, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    public void initialize_spinner(final String json){
        // открываем подключение к БД
        db = new DB(this);
        db.open();

        // формируем столбцы сопоставления
        String[] from = new String[] { DB.COLUMN_COUNTRY};
        int[] to = new int[] {android.R.id.text1};

        // создаем адаптер и настраиваем список
        adapter = new SimpleCursorAdapter(this, android.R.layout.simple_spinner_item, null, from, to, 0);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        // создаем лоадер для чтения данных
        getSupportLoaderManager().initLoader(0, null, this);

        Spinner spinner = (Spinner) findViewById(R.id.spinner);
        spinner.setAdapter(adapter);
        // заголовок
        spinner.setPrompt("Change city");
        // выделяем элемент
        spinner.setSelection(0);
        // устанавливаем обработчик нажатия
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {

            @Override
            public void onItemSelected(AdapterView<?> parent, View view,
                                       int position, long id) {
                RecyclerView recyclerView = (RecyclerView)findViewById(R.id.recyclerView);
                recyclerView.setAdapter(new RecycleCitiesAdapter(mainSettings.findCitiesbyCountry(json, db.getCountryName(id))));
            }
            @Override
            public void onNothingSelected(AdapterView<?> arg0) {
            }
        });
    }

    public void initTabs() {
        viewPager = (ViewPager) findViewById(R.id.viewPager);
        TabsPagerFragmentAdapter tabsPagerFragmentAdapter = new TabsPagerFragmentAdapter(getSupportFragmentManager());
        viewPager.setAdapter(tabsPagerFragmentAdapter);

        TabLayout tabLayout = (TabLayout) findViewById(R.id.tabs);
        tabLayout.setupWithViewPager(viewPager);
    }

    protected void onDestroy() {
        super.onDestroy();
        db.close();
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        return new MyCursorLoader(this, db);

    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        adapter.swapCursor(data);
        if(progressDialog !=null && progressDialog.isShowing()) {
            progressDialog.dismiss();
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {

    }

    static class MyCursorLoader extends CursorLoader {

        DB db;

        public MyCursorLoader(Context context, DB db) {
            super(context);
            this.db = db;
        }

        @Override
        public Cursor loadInBackground() {

            Cursor cursor = db.getAllDataCountries();
            try {
                TimeUnit.SECONDS.sleep(1);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            return cursor;
        }
    }

    private class GetCurrentCountries extends GetCountries {

        public GetCurrentCountries(ProgressDialog progressDialog, Context context) {
            super(progressDialog, context);
        }

        @Override
        public void onSucess(String s) {
            initialize_spinner(s);
        }
    }
}
