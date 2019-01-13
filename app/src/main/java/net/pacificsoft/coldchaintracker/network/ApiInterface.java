package net.pacificsoft.coldchaintracker.network;

import net.pacificsoft.coldchaintracker.models.Message;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.GET;

/**
 * Created by Ravi Tamada on 21/02/17.
 * www.androidhive.info
 */

public interface ApiInterface {
    @GET("inbox.json")
    Call<List<Message>> getInbox();
}
