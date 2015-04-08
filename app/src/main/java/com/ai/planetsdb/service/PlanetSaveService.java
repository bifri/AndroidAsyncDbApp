/*
 * Partly copied from:
 * https://android.googlesource.com/platform/packages/apps/Contacts/+/
 * android-5.0.2_r1/src/com/android/contacts/ContactSaveService.java
 */

package com.ai.planetsdb.service;

import android.app.Activity;
import android.app.IntentService;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.widget.Toast;

import com.ai.planetsdb.R;
import com.ai.planetsdb.activity.EditPlanetActivity;
import com.ai.planetsdb.provider.PlanetsContract;

import java.util.concurrent.CopyOnWriteArrayList;

/**
 * A service responsible for saving changes to the content provider.
 */
public class PlanetSaveService extends IntentService {
    private static final String TAG = PlanetSaveService.class.getSimpleName();
    private static final boolean DEBUG = true;

    private static final long DEBUG_SLEEP_TIME = 5000L;

    private static final String ACTION_INSERT_PLANET = "insert";
    private static final String ACTION_UPDATE_PLANET = "edit";
    private static final String ACTION_DELETE_PLANET = "delete";

    private static final String EXTRA_CONTENT_VALUES = "contentValues";
    private static final String EXTRA_PLANET_INIT_NAME = "initName";
    private static final String EXTRA_CALLBACK_INTENT = "callbackIntent";
    public static final String EXTRA_SERVICE_SUCCEEDED = "saveSucceeded";

    public interface Listener {
        public void onServiceCompleted(Intent callbackIntent);
    }

    private static final CopyOnWriteArrayList<Listener> sListeners =
            new CopyOnWriteArrayList<>();

    private Handler mMainHandler;

    public PlanetSaveService() {
        super(TAG);
        setIntentRedelivery(true);
        mMainHandler = new Handler(Looper.getMainLooper());
    }

    public static void registerListener(Listener listener) {
        sListeners.add(0, listener);
    }

    public static void unregisterListener(Listener listener) {
        sListeners.remove(listener);
        Log.i(TAG, "Listener removed");
    }

    @Override
    public Object getSystemService(String name) {
        Object service = super.getSystemService(name);
        if (service != null) {
            return service;
        }

        return getApplicationContext().getSystemService(name);
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        String action = intent.getAction();
        switch (action) {
            case ACTION_INSERT_PLANET:
                insertPlanet(intent);
                break;
            case ACTION_UPDATE_PLANET:
                updatePlanet(intent);
                break;
            case ACTION_DELETE_PLANET:
                deletePlanet(intent);
                break;
            default:
                throw new UnsupportedOperationException("Wrong service intent action");
        }
    }

    /**
     * Creates an intent that can be sent to this service to create a new planet
     * using data presented as a set of ContentValues.
     */
    public static Intent createInsertPlanetIntent(
            Context context,
            ContentValues values,
            Class<? extends Activity> callbackActivity,
            int callbackActivityId,
            String callbackAction) {

        Intent serviceIntent = new Intent(context, PlanetSaveService.class);
        serviceIntent.setAction(PlanetSaveService.ACTION_INSERT_PLANET);
        serviceIntent.putExtra(PlanetSaveService.EXTRA_CONTENT_VALUES, values);

        // Callback intent will be invoked by the service once the new contact is
        // created.  The service will put the URI of the new contact as "data" on
        // the callback intent.
        Intent callbackIntent = new Intent(context, callbackActivity);
        callbackIntent.setAction(callbackAction);
        callbackIntent.putExtra(EditPlanetActivity.ACTIVITY_ID, callbackActivityId);
        serviceIntent.putExtra(PlanetSaveService.EXTRA_CALLBACK_INTENT, callbackIntent);
        return serviceIntent;
    }

