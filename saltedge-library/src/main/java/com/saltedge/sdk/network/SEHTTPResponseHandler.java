/*
Copyright © 2015 Salt Edge. https://saltedge.com

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the Software is
furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in
all copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
THE SOFTWARE.
*/
package com.saltedge.sdk.network;

import com.loopj.android.http.JsonHttpResponseHandler;
import com.saltedge.sdk.utils.SEConstants;

import org.apache.http.Header;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;

public class SEHTTPResponseHandler extends JsonHttpResponseHandler {

    public interface RestAPIListener {
        void onFailureResponse(int statusCode, JSONObject errorResponse);

        void onSuccessResponse(int statusCode, JSONObject response);
    }

    private RestAPIListener responseListener;

    public SEHTTPResponseHandler(RestAPIListener responseListener) {
        super();
        this.responseListener = responseListener;
    }

    @Override
    public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONObject errorResponse) {
        super.onFailure(statusCode, headers, throwable, errorResponse);
        if (errorResponse != null) {
            handleFailure(statusCode, errorResponse);
        } else {
            handleFailure(statusCode, (Exception) throwable);
        }
    }

    @Override
    public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
        super.onSuccess(statusCode, headers, response);
        if (responseListener == null) {
            return;
        }
        responseListener.onSuccessResponse(statusCode, response);
    }

    @Override
    public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
        super.onSuccess(statusCode, headers, responseBody);
        if (responseBody == null || responseBody.length == 0) {
            return;
        }
        try {
            String resultString = new String(responseBody, "UTF-8");
            if (resultString.startsWith("{")) {
                return;
            }
            JSONObject resultJson = new JSONObject();
            resultJson.put(SEConstants.KEY_DATA, resultString);
            onSuccess(statusCode, headers, resultJson);
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private void handleFailure(int statusCode, JSONObject response) {
        sendFailureResponse(statusCode, response);
    }

    private void handleFailure(int statusCode, Exception response) {
        sendFailureResponse(statusCode, null);
    }

    private void sendFailureResponse(int statusCode, JSONObject response) {
        if (responseListener != null) {
            responseListener.onFailureResponse(statusCode, response);
        }
    }

}