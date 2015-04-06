package com.ai.planetsdb.activity;


import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Fragment;
import android.content.AsyncQueryHandler;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.ai.planetsdb.R;
import com.ai.planetsdb.activity.EditPlanetActivity.EventInfo;
import com.ai.planetsdb.model.PlanetModel;
import com.ai.planetsdb.provider.PlanetsContract;
import com.ai.planetsdb.service.PlanetSaveService;
import com.ai.planetsdb.util.Utils;
import com.ai.planetsdb.views.EditPlanetView;

import java.lang.ref.WeakReference;

public class EditPlanetFragment extends Fragment {

    private static final String TAG = EditPlanetFragment.class.getSimpleName();
    private static final boolean DEBUG = false;

//    private static final String BUNDLE_KEY_ACTIVITY_ID = "activityId";
//    private static final String BUNDLE_KEY_IS_EDIT_ENABLED = "isEditEnabled";
//    private static final String BUNDLE_KEY_IS_INIT_DATA_LOADED = "isInitDataLoaded";
//    private static final String BUNDLE_KEY_INIT_PLANET_NAME = "initialPlanetName";

    public static final String ARGUMENT1 = "arg1";
    public static final String ARGUMENT2 = "arg2";

    private static final String BUNDLE_KEY_MODEL = "model";
    private static final String BUNDLE_KEY_ORIGINAL_MODEL = "original_model";
    private static final String BUNDLE_KEY_INIT_STATE = "key_init_state";
    private static final String BUNDLE_KEY_EDIT_STATE = "key_edit_state";
    private static final String BUNDLE_KEY_EVENT = "key_event";

    // Just for possible extensions in future
    private static final int TOKEN_PLANET = 1;

    private static final int TOKEN_ALL = TOKEN_PLANET;
    private static final int TOKEN_UNITIALIZED = 1 << 31;

    /**
     * A bitfield of TOKEN_* to keep track which query hasn't been completed
     * yet. Once all queries have returned, the model can be applied to the
     * view. (Just for possible extension in future).
     */
    private int mOutstandingQueries = TOKEN_UNITIALIZED;
    private final Object queryLock = new Object();

    EditPlanetHelper mHelper;
    PlanetModel mModel, mOriginalModel;
    PlanetModel mRestoreModel, mRestoreOriginalModel;
    EditPlanetView mView;
    QueryHandler mHandler;

    int mModification = Utils.MODIFY_UNINITIALIZED;

    private EventInfo mEvent;

    private EditPlanetActivity mActivity;
//  private InputMethodManager mInputMethodManager;
    private final Done mOnDone = new Done();

    @SuppressWarnings("unused")
    private boolean mSaveOnDetach = true;
    private boolean mIsEditable = true;

    public EditPlanetFragment() {
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);

        mEvent = (EventInfo) getArguments().getSerializable(ARGUMENT1);
        if (mEvent == null) {
            Log.e(TAG, "Required parameter is missed.");
        }
        Intent intent = getArguments().getParcelable(ARGUMENT2);

