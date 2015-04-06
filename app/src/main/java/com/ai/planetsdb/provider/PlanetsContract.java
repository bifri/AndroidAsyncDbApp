package com.ai.planetsdb.provider;

import android.content.ContentResolver;
import android.net.Uri;

// Contract Class for accessing ContentResolver

public final class PlanetsContract {

    public static final String AUTHORITY = "com.ai.planetsdb.provider";
    static final String PATH_PLANETS = "planets";

    static final Uri BASE_URI = Uri.parse("content://" + AUTHORITY + "/");

    public static final Uri CONTENT_URI_PLANETS =
            Uri.withAppendedPath(BASE_URI, PATH_PLANETS);

    // Mime type for a directory of data items in planets table
    public static final String CONTENT_DIR_TYPE_PLANETS = ContentResolver.CURSOR_DIR_BASE_TYPE
            + "/vnd.com.ai.planetsdb.provider.planets";

    // Mime type for a single data item in planets table
    public static final String CONTENT_ITEM_TYPE_PLANETS = ContentResolver.CURSOR_ITEM_BASE_TYPE
            + "/vnd.com.ai.planetsdb.provider.planets";

    /**
     * Query parameter to create a distinct query.
     */
    public static final String QUERY_PARAMETER_DISTINCT = "distinct";

    // All columns of PLANETS table
    public static final String _ID = "_id";
    public static final String PLANET_NAME = "mPlanetName";
    public static final String DISTANCE_FROM_EARTH = "distanceFromEarth";
    public static final String DISCOVERER = "mDiscoverer";
    public static final String DIAMETER = "mDiameter";
    public static final String HAS_ATMOSPHERE = "hasAtmosphere";

    /**
     * This utility class cannot be instantiated
     */
    private PlanetsContract() {}
}

