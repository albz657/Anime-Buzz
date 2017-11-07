package me.jakemoritz.animebuzz.network;

import android.support.annotation.NonNull;
import android.util.Base64;

import java.io.IOException;

import okhttp3.Interceptor;
import okhttp3.Request;
import okhttp3.Response;

public class MalHeader implements Interceptor {

    private static MalHeader malHeader;

    private String username;
    private String password;

    public synchronized static MalHeader getInstance(){
        if (malHeader == null){
            malHeader = new MalHeader();
        }

        return malHeader;
    }

    @Override
    public Response intercept(@NonNull Chain chain) throws IOException {
        String credentials = username + ":" + password;
        String basic = "Basic " + Base64.encodeToString(credentials.getBytes(), Base64.NO_WRAP);

        Request original = chain.request();
        Request.Builder requestBuilder = original.newBuilder()
                .header("Authorization", basic)
                .header("Accept", "application/xml")
                .method(original.method(), original.body());

        Request request = requestBuilder.build();
        return chain.proceed(request);
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}