        mActivity = (EditPlanetActivity) activity;
        mHelper = new EditPlanetHelper((EditPlanetActivity) activity);
        mHandler = new QueryHandler(new WeakReference<>(queryListener), activity.getContentResolver());
        mModel = new PlanetModel(intent);
        PlanetSaveService.registerListener(saveServiceListener);

/*      mInputMethodManager = (InputMethodManager)
                activity.getSystemService(Context.INPUT_METHOD_SERVICE);*/
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (savedInstanceState != null) {
            if (savedInstanceState.containsKey(BUNDLE_KEY_MODEL)) {
                mRestoreModel = savedInstanceState.getParcelable(
                        BUNDLE_KEY_MODEL);
            }
            if (savedInstanceState.containsKey(BUNDLE_KEY_ORIGINAL_MODEL)) {
                mOriginalModel = savedInstanceState.getParcelable(
                        BUNDLE_KEY_ORIGINAL_MODEL);
            }
            if (savedInstanceState.containsKey(BUNDLE_KEY_INIT_STATE)) {
                mModification = savedInstanceState.getInt(BUNDLE_KEY_INIT_STATE);
            }
            if (savedInstanceState.containsKey(BUNDLE_KEY_EDIT_STATE)) {
                mIsEditable = savedInstanceState.getBoolean(BUNDLE_KEY_EDIT_STATE);
            }
            if (savedInstanceState.containsKey(BUNDLE_KEY_EVENT)) {
                mEvent = (EventInfo) savedInstanceState.getSerializable(BUNDLE_KEY_EVENT);
            }
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        @SuppressLint("InflateParams") View view = inflater.inflate(
                R.layout.edit_planet, null);
        mView = new EditPlanetView(mActivity, view);
        mView.setViewListener(viewListener);
        // TODO: add different activity labels for different events:
        // Edit, Insert, Delete
        switch (mEvent.eventType) {
            case Intent.ACTION_EDIT:
                mView.setOkButtonLabel(getResources().getString(R.string.save));
                break;

            case Intent.ACTION_INSERT:
                mView.setOkButtonLabel(getResources().getString(R.string.insert));
                break;

            case Intent.ACTION_DELETE:
                mView.setOkButtonLabel(getResources().getString(R.string.delete));
                break;

            default:
                Log.e(TAG, "Wrong event type");
                break;
        }

        startQuery();

        return view;
    }


    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        PlanetSaveService.registerListener(saveServiceListener);
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    public void onPause() {
        // TODO: is save needed when user pressed back/home? delete mSaveOnDetach flag field?
/*
        Activity act = getActivity();
        if (mSaveOnDetach && act != null && !act.isChangingConfigurations()) {
            mOnDone.setDoneCode(Utils.DONE_SAVE);
            mOnDone.run();
        }
*/
        super.onPause();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
    }

    @Override
    public void onDestroy() {
        if (mView != null) {
            mView.setModel(null);
        }
        PlanetSaveService.unregisterListener(saveServiceListener);

        super.onDestroy();
    }

    /**
     * This is how we receive events from the view.
     * The view takes user actions
     * The controller/activity responds to user actions
     */
    private EditPlanetView.ViewListener viewListener = new EditPlanetView.ViewListener() {

        @Override
        public void onOkClick() {
            if (mView != null) {
                if (mEvent.eventType.equals(Intent.ACTION_DELETE)) {
                    mOnDone.setDoneCode(Utils.DONE_DELETE);
                } else {
                    mOnDone.setDoneCode(Utils.DONE_SAVE | Utils.DONE_EXIT);
                }
                mOnDone.run();
            } else {
                mOnDone.setDoneCode(Utils.DONE_EXIT);
                mOnDone.run();
            }
        }

        @Override
        public void onCancelClick() {
            mOnDone.setDoneCode(Utils.DONE_EXIT);
            mOnDone.run();
        }

        @Override
        public void onEditorsValidate() {
            if (mModel == null) {
                Log.e(TAG, "Model is missed.");
                return;
            }

            mView.setValidity(mModel.getValidity());
        }
    };

    private QueryHandler.Listener queryListener = new QueryHandler.Listener() {

        @Override
        public void onQueryCompleted(int token, Object cookie, Cursor cursor) {
            // If the query didn't return a cursor for some reason return
            if (cursor == null) {
                return;
            }

            // If the Activity is finishing, then close the cursor.
            // Otherwise, use the new cursor in the adapter.
            final Activity activity = EditPlanetFragment.this.getActivity();
            if (activity == null || activity.isFinishing()) {
                cursor.close();
                return;
            }
            switch (token) {
                case TOKEN_PLANET:
                    if (cursor.getCount() == 0) {
                        // The cursor is empty. This can happen if the planet
                        // was deleted.
                        cursor.close();
                        mOnDone.setDoneCode(Utils.DONE_EXIT);
                        mSaveOnDetach = false;
                        mOnDone.run();
                        return;
                    }
                    mOriginalModel = new PlanetModel();
                    EditPlanetHelper.setModelFromCursor(mOriginalModel, cursor);
                    EditPlanetHelper.setModelFromCursor(mModel, cursor);
                    cursor.close();

//                    mOriginalModel.mUri = mUri.toString();
//                    mModel.mUri = mUri.toString();

                    setModelIfDone(TOKEN_PLANET);
                    break;

                default:
                    cursor.close();
                    break;
            }
        }
    };

