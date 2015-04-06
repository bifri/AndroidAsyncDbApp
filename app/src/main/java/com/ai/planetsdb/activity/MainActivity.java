package com.ai.planetsdb.activity;

import android.app.ListActivity;
import android.app.LoaderManager;
import android.content.CursorLoader;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView.AdapterContextMenuInfo;
import android.widget.CursorAdapter;
import android.widget.ListView;

import com.ai.planetsdb.R;
import com.ai.planetsdb.adapter.PlanetsListCursorAdapter;
import com.ai.planetsdb.provider.PlanetsContract;


public class MainActivity extends ListActivity implements
        LoaderManager.LoaderCallbacks<Cursor> {
    //TODO: add Javadoc comments to methods and classes
    //TODO: remove warnings
    //TODO: check & change method & field visibility if needed
    //TODO: change placement of methods to logical
    //TODO: write readme for githubcom

    @SuppressWarnings("UnusedDeclaration")
    private static final String TAG = MainActivity.class.getSimpleName();

    // The loader's unique id. Loader ids are specific to the Activity or
    // Fragment in which they reside.
    private static final int LOADER_ID_PLANETS_LIST = 0;

    private static final int REQUEST_EDIT_PLANET = 0;
    private static final int REQUEST_DELETE_PLANET = 1;
    private static final int REQUEST_INSERT_PLANET = 2;

    private static final String[] PROJECTION =
            new String[]{PlanetsContract._ID,
                    PlanetsContract.PLANET_NAME,
                    PlanetsContract.DISTANCE_FROM_EARTH,
                    PlanetsContract.DISCOVERER,
                    PlanetsContract.DIAMETER,
                    PlanetsContract.HAS_ATMOSPHERE};

    // The adapter that binds our data to the ListView
    private CursorAdapter mAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Initialize the adapter. Note that we pass a 'null' Cursor as the
        // third argument. We will pass the adapter a Cursor only when the
        // data has finished loading for the first time (i.e. when the
        // LoaderManager delivers the data to onLoadFinished). Also note
        // that we have passed the '0' flag as the last argument. This
        // prevents the adapter from registering a ContentObserver for the
        // Cursor (the CursorLoader will do this for us!).
        mAdapter = new PlanetsListCursorAdapter(this, null, 0);

        // Associate the (now empty) adapter with the ListView.
        setListAdapter(mAdapter);

        // The Activity (which implements the LoaderCallbacks<Cursor>
        // interface) is the callbacks object through which we will interact
        // with the LoaderManager. The LoaderManager uses this object to
        // instantiate the Loader and to notify the client when data is made
        // available/unavailable.
        LoaderManager.LoaderCallbacks<Cursor> mListLoaderCallbacks = this;

        // Initialize the Loader with id '1' and callbacks 'mListLoaderCallbacks'.
        // If the loader doesn't already exist, one is created. Otherwise,
        // the already created Loader is reused. In either case, the
        // LoaderManager will manage the Loader across the Activity/Fragment
        // lifecycle, will receive any new loads once they have completed,
        // and will report this new data back to the 'mListLoaderCallbacks' object.
        getLoaderManager().initLoader(
                LOADER_ID_PLANETS_LIST, null, mListLoaderCallbacks);

        // Long presses invoke Context Menu
        registerForContextMenu(getListView());
    }

    // Called when the user selects an item from the List
    @Override
    public void onListItemClick(ListView l, View v, int pos, long id) {
        Uri uri = Uri.withAppendedPath(
                PlanetsContract.CONTENT_URI_PLANETS, Long.toString(id));
        Intent intent = new Intent(Intent.ACTION_EDIT, uri);
        intent.setClass(this, EditPlanetActivity.class);
        startActivityForResult(intent, REQUEST_EDIT_PLANET);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        switch (item.getItemId()) {
            case R.id.action_add_planet:
                Intent intent = new Intent(Intent.ACTION_INSERT);
                intent.setClass(this, EditPlanetActivity.class);
                startActivityForResult(intent, REQUEST_INSERT_PLANET);
                return true;

            default:
                return super.onOptionsItemSelected(item);
        }
    }

    // Create Context Menu
    @Override
    public void onCreateContextMenu(ContextMenu menu, View v,
                                    ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_context_list_item_planet, menu);
    }

    // Process clicks on Context Menu Items
    @Override
    public boolean onContextItemSelected(MenuItem item) {
        AdapterContextMenuInfo info = (AdapterContextMenuInfo) item.getMenuInfo();
        switch (item.getItemId()) {
            case R.id.delete_single_planet:
                Uri uri = Uri.withAppendedPath(
                        PlanetsContract.CONTENT_URI_PLANETS, Long.toString(info.id));
                Intent intent = new Intent(Intent.ACTION_DELETE, uri);
                intent.setClass(this, EditPlanetActivity.class);
                startActivityForResult(intent, REQUEST_DELETE_PLANET);
                return true;

            default:
                return super.onContextItemSelected(item);
        }
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        // A switch-case is useful when dealing with multiple Loaders/IDs
        switch (id) {
            case LOADER_ID_PLANETS_LIST:
                return new CursorLoader(this, PlanetsContract.CONTENT_URI_PLANETS,
                        PROJECTION, null, null, PlanetsContract.PLANET_NAME + " ASC");

            default:
                throw new UnsupportedOperationException("Unknown loader id: " + id);
        }
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
        if (isFinishing()) {
            return;
        }

        // A switch-case is useful when dealing with multiple Loaders/IDs
        switch (loader.getId()) {
            case LOADER_ID_PLANETS_LIST:
                // The asynchronous load is complete and the data
                // is now available for use. Only now can we associate
                // the queried Cursor with the CursorAdapter.
                mAdapter.swapCursor(cursor);
                break;
        }
        // The listview now displays the queried data.
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        // For whatever reason, the Loader's data is now unavailable.
        // Remove any references to the old data by replacing it with
        // a null Cursor.
        mAdapter.swapCursor(null);
    }
}