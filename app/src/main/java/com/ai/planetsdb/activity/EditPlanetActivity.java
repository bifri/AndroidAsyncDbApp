package com.ai.planetsdb.activity;


import android.app.Activity;
import android.app.FragmentTransaction;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.util.Log;

import com.ai.planetsdb.R;
import com.ai.planetsdb.provider.PlanetsContract;

import java.io.Serializable;
import java.util.concurrent.atomic.AtomicInteger;

public class EditPlanetActivity extends Activity {

    private static final String TAG = EditPlanetActivity.class.getSimpleName();

    public static final String ACTIVITY_ID = "activityId";
    public static final String KEY_ACTIVITY_ID = "activityId";

    // Used for generating unique Activity Ids
    private static AtomicInteger mUniqueActivityId = new AtomicInteger(0);

    private int mActivityId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.i(TAG, getClass().getSimpleName() + ":entered onCreate()");

        super.onCreate(savedInstanceState);

        // this Activity can be started only using valid Intent
        Intent intent = getIntent();
        if (intent == null || !(Intent.ACTION_EDIT.equals(intent.getAction())
                || Intent.ACTION_INSERT.equals(intent.getAction())
                || Intent.ACTION_DELETE.equals(intent.getAction()))) {
            setResult(Activity.RESULT_CANCELED);
            finish();
            return;
        }

        if (savedInstanceState != null) {
            mActivityId = savedInstanceState.getInt(KEY_ACTIVITY_ID);
        } else {
            mActivityId = getNextActivityId();
        }

        setContentView(R.layout.simple_frame_layout);

        EventInfo eventInfo = getPlanetInfoFromIntent();

        EditPlanetFragment editFragment = (EditPlanetFragment) getFragmentManager().
                findFragmentById(R.id.main_frame);

        if (editFragment == null) {
            if (eventInfo.id != -1) {
                intent = null;
            }

            editFragment = new EditPlanetFragment();
            Bundle args = new Bundle();
            args.putSerializable(EditPlanetFragment.ARGUMENT1, eventInfo);
            args.putParcelable(EditPlanetFragment.ARGUMENT2, intent);
            editFragment.setArguments(args);
            FragmentTransaction ft = getFragmentManager().beginTransaction();
            ft.replace(R.id.main_frame, editFragment);
            ft.show(editFragment);
            ft.commit();
        }
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        outState.putInt(KEY_ACTIVITY_ID, mActivityId);
        super.onSaveInstanceState(outState);
    }

    /**
     * returns a practically unique token for db operations
     */
    private int getNextActivityId() {
        return mUniqueActivityId.getAndIncrement();
    }

    public int getActivityId() {
        return mActivityId;
    }

    private EventInfo getPlanetInfoFromIntent() {
        EventInfo info = new EventInfo();

        long planetId = -1;
        Intent intent = getIntent();
        Uri data = intent.getData();
        if (data != null) {
            String mime = getContentResolver().getType(data);
            if (mime != null &&
                    mime.equals(PlanetsContract.CONTENT_ITEM_TYPE_PLANETS)) {
                try {
                    planetId = Long.parseLong(data.getLastPathSegment());
                } catch (NumberFormatException e) {
                        Log.d(TAG, "Create new event");
                }
            }
        }

        info.eventType = intent.getAction();
        info.id = planetId;
        info.planetName = intent.getStringExtra(PlanetsContract.PLANET_NAME);

        return info;
    }

    public static class EventInfo implements Serializable {
        private static final long serialVersionUID = 1L;
        public String eventType;  // one of the ACTION_EDIT, ACTION_INSERT, ACTION_DELETE
        public long id = -1;      // event id
        public String planetName; // planet name
    }
}
