package com.ai.planetsdb.provider;

import android.content.ContentValues;

import java.util.LinkedList;
import java.util.List;

/**
 * Helper class which provides default initial content for Planets database.
 */
public final class DefaultPlanets {

    public static final Iterable<ContentValues> sPLANETS_VALUES = buildPlanetValues();

    private static class Planet {
        private String planetName;
        private double distance;
        private String discoverer;
        private double diameter;
        private boolean hasAtmosphere;

        public Planet(String planetName, double distance, String discoverer,
                      double diameter, boolean hasAtmosphere) {
            this.planetName = planetName;
            this.distance = distance;
            this.discoverer = discoverer;
            this.diameter = diameter;
            this.hasAtmosphere = hasAtmosphere;
        }
    }

    private static Iterable<ContentValues> buildPlanetValues() {
        List<ContentValues> list = new LinkedList<>();
        Iterable<Planet> planets = createPlanets();

        ContentValues values;
        for (Planet planet: planets) {
            values = new ContentValues();
            values.put(PlanetsDatabase.PLANET_NAME, planet.planetName);
            values.put(PlanetsDatabase.DISTANCE_FROM_EARTH, planet.distance);
            values.put(PlanetsDatabase.DISCOVERER, planet.discoverer);
            values.put(PlanetsDatabase.DIAMETER, planet.diameter);
            values.put(PlanetsDatabase.HAS_ATMOSPHERE, planet.hasAtmosphere);
            list.add(values);
        }

        return list;
    }

    @SuppressWarnings("ConstantConditions")
    private static Iterable<Planet> createPlanets() {
        List<Planet> planets = new LinkedList<>();
        String planetName;
        double distance;
        String discoverer;
        double diameter;
        boolean hasAtmosphere;

        planetName = "Jupiter";
        distance = 628311058.0;
        discoverer = "Babylonian astronomers";
        diameter = 142984.0;
        hasAtmosphere = false;
        planets.add(new Planet(planetName, distance, discoverer, diameter, hasAtmosphere));

        planetName = "Neptune";
        distance = 4347314131.0;
        discoverer = "Urbain Le Verrier & Johann Galle";
        diameter = 49528.0;
        hasAtmosphere = false;
        planets.add(new Planet(planetName, distance, discoverer, diameter, hasAtmosphere));

        planetName = "Mars";
        distance = 77790893.0;
        discoverer = "Egyptian astronomers";
        diameter = 6805.0;
        hasAtmosphere = true;
        planets.add(new Planet(planetName, distance, discoverer, diameter, hasAtmosphere));

        planetName = "Mercury";
        distance = 49367297.0;
        discoverer = "Assyrian astronomers";
        diameter = 4879.0;
        hasAtmosphere = false;
        planets.add(new Planet(planetName, distance, discoverer, diameter, hasAtmosphere));

        planetName = "Saturn";
        distance = 1277565818.0;
        discoverer = "Assyrians";
        diameter = 120536.0;
        hasAtmosphere = false;
        planets.add(new Planet(planetName, distance, discoverer, diameter, hasAtmosphere));

        planetName = "Uranus";
        distance = 2713705380.0;
        discoverer = "William Herschel";
        diameter = 51118.0;
        hasAtmosphere = false;
        planets.add(new Planet(planetName, distance, discoverer, diameter, hasAtmosphere));

        planetName = "Venus";
        distance =  41887404.0;
        discoverer = "Babylonian astronomers";
        diameter = 12104.0;
        hasAtmosphere = false;
        planets.add(new Planet(planetName, distance, discoverer, diameter, hasAtmosphere));

        return planets;
    }

    private DefaultPlanets() {}
}