    private PlanetSaveService.Listener saveServiceListener = new PlanetSaveService.Listener() {

        @Override
        public void onServiceCompleted(Intent callbackIntent) {
            if (callbackIntent.getAction().equals(
                        Utils.ACTION_SAVE_OR_DELETE_COMPLETED)
                    && mActivity != null
                    && !mActivity.isFinishing()
                    && callbackIntent.getIntExtra(
                        EditPlanetActivity.ACTIVITY_ID, -1) ==
                        mActivity.getActivityId()) {

                boolean serviceSucceeded = callbackIntent.getBooleanExtra(
                        PlanetSaveService.EXTRA_SERVICE_SUCCEEDED, false);

                // All results already were presented to the user as toast messages
                // by PlanetSaveService.
                // Now we just finish activity if action succeeded
                if (serviceSucceeded) {
                    mOnDone.setDoneCode(Utils.DONE_EXIT);
                    mOnDone.run();
                } else {
                    mIsEditable = true;
                    mView.setViewStates(mModification);
                    if (mEvent.eventType.equals(Intent.ACTION_DELETE)) {
                        mView.setEditable(false);
                    }
                }
            }
        }
    };

    DeletePlanetHelper.DeleteNotifyListener deleteNotifyListener =
            new DeletePlanetHelper.DeleteNotifyListener() {

                @Override
                public void onDeleteStarted() {
                    mView.setViewStates(Utils.MODIFY_UNINITIALIZED);
                    mIsEditable = false;
                }
            };

    @Override
    public void onSaveInstanceState(Bundle outState) {
        mView.prepareForSave();
        outState.putParcelable(BUNDLE_KEY_MODEL, mModel);
        outState.putParcelable(BUNDLE_KEY_ORIGINAL_MODEL, mOriginalModel);
        outState.putInt(BUNDLE_KEY_INIT_STATE, mModification);
        outState.putBoolean(BUNDLE_KEY_EDIT_STATE, mIsEditable);
        outState.putSerializable(BUNDLE_KEY_EVENT, mEvent);
    }

    // Could be used with implemented controller
/*  @Override
    public long getSupportedEventTypes() {
        return EventType.USER_HOME;
    }

    @Override
    public void handleEvent(EventInfo event) {
        // It's currently unclear if we want to save the event or not when home
        // is pressed. When creating a new event we shouldn't save since we
        // can't get the id of the new event easily.
        if ((false && event.eventType == EventType.USER_HOME) || (event.eventType == EventType.GO_TO
                && mSaveOnDetach)) {
            if (mView != null && mView.prepareForSave()) {
                mOnDone.setDoneCode(Utils.DONE_SAVE);
                mOnDone.run();
            }
        }
    }*/

    private void setModelIfDone(int queryType) {
        synchronized (queryLock) {
            mOutstandingQueries &= ~queryType;
        }

        if (mOutstandingQueries == 0) {
            setModelWhenDone();
        }
    }

    private void setModelWhenDone() {
        if (mRestoreModel != null) { mModel = mRestoreModel; }
        if (mRestoreOriginalModel != null) { mOriginalModel = mRestoreOriginalModel; }
        if (mModification == Utils.MODIFY_UNINITIALIZED) {
            mModification = Utils.MODIFY_ALL;
        }
        mView.setModel(mModel);
        if (mIsEditable) {
            mView.setModification(mModification);
        } else {
            mView.setModification(Utils.MODIFY_UNINITIALIZED);
        }
        mView.setValidity(mModel.getValidity());

        if (mEvent.eventType.equals(Intent.ACTION_DELETE)) {
            mView.setEditable(false);
        }
    }

