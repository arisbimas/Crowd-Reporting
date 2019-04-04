package com.aris.crowdreporting;

import com.google.type.LatLng;

import java.util.Comparator;

public class SortPlaces implements Comparator<Near> {
    LatLng currentLoc;
    public Double lt;
    public Double lg;

    public SortPlaces(Double lt, Double lg){
        this.lt = lt;
        this.lg = lg;
    }

    @Override
    public int compare(final Near place1, final Near place2) {
        String lat1 = place1.getLatitude();
        String lon1 = place1.getLongitude();
        String lat2 = place2.getLatitude();
        String lon2 = place2.getLongitude();

        Double l1 = Double.parseDouble(lat1);
        Double lg1 = Double.parseDouble(lon1);
        Double l2 = Double.parseDouble(lat2);
        Double lg2 = Double.parseDouble(lon2);

        double distanceToPlace1 = distance(lt, lg, l1, lg1);
        double distanceToPlace2 = distance(lt, lg, l2, lg2);
        return (int) (distanceToPlace1 - distanceToPlace2);
    }

    public double distance(double fromLat, double fromLon, double toLat, double toLon) {
        double radius = 6378137;   // approximate Earth radius, *in meters*
        double deltaLat = toLat - fromLat;
        double deltaLon = toLon - fromLon;
        double angle = 2 * Math.asin( Math.sqrt(
                Math.pow(Math.sin(deltaLat/2), 2) +
                        Math.cos(fromLat) * Math.cos(toLat) *
                                Math.pow(Math.sin(deltaLon/2), 2) ) );
        return radius * angle;
    }
}