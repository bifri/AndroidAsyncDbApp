package com.ai.planetsdb.activity;


import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.text.TextUtils;
import android.util.Log;

import com.ai.planetsdb.model.PlanetModel;
import com.ai.planetsdb.provider.PlanetsContract;
import com.ai.planetsdb.service.PlanetSaveService;
import com.ai.planetsdb.util.Utils;

public class EditPlanetHelper {

    private static final String TAG = EditPlanetHelper.class.getSimpleName();

    public static final String[] PLANET_PROJECTION = new String[]{
            PlanetsContract._ID,                 // 0
            PlanetsContract.PLANET_NAME,         // 1
            PlanetsContract.DISTANCE_FROM_EARTH, // 2
            PlanetsContract.DISCOVERER,          // 3
            PlanetsContract.DIAMETER,            // 4
            PlanetsContract.HAS_ATMOSPHERE,      // 5
    };
    private static final int PLANET_INDEX_ID = 0;
    private static final int PLANET_INDEX_NAME = 1;
    private static final int PLANET_INDEX_DISTANCE = 2;
    private static final int PLANET_INDEX_DISCOVERER = 3;
    private static final int PLANET_INDEX_DIAMETER = 4;
    private static final int PLANET_INDEX_HAS_ATMOSPHERE = 5;

    private final EditPlanetActivity mContext;


    public EditPlanetHelper(EditPlanetActivity context) {
        mContext = context;
    }

    /**
     * Saves the planet. Returns true if the planet was successfully queued
     * for saving, false otherwise.
     *
     * @param model         The planet model to save
     * @param originalModel A model of the original planet if it exists
     * @param modifyWhich   A modification status
     * @return true if the planet was successfully queued for saving
     */
    public boolean savePlanet(PlanetModel model, PlanetModel originalModel) {

        // It's a problem if we try to save a non-existent or invalid model or
        // if we're modifying an existing planet and we have the wrong original model
        if (model == null) {
            Log.e(TAG, "Attempted to save null model.");
            return false;
        }

        if (originalModel != null && !isSamePlanet(model, originalModel)) {
            Log.e(TAG, "Attempted to update existing planet but models didn't" +
                    " refer to the same planet.");
            return false;
        }
        if (originalModel != null && model.isUnchanged(originalModel)) {
            return false;
        }

        if (model.mId != -1 && originalModel == null) {
            Log.e(TAG, "Existing planet but no originalModel provided. Aborting save.");
            return false;
        }

        ContentValues values = getContentValuesFromModel(model);

        Uri uri = null;
        if (model.mId != -1) {
            uri = ContentUris.withAppendedId(
                    PlanetsContract.CONTENT_URI_PLANETS, model.mId);
        }

        if (uri == null) {
            Intent intent = PlanetSaveService.createInsertPlanetIntent(
                    mContext, values, mContext.getClass(),
                    mContext.getActivityId(),
                    Utils.ACTION_SAVE_OR_DELETE_COMPLETED);

            mContext.startService(intent);

/*            mService.startInsert(mService.getNextToken(), null,
                    PlanetsContract.CONTENT_URI_PLANETS, values, Utils.UNDO_DELAY);*/
        } else {

            Intent intent = PlanetSaveService.createUpdatePlanetIntent(
                    mContext, uri, values, originalModel.mName,
                    mContext.getClass(), mContext.getActivityId(),
                    Utils.ACTION_SAVE_OR_DELETE_COMPLETED);

            mContext.startService(intent);

/*            mService.startUpdate(mService.getNextToken(), null, uri, values,
                    null, null, Utils.UNDO_DELAY);*/
        }

        return true;
    }

    /**
     * Goes through a planet model and fills in content values for saving. This
     * method will perform the initial collection of values from the model and
     * put them into a set of ContentValues.
     *
     * @param model The complete model of the planet you want to save
     * @return values
     */
    private ContentValues getContentValuesFromModel(PlanetModel model) {

        ContentValues values = new ContentValues();

        String name = model.mName;
        if (!TextUtils.isEmpty(name)) {
            values.put(PlanetsContract.PLANET_NAME, name);
        }

        values.put(PlanetsContract.DISTANCE_FROM_EARTH, model.mDistance);

        String discoverer = model.mDiscoverer;
        if (discoverer != null) {
            values.put(PlanetsContract.DISCOVERER, discoverer);
        }

        values.put(PlanetsContract.DIAMETER, model.mDiameter);
        values.put(PlanetsContract.HAS_ATMOSPHERE, model.mHasAtmosphere ? 1 : 0);

        return values;
    }

    /**
     * Compares two models to ensure that they refer to the same planet. This is
     * a safety check to make sure an updated planet model refers to the same
     * planet as the original model. If the original model is null then this is a
     * new planet or we're forcing an overwrite so we return true in that case.
     * The important identifier is the Planet Id.
     *
     */
    public static boolean isSamePlanet(PlanetModel model, PlanetModel originalModel) {
        return originalModel == null || model.mId == originalModel.mId;
    }

    /**
     * Uses a planet cursor to fill in the given model. This method assumes the
     * cursor used {@link #PLANET_PROJECTION} as it's query projection. It uses
     * the cursor to fill in the given model with all the information available.
     *
     * @param model the model to fill in
     * @param cursor a planet cursor that used {@link #PLANET_PROJECTION} for the query
     */
    public static void setModelFromCursor(PlanetModel model, Cursor cursor) {
        if (model == null || cursor == null || cursor.getCount() != 1) {
            Log.wtf(TAG, "Attempted to build non-existent model or from an incorrect query.");
            return;
        }

        model.clear();
        cursor.moveToFirst();

        model.mId = cursor.getInt(PLANET_INDEX_ID);
        model.mName = cursor.getString(PLANET_INDEX_NAME);
        model.mDistance = cursor.getDouble(PLANET_INDEX_DISTANCE);
        model.mDiscoverer = cursor.getString(PLANET_INDEX_DISCOVERER);
        model.mDiameter = cursor.getDouble(PLANET_INDEX_DIAMETER);
        model.mHasAtmosphere = cursor.getInt(PLANET_INDEX_HAS_ATMOSPHERE) > 0;
    }

    public interface EditDoneRunnable extends Runnable {
        @SuppressWarnings("unused")
        public void setDoneCode(int code);
    }
}