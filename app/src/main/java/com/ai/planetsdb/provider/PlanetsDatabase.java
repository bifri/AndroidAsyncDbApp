package com.ai.planetsdb.provider;

import android.content.ContentValues;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * Helper for managing {@link android.database.sqlite.SQLiteDatabase}
 * that stores data for {@link PlanetsProvider}.
 *
 * DO NOT call this class directly, use PlanetsProvider instead.
 */
public class PlanetsDatabase extends SQLiteOpenHelper {

    @SuppressWarnings("UnusedDeclaration")
    private static final String TAG = PlanetsDatabase.class.getSimpleName();

    private static final String DB_NAME = "planets.db";
    private static final int DB_VERSION = 1;

    // Tables
    static final String TABLE_PLANETS = "planets";

    // Column names
    static final String _ID = "_id";
    static final String PLANET_NAME = "mPlanetName";
    static final String DISTANCE_FROM_EARTH = "distanceFromEarth";
    static final String DISCOVERER = "mDiscoverer";
    static final String DIAMETER = "mDiameter";
    static final String HAS_ATMOSPHERE = "hasAtmosphere";

    public PlanetsDatabase(Context context) {
        super(context, DB_NAME, null, DB_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        createTablePlanets(db);
        insertDefaultValuesInPlanets(db);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS "
                + TABLE_PLANETS);
        onCreate(db);
    }

    private void createTablePlanets(SQLiteDatabase db) {
        String CREATE_TABLE_PLANETS = "CREATE TABLE "
                + TABLE_PLANETS + " ("
                + _ID + " INTEGER PRIMARY KEY, "
                + PLANET_NAME + " TEXT NOT NULL, "
                + DISTANCE_FROM_EARTH + " REAL, "
                + DISCOVERER + " TEXT, "
                + DIAMETER + " REAL, "
                + HAS_ATMOSPHERE + " INTEGER, "
                + "UNIQUE (" + PLANET_NAME + ") ON CONFLICT ABORT)";
/*        String CREATE_TABLE_PLANETS = "CREATE TABLE "
                + TABLE_PLANETS + " ("
                + _ID + " INTEGER PRIMARY KEY, "
                + PLANET_NAME + " TEXT NOT NULL, "
                + DISTANCE_FROM_EARTH + " REAL, "
                + DISCOVERER + " TEXT, "
                + DIAMETER + " REAL, "
                + HAS_ATMOSPHERE + " INTEGER)";*/
        db.execSQL(CREATE_TABLE_PLANETS);
    }

    private void insertDefaultValuesInPlanets(SQLiteDatabase db) {
        for(ContentValues values: DefaultPlanets.sPLANETS_VALUES) {
            db.insertOrThrow(TABLE_PLANETS, null, values);
        }
    }
}

