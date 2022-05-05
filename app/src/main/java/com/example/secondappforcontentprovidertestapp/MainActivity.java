package com.example.secondappforcontentprovidertestapp;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.loader.app.LoaderManager;
import androidx.loader.content.CursorLoader;
import androidx.loader.content.Loader;

import android.annotation.SuppressLint;
import android.content.ContentValues;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MainActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor> {

    Button btn_command;
    Spinner spinnerTable;
    Spinner spinnerCommand;
    EditText etId;

    // for update/insert
    EditText etIdUpdate;
    EditText etNameUpdate;
    EditText et3ParamUpdate;

    // Bundle key
    public final String TABLE_KEY = "TABLE";

    LoaderManager loaderManager;
    private int LOADER_ID = 123;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initialization();

        btn_command.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //getSupportLoaderManager().initLoader(123,null,MainActivity.this);
                checkCommand();
            }
        });
    }

    private void checkCommand() {
        String table = spinnerTable.getSelectedItem().toString().toLowerCase(Locale.ROOT);
        String command = spinnerCommand.getSelectedItem().toString().toLowerCase(Locale.ROOT);

        switch (command) {
            case "query":
                showDescriptionTable(table);
                break;
            case "update":
                if (isValidEditTextID(etId.getText().toString())) {
                    updateRow(table, etIdUpdate.getText().toString(), etNameUpdate.getText().toString(), et3ParamUpdate.getText().toString());
                } else {
                    Toast.makeText(this, "Некоректный ID в поле", Toast.LENGTH_SHORT).show();
                }
                break;
            case "delete":
                if (isValidEditTextID(etId.getText().toString())) {
                    deleteRow(table, etId.getText().toString());
                } else {
                    Toast.makeText(this, "Некоректный ID в поле", Toast.LENGTH_SHORT).show();
                }
                break;
            case "insert":
                if (isValidEditTextID(etIdUpdate.getText().toString())) {
                    insertRow(table, etIdUpdate.getText().toString(), etNameUpdate.getText().toString(), et3ParamUpdate.getText().toString());
                } else {

                }
                break;
            default:
                Toast.makeText(MainActivity.this, "Без команды", Toast.LENGTH_SHORT).show();
                break;
        }
    }

    private void insertRow(String table, String idToInsert, String param2ToInsert, String param3ToInsert) {
        ContentValues values = new ContentValues();
        Uri uri = Uri.parse("content://example.com.databaseapp.musicprovider/" + table);
        int id = Integer.parseInt(idToInsert);
        switch (table) {
            case "album":
                values.put("id", id);
                values.put("name", param2ToInsert);
                values.put("release", param3ToInsert);
                break;
            case "song":
                values.put("id", id);
                values.put("name", param2ToInsert);
                values.put("duration", param3ToInsert);
                break;
            case "albumsong":
                values.put("id", id);
                values.put("album_id", Integer.parseInt(param2ToInsert));
                values.put("song_id", Integer.parseInt(param3ToInsert));
                break;
            default:
                break;
        }
        getContentResolver().insert(uri, values);
        Toast.makeText(this, "Запись добавлена", Toast.LENGTH_SHORT).show();
    }

    private void deleteRow(String table, String idToDelete) {
        Uri uri = Uri.parse("content://example.com.databaseapp.musicprovider/" + table + "/" + idToDelete);
        int checkDel = getContentResolver().delete(uri, null, null);
        if (checkDel == 1) {
            Toast.makeText(this, "Запись удалена", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(this, "Данный ID некорректен", Toast.LENGTH_SHORT).show();
        }
    }

    private void updateRow(String table, String idToUpdate, String nameToUpdate, String param3ToUpdate) {
        ContentValues values = new ContentValues();
        if (isValidEditTextID(idToUpdate)) {
            int id = Integer.parseInt(idToUpdate);
            Uri uri;
            switch (table) {
                case "album":
                    values.put("id", id);
                    values.put("name", nameToUpdate);
                    values.put("release", param3ToUpdate);
                    uri = Uri.parse("content://example.com.databaseapp.musicprovider/" + table + "/" + etId.getText().toString());
                    break;
                case "song":
                    values.put("id", id);
                    values.put("name", nameToUpdate);
                    values.put("duration", param3ToUpdate);
                    uri = Uri.parse("content://example.com.databaseapp.musicprovider/" + table + "/" + etId.getText().toString());
                    break;
                case "albumsong":
                    values.put("id", id);
                    values.put("album_id", Integer.parseInt(nameToUpdate));
                    values.put("song_id", Integer.parseInt(param3ToUpdate));
                    uri = Uri.parse("content://example.com.databaseapp.musicprovider/" + table + "/" + etId.getText().toString());
                    break;
                default:
                    uri = null;
                    break;
            }
            int checkUpdate = getContentResolver().update(uri, values, null, null);
            if (checkUpdate == 1) {
                Toast.makeText(this, "Запись изменена", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "Данный ID некорректен", Toast.LENGTH_SHORT).show();
            }
        } else {
            Toast.makeText(this, "Введите ID в поле для обновления", Toast.LENGTH_SHORT).show();
        }
    }

    private boolean isValidEditTextID(String string) {
        return !string.isEmpty() && string.matches("\\d+");
    }

    private void showDescriptionTable(String table) {
        Bundle args = new Bundle();
        args.putString(TABLE_KEY, table);
        Loader<Cursor> loader = loaderManager.initLoader(LOADER_ID, args, MainActivity.this);
    }

    @NonNull
    @Override
    public Loader<Cursor> onCreateLoader(int id, @Nullable Bundle args) {
        String table = args.getString(TABLE_KEY).toLowerCase(Locale.ROOT).trim();
        //Toast.makeText(this, table, Toast.LENGTH_SHORT).show();
        return new CursorLoader(this, Uri.parse("content://example.com.databaseapp.musicprovider/" + table),
                null,
                null,
                null,
                null);
    }

    @SuppressLint("Range")
    @Override
    public void onLoadFinished(@NonNull Loader<Cursor> loader, Cursor data) {
        if (data != null && data.moveToFirst()) {
            StringBuilder stringBuilder = new StringBuilder();
            String table = spinnerTable.getSelectedItem().toString().toLowerCase(Locale.ROOT);
            switch (table) {
                case "album":
                    do {
                        stringBuilder.append("id = " + data.getString(data.getColumnIndex("id")))
                                .append(" name = " + data.getString(data.getColumnIndex("name")))
                                .append("release = " + data.getString(data.getColumnIndex("release")))
                                .append("\n");
                    } while (data.moveToNext());
                    break;
                case "song":
                    do {
                        stringBuilder.append("id = " + data.getString(data.getColumnIndex("id")))
                                .append(" name = " + data.getString(data.getColumnIndex("name")))
                                .append(" duration = " + data.getString(data.getColumnIndex("duration")))
                                .append("\n");
                    } while (data.moveToNext());
                    break;
                case "albumsong":
                    do {
                        stringBuilder.append("id = " + data.getString(data.getColumnIndex("id")))
                                .append(" album_id = " + data.getString(data.getColumnIndex("album_id")))
                                .append(" song_id =" + data.getString(data.getColumnIndex("song_id")))
                                .append("\n");;
                    } while (data.moveToNext());
                    break;
                default: break;
            }

            Toast.makeText(this, stringBuilder.toString(), Toast.LENGTH_LONG).show();
        }
        loaderManager.destroyLoader(loader.getId());
    }

    @Override
    public void onLoaderReset(@NonNull Loader<Cursor> loader) {

    }

    private void initialization() {
        spinnerTable = findViewById(R.id.spinnerTable);
        spinnerCommand = findViewById(R.id.spinnerCommand);
        btn_command = findViewById(R.id.btn_command);
        etId = findViewById(R.id.et_id);
        etIdUpdate = findViewById(R.id.et_id_update);
        etNameUpdate = findViewById(R.id.et_name_update);
        et3ParamUpdate = findViewById(R.id.et_3param_update);

        loaderManager = LoaderManager.getInstance(MainActivity.this);
    }
}