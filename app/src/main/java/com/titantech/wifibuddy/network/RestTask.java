package com.titantech.wifibuddy.network;

import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;

import com.titantech.wifibuddy.models.Constants;
import com.titantech.wifibuddy.network.requests.RestRequest;
import com.titantech.wifibuddy.parsers.ResultParser;

/**
 * Created by Robert on 23.01.2015.
 */
public class RestTask<T> extends AsyncTask<RestRequest, Void, T> {

    private Context context;
    private ResultParser<T> parser;
    private ResultListener<T> listener;

    public RestTask(Context context, ResultParser<T> parser, ResultListener<T> listener) {
        super();
        this.context = context;
        this.parser = parser;

        // Usually the service acts as a listener
        this.listener = listener;
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
        context.sendBroadcast(new Intent(Constants.TASK_STARTED));
    }


    @Override
    protected T doInBackground(RestRequest... params) {
        if (params.length > 0) {
            RestRequest request = params[0];
            try {
                String content = request.getJson();
                return parser.parseResult(content);
            } catch (Exception e) {
                Intent exceptionIntent = new Intent(Constants.TASK_EXCEPTION_OCCURRED);
                exceptionIntent.putExtra("exception", e);

                context.sendBroadcast(exceptionIntent);
                e.printStackTrace();
                return null;
            }
        } else {
            context.sendBroadcast(new Intent(Constants.TASK_URL_NOT_PROVIDED));
            return null;
        }
    }

    @Override
    protected void onPostExecute(T result) {
        super.onPostExecute(result);
        listener.onDownloadResult(result);
        context.sendBroadcast(new Intent(Constants.TASK_COMPLETED));
    }
}
