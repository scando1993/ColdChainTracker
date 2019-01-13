package net.pacificsoft.coldchaintracker.models;

import android.net.wifi.ScanResult;
import android.os.Parcel;
import android.os.Parcelable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class WifiData implements Parcelable {
    private List<WifiDataNetwork> mNetworks;
    private String name;
    private Integer id;

    public WifiData() {
        mNetworks = new ArrayList<>();
    }

    public WifiData(String name, Integer id) {
        this.name = name;
        this.id = id;
    }

    public WifiData(Parcel in) {
        in.readTypedList(mNetworks, WifiDataNetwork.CREATOR);
        name = in.readString();
        id = in.readInt();
    }

    public static final Parcelable.Creator<WifiData> CREATOR = new Parcelable.Creator<WifiData>() {
        public WifiData createFromParcel(Parcel in) {
            return new WifiData(in);
        }

        public WifiData[] newArray(int size) {
            return new WifiData[size];
        }
    };

    /**
     * Stores the last WiFi scan performed by {@link
     * } creating a {@link WifiDataNetwork()} object
     * for each network detected.
     *
     * @param results
     *            list of networks detected
     */
    public void addNetworks(List<ScanResult> results) {
        mNetworks.clear();
        for (ScanResult result : results) {
            mNetworks.add(new WifiDataNetwork(result));
        }
        Collections.sort(mNetworks);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeTypedList(mNetworks);
        dest.writeString(name);
        dest.writeInt(id);
    }

    /**
     * @return Returns a string containing a concise, human-readable description
     *         of this object.
     */
    @Override
    public String toString() {
        if (mNetworks == null || mNetworks.size() == 0)
            return "Empty data";
        else
            return mNetworks.size() + " networks data";
    }

    /**
     * @return Returns the list of scanned networks
     */
    public List<WifiDataNetwork> getNetworks() {
        return mNetworks;
    }
}
