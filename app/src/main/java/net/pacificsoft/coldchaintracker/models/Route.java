package net.pacificsoft.coldchaintracker.models;

import android.content.Intent;
import android.os.Parcel;
import android.os.Parcelable;

import java.util.ArrayList;
import java.util.List;

public class Route implements Comparable<Route>, Parcelable {

    private List<WifiData> points;
    private String name;
    private String description;
    private Integer numberOfDevices;
    private Integer numberOfPoints;

    public Route(List<WifiData> points, String name, String description, Integer numberOfDevices, Integer numberOfPoints) {
        this.points = points;
        this.name = name;
        this.description = description;
        this.numberOfDevices = numberOfDevices;
        this.numberOfPoints = numberOfPoints;
    }

    public Route(String name, String description, Integer numberOfDevices, Integer numberOfPoints) {
        this.points = new ArrayList<>();
        this.name = name;
        this.description = description;
        this.numberOfDevices = numberOfDevices;
        this.numberOfPoints = numberOfPoints;
    }

    public Route(Parcel in) {
        in.readTypedList(points, WifiData.CREATOR);
        this.name = in.readString();
        this.description = in.readString();
        this.numberOfDevices = in.readInt();
        this.numberOfPoints = in.readInt();
    }

    public static final Parcelable.Creator<Route> CREATOR = new Creator<Route>() {
        @Override
        public Route createFromParcel(Parcel source) {
            return new Route(source);
        }

        @Override
        public Route[] newArray(int size) {
            return new Route[size];
        }
    };

    public void addPoints(List<WifiData> points){
        if(points.size() + this.points.size() <= this.numberOfPoints){
            this.points.addAll(points);
        }else if(points.size() <= this.numberOfPoints){
            for(int i = 0; this.points.size() <= this.numberOfPoints; i++){
                this.points.add(points.get(i));
            }
        }
    }

    public void addPoint(WifiData point){
        if (this.points.size() < this.numberOfPoints)
            this.points.add(point);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeTypedList(points);
        dest.writeString(name);
        dest.writeString(description);
        dest.writeInt(numberOfDevices);
        dest.writeInt(numberOfPoints);
    }

    @Override
    public int compareTo(Route o) {
        return 0;
    }

}