    private void startQuery() {
        Uri uri = null;
        if (mEvent != null) {
            if (mEvent.id != -1) {
                mModel.mId = mEvent.id;
                uri = ContentUris.withAppendedId(
                        PlanetsContract.CONTENT_URI_PLANETS, mEvent.id);
            }
        }

        // Kick off the query for the planet
        boolean newPlanet = uri == null;
        if (!newPlanet && mModification == Utils.MODIFY_UNINITIALIZED) {
            mOutstandingQueries = TOKEN_ALL;
            if (DEBUG) {
                Log.d(TAG, "startQuery: uri for planet is " + uri.toString());
            }
            mHandler.startQuery(TOKEN_PLANET, null, uri, EditPlanetHelper.PLANET_PROJECTION,
                    null /* selection */, null /* selection args */, null /* sort order */);
        } else {
            setModelWhenDone();
        }
    }

    private static class QueryHandler extends AsyncQueryHandler {

        public interface Listener {
            public void onQueryCompleted(int token, Object cookie, Cursor cursor);
        }

        private final WeakReference<Listener> mWeakListener;

        public QueryHandler(WeakReference<Listener> listener, ContentResolver cr) {
            super(cr);
            mWeakListener = listener;
        }

        @Override
        protected void onQueryComplete(int token, Object cookie, Cursor cursor) {
            Listener listener;

            if (mWeakListener != null
                    && (listener = mWeakListener.get()) != null) {
                listener.onQueryCompleted(token, cookie, cursor);
            }
        }
    }

    class Done implements EditPlanetHelper.EditDoneRunnable {
        private int mCode = -1;

        @Override
        public void setDoneCode(int code) {
            mCode = code;
        }

        @Override
        public void run() {
            // We only want this to get called once, either because the user
            // pressed back/home or one of the buttons on screen
            mSaveOnDetach = false;

            if (mModification == Utils.MODIFY_UNINITIALIZED) {
                mModification = Utils.MODIFY_ALL;
            }
            boolean isInsert = mEvent.eventType.equals(Intent.ACTION_INSERT);
            if ((mCode & Utils.DONE_SAVE) != 0
                    && mModel != null
                    && ((mOriginalModel != null && !isInsert)
                        || (mOriginalModel == null && isInsert))
                    && mView.prepareForSave()) {

                int validity = mModel.getValidity();
                if (validity != 0) {
                    mView.setValidity(validity);
                    return;
                }

                if (mHelper.savePlanet(mModel, mOriginalModel)) {
                    mView.setViewStates(Utils.MODIFY_UNINITIALIZED);
                    mIsEditable = false;
                    return;
                }
            }

            if ((mCode & Utils.DONE_DELETE) != 0 && mOriginalModel != null) {
                DeletePlanetHelper deleteHelper = new DeletePlanetHelper(mActivity);
                deleteHelper.registerDeleteNotifyListener(deleteNotifyListener);
                deleteHelper.delete(mOriginalModel);
                return;
            }

            if ((mCode & Utils.DONE_EXIT) != 0) {
                // This will exit the edit planet screen, should be called
                // when we want to return to the main planet view
                Activity a = EditPlanetFragment.this.getActivity();
                if (a != null) {
                    Intent returnIntent = new Intent();
                    a.setResult(Activity.RESULT_OK, returnIntent);
                    a.finish();
                }
            }

            // Hide a software keyboard so that user won't see it even after this Fragment's
            // disappearing.
/*            final View focusedView = mActivity.getCurrentFocus();
            if (focusedView != null) {
                mInputMethodManager.hideSoftInputFromWindow(focusedView.getWindowToken(), 0);
                focusedView.clearFocus();
            }*/
        }
    }
}