    private void insertPlanet(Intent intent) {
        if (DEBUG) {
            try {
                Thread.sleep(DEBUG_SLEEP_TIME);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        final ContentResolver resolver = getContentResolver();

        ContentValues values = intent.getParcelableExtra(
                PlanetSaveService.EXTRA_CONTENT_VALUES);
        String newPlanetName = values.getAsString(PlanetsContract.PLANET_NAME);

        // Check if there is no such planet name (but with another ID)
        // already in ContentProvider.
        // Owerwrite of existing planet is not allowed.
        Cursor cursor = resolver.query(
                PlanetsContract.CONTENT_URI_PLANETS,
                new String[]{PlanetsContract.PLANET_NAME},
                PlanetsContract.PLANET_NAME + " COLLATE NOCASE " + "= ?",
                new String[]{newPlanetName}, null);

        boolean succeeded = false;
        Uri itemUri = null;

        // Some providers return null if an error occurs, others throw an exception
        if (null == cursor) {
            Log.e(TAG, "DB error: getContentResolver().query returned null");
            showToast(R.string.db_error);

        // There is already such name in ContentProvider
        } else if (cursor.getCount() > 0) {
            cursor.close();
            showToast(R.string.repeated_name_error);

        } else {
            cursor.close();

            // Attempt to persist changes
            try {
                itemUri = resolver.insert(PlanetsContract.CONTENT_URI_PLANETS, values);
                succeeded = itemUri != null;

                if (succeeded) {
                    showToast(R.string.planet_created);
                } else {
                    showToast(R.string.planet_create_error);
                }

            } catch (Exception e) {
                throw new RuntimeException("Failed to store new planet", e);
            }
        }

        Intent callbackIntent = intent.getParcelableExtra(EXTRA_CALLBACK_INTENT);
        if (callbackIntent != null) {
            callbackIntent.putExtra(EXTRA_SERVICE_SUCCEEDED, succeeded);
            callbackIntent.setData(itemUri);
            deliverCallback(callbackIntent);
        }
    }

    /**
     * Creates an intent that can be sent to this service to update planet
     * using data presented as a set of ContentValues.
     */
    public static Intent createUpdatePlanetIntent(
            Context context,
            Uri uri,
            ContentValues values,
            String initName,
            Class<? extends Activity> callbackActivity,
            int callbackActivityId,
            String callbackAction) {

        Intent serviceIntent = new Intent(context, PlanetSaveService.class);
        serviceIntent.setAction(PlanetSaveService.ACTION_UPDATE_PLANET);
        serviceIntent.setData(uri);
        serviceIntent.putExtra(PlanetSaveService.EXTRA_CONTENT_VALUES, values);
        serviceIntent.putExtra(PlanetSaveService.EXTRA_PLANET_INIT_NAME, initName);

        // Callback intent will be invoked by the service once the new contact is
        // created.  The service will put the URI of the new contact as "data" on
        // the callback intent.
        Intent callbackIntent = new Intent(context, callbackActivity);
        callbackIntent.setAction(callbackAction);
        callbackIntent.putExtra(EditPlanetActivity.ACTIVITY_ID, callbackActivityId);
        serviceIntent.putExtra(PlanetSaveService.EXTRA_CALLBACK_INTENT, callbackIntent);
        return serviceIntent;
    }

    private void updatePlanet(Intent intent) {
        if (DEBUG) {
            try {
                Thread.sleep(DEBUG_SLEEP_TIME);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        final ContentResolver resolver = getContentResolver();

        ContentValues values = intent.getParcelableExtra(
                PlanetSaveService.EXTRA_CONTENT_VALUES);
        String initPlanetName = intent.getStringExtra(
                PlanetSaveService.EXTRA_PLANET_INIT_NAME);

        String newPlanetName = values.getAsString(PlanetsContract.PLANET_NAME);

        boolean isResultFlag = false;
        boolean succeeded = false;
        Uri uri = intent.getData();

        // In editing planet name was changed
        if (!initPlanetName.equalsIgnoreCase(newPlanetName)) {

            // Check if there is no such planet name (but with another ID)
            // already in ContentProvider.
            // Owerwrite of existing planet is not allowed.
            Cursor cursor = resolver.query(
                    PlanetsContract.CONTENT_URI_PLANETS,
                    new String[]{PlanetsContract.PLANET_NAME},
                    PlanetsContract.PLANET_NAME + " COLLATE NOCASE " + "= ?",
                    new String[]{newPlanetName}, null);

            // Some providers return null if an error occurs, others throw an exception
            if (null == cursor) {
                Log.e(TAG, "DB error: getContentResolver().query returned null");
                showToast(R.string.db_error);
                isResultFlag = true;

              // There is already such name in ContentProvider
            } else if (cursor.getCount() > 0) {
                cursor.close();
                showToast(R.string.repeated_name_error);
                isResultFlag = true;
            } else {
                cursor.close();
            }
        }

        if (!isResultFlag) {
            // Attempt to persist changes
            try {
                int result = resolver.update(uri, values, null, null);
                succeeded = result != 0;

                if (succeeded) {
                    showToast(R.string.planet_saved);
                } else {
                    showToast(R.string.planet_save_error);
                }
            } catch (IllegalArgumentException e) {
                // This is thrown by applyBatch on malformed requests
                Log.e(TAG, "Problem persisting user edits", e);
                succeeded = false;
                showToast(R.string.db_error);
            }
        }

        Intent callbackIntent = intent.getParcelableExtra(EXTRA_CALLBACK_INTENT);
        if (callbackIntent != null) {
            callbackIntent.putExtra(EXTRA_SERVICE_SUCCEEDED, succeeded);
            callbackIntent.setData(uri);
            deliverCallback(callbackIntent);
        }
    }

    /**
     * Creates an intent that can be sent to this service to delete a planet.
     */
    public static Intent createDeletePlanetIntent(
            Context context, Uri contactUri,
            Class<? extends Activity> callbackActivity,
            int callbackActivityId,
            String callbackAction) {

        Intent serviceIntent = new Intent(context, PlanetSaveService.class);
        serviceIntent.setAction(PlanetSaveService.ACTION_DELETE_PLANET);
        serviceIntent.setData(contactUri);

        // Callback intent will be invoked by the service once the new contact is
        // created.  The service will put the URI of the new contact as "data" on
        // the callback intent.
        Intent callbackIntent = new Intent(context, callbackActivity);
        callbackIntent.setAction(callbackAction);
        callbackIntent.putExtra(EditPlanetActivity.ACTIVITY_ID, callbackActivityId);
        serviceIntent.putExtra(PlanetSaveService.EXTRA_CALLBACK_INTENT, callbackIntent);
        return serviceIntent;
    }

    private void deletePlanet(Intent intent) {
        if (DEBUG) {
            try {
                Thread.sleep(DEBUG_SLEEP_TIME);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        Uri contactUri = intent.getData();
        if (contactUri == null) {
            Log.e(TAG, "Invalid arguments for deletePlanet request");
            return;
        }

        getContentResolver().delete(contactUri, null, null);
        showToast(R.string.planet_deleted);

        Intent callbackIntent = intent.getParcelableExtra(EXTRA_CALLBACK_INTENT);
        if (callbackIntent != null) {
            callbackIntent.putExtra(EXTRA_SERVICE_SUCCEEDED, true);
            deliverCallback(callbackIntent);
        }
    }

    /**
     * Shows a toast on the UI thread.
     */
    private void showToast(final int message) {
        mMainHandler.post(new Runnable() {

            @Override
            public void run() {
                Toast.makeText(PlanetSaveService.this,
                        getResources().getString(message), Toast.LENGTH_LONG)
                     .show();
            }
        });
    }

    private void deliverCallback(final Intent callbackIntent) {
        mMainHandler.post(new Runnable() {

            @Override
            public void run() {
                deliverCallbackOnUiThread(callbackIntent);
            }
        });
    }

    void deliverCallbackOnUiThread(final Intent callbackIntent) {
        for (Listener listener : sListeners) {
            if (listener != null) {
                listener.onServiceCompleted(callbackIntent);
            }
        }
    }
}

