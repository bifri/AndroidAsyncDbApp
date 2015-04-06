package com.ai.planetsdb.activity;


import android.app.AlertDialog;
import android.content.ContentUris;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;

import com.ai.planetsdb.R;
import com.ai.planetsdb.model.PlanetModel;
import com.ai.planetsdb.provider.PlanetsContract;
import com.ai.planetsdb.service.PlanetSaveService;
import com.ai.planetsdb.util.Utils;

/**
 * A helper class for deleting planets.
 *
 * <p>
 * To use this class, create an instance, passing in the parent activity
 * and a boolean that determines if the parent activity should exit if the
 * event is deleted.  Then to use the instance, call one of the
 * {@link delete()} methods on this class.
 *
 * An instance of this class may be created once and reused (by calling
 * {@link #delete()} multiple times).
 */
public class DeletePlanetHelper {

    @SuppressWarnings("unused")
    private static final String TAG = DeletePlanetHelper.class.getSimpleName();

    private EditPlanetActivity mContext;

    private PlanetModel mModel;
    private DeleteNotifyListener mDeleteStartedListener = null;

    public interface DeleteNotifyListener {
        public void onDeleteStarted();
    }

    public DeletePlanetHelper(EditPlanetActivity context) {
        mContext = context;
    }

    /**
     * This callback is used when planet is deleted.
     */
    private DialogInterface.OnClickListener mDeleteDialogListener =
            new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int button) {
                    deleteStarted();
                    long id = mModel.mId;
                    Uri uri = ContentUris.withAppendedId(
                            PlanetsContract.CONTENT_URI_PLANETS, id);

                    Intent intent = PlanetSaveService.createDeletePlanetIntent(
                            mContext, uri,
                            mContext.getClass(), mContext.getActivityId(),
                            Utils.ACTION_SAVE_OR_DELETE_COMPLETED);

                    mContext.startService(intent);
                }
            };


    private DialogInterface.OnClickListener mCancelDialogListener =
            new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int button) {
                    dialog.dismiss();
                }
            };

    private void deleteStarted() {
        if (mDeleteStartedListener != null) {
            mDeleteStartedListener.onDeleteStarted();
        }
    }

    /**
     * Does the required processing for deleting a planet. This method
     * takes a {@link com.ai.planetsdb.model.PlanetModel} object, which must have a valid
     * uri for referencing the planet in the database and have the required
     * fields listed below.
     * The required fields are:
     * <p/>
     * <ul>
     * <li> Planet._ID </li>
     * </ul>
     * <p/>
     * If the planet no longer exists in the db this will still prompt
     * the user but will return without modifying the db after the query
     * returns.
     *
     * @param cursor the database cursor containing the required fields
     */
    public void delete(PlanetModel model) {
        mModel = model;

        AlertDialog dialog = new AlertDialog.Builder(mContext)
                .setMessage(R.string.delete_this_planet_title)
                .setIconAttribute(android.R.attr.alertDialogIcon)
                .setNegativeButton(R.string.cancel, mCancelDialogListener)
                .setPositiveButton(R.string.ok, mDeleteDialogListener)
                .create();
        dialog.show();
    }

    public void registerDeleteNotifyListener(DeleteNotifyListener listener) {
        mDeleteStartedListener = listener;
    }
}