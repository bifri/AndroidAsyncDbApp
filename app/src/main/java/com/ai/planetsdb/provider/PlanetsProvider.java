/*
 * Simplified modification of
 * https://github.com/google/iosched/blob/aacd2a01c13c91ea82736cf66ac5776db266b2e6/
 * android/src/main/java/com/google/samples/apps/iosched/provider/ScheduleProvider.java
 */

package com.ai.planetsdb.provider;

import android.app.Activity;
import android.content.ContentProvider;
import android.content.ContentProviderOperation;
import android.content.ContentProviderResult;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.OperationApplicationException;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.text.TextUtils;

import com.ai.planetsdb.util.SelectionBuilder;

import java.util.ArrayList;

/**
 * Provider that stores {@link PlanetsContract} data. Data is usually queried
 * by various {@link Activity} instances.
 */
public class PlanetsProvider extends ContentProvider {

    @SuppressWarnings("unused")
    private static final String TAG = PlanetsProvider.class.getSimpleName();

    private PlanetsDatabase mOpenHelper;

    // setup UriMatcher
    private static final UriMatcher sUriMatcher = buildUriMatcher();

    private static final int PLANETS = 10;
    private static final int PLANETS_ID = 11;

    /**
     * Build and return a {@link UriMatcher} that catches all {@link Uri}
     * variations supported by this {@link ContentProvider}.
     */
    private static UriMatcher buildUriMatcher() {
        final UriMatcher matcher = new UriMatcher(UriMatcher.NO_MATCH);
        final String authority = PlanetsContract.AUTHORITY;
        matcher.addURI(authority, PlanetsContract.PATH_PLANETS, PLANETS);
        matcher.addURI(authority, PlanetsContract.PATH_PLANETS + "/#", PLANETS_ID);
        return matcher;
    }

    @Override
    public boolean onCreate() {
        mOpenHelper = new PlanetsDatabase(getContext());
        return true;
    }

    @Override
    public String getType(Uri uri) {
        final int match = sUriMatcher.match(uri);
        switch (match) {
            case PLANETS:
                return PlanetsContract.CONTENT_DIR_TYPE_PLANETS;
            case PLANETS_ID:
                return PlanetsContract.CONTENT_ITEM_TYPE_PLANETS;
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }
    }

    @Override
    public Cursor query(Uri uri, String[] projection, String selection,
                        String[] selectionArgs, String sortOrder) {

        final SQLiteDatabase db = mOpenHelper.getReadableDatabase();
        final SelectionBuilder builder = buildSimpleSelection(uri);

        boolean distinct = !TextUtils.isEmpty(
                uri.getQueryParameter(PlanetsContract.QUERY_PARAMETER_DISTINCT));

        Cursor cursor = builder
                .where(selection, selectionArgs)
                .query(db, distinct, projection, sortOrder, null);
        Context context = getContext();
        if (null != context) {
            cursor.setNotificationUri(context.getContentResolver(), uri);
        }
        return cursor;
    }

    @Override
    public Uri insert(Uri uri, ContentValues values) {
        final int match = sUriMatcher.match(uri);
        switch (match) {
            case PLANETS:
                long rowID = mOpenHelper.getWritableDatabase().insert(
                        PlanetsDatabase.TABLE_PLANETS, null, values);
                if (rowID > 0) {
                    Uri fullUri = ContentUris.withAppendedId(
                            PlanetsContract.CONTENT_URI_PLANETS, rowID);
                    getContext().getContentResolver().notifyChange(fullUri, null);
                    return fullUri;
                }
                throw new SQLException("Failed to add record into" + uri);
            default:
                throw new UnsupportedOperationException("Unknown insert uri: " + uri);
        }
    }

    @Override
    public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {

        final SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        final SelectionBuilder builder = buildSimpleSelection(uri);

        int retVal = builder.where(selection, selectionArgs).update(db, values);
        getContext().getContentResolver().notifyChange(uri, null);
        return retVal;
    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {

        final SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        final SelectionBuilder builder = buildSimpleSelection(uri);
        int retVal = builder.where(selection, selectionArgs).delete(db);
        getContext().getContentResolver().notifyChange(uri, null);
        return retVal;
    }

    /**
     * Apply the given set of {@link android.content.ContentProviderOperation}, executing inside
     * a {@link SQLiteDatabase} transaction. All changes will be rolled back if
     * any single one fails.
     */
    @Override
    public ContentProviderResult[] applyBatch(@NonNull ArrayList<ContentProviderOperation> operations)
            throws OperationApplicationException {
        final SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        db.beginTransaction();
        try {
            final int numOperations = operations.size();
            final ContentProviderResult[] results = new ContentProviderResult[numOperations];
            for (int i = 0; i < numOperations; i++) {
                results[i] = operations.get(i).apply(this, results, i);
            }
            db.setTransactionSuccessful();
            return results;
        } finally {
            db.endTransaction();
        }
    }

    /**
     * Build a simple {@link SelectionBuilder} to match the requested
     * {@link Uri}. This is usually enough to support {@link #insert},
     * {@link #update}, and {@link #delete} operations.
     */
    private SelectionBuilder buildSimpleSelection(Uri uri) {
        final SelectionBuilder builder = new SelectionBuilder();
        final int match = sUriMatcher.match(uri);

        switch (match) {
            case PLANETS: {
                return builder.table(PlanetsDatabase.TABLE_PLANETS);
            }
            case PLANETS_ID: {
                final String blockId = uri.getLastPathSegment();
                return builder.table(PlanetsDatabase.TABLE_PLANETS)
                        .where(PlanetsDatabase._ID + "=?", blockId);
            }
            default: {
                throw new UnsupportedOperationException("Unknown uri for " + match + ": " + uri);
            }
        }
    }
}