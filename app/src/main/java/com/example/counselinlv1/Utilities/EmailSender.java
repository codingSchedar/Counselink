package com.example.counselinlv1.Utilities;

import okhttp3.*;
import org.json.JSONArray;
import org.json.JSONObject;
import java.io.IOException;

public class EmailSender {

    private static final String SENDGRID_API_KEY = System.getenv("SENDGRID_API_KEY");
    private static final String SENDGRID_API_URL = "https://api.sendgrid.com/v3/mail/send";

    public static void sendOtpEmail(String recipientEmail, String subject, String content, final EmailCallback callback) {
        OkHttpClient client = new OkHttpClient();

        JSONObject emailData = new JSONObject();
        try {
            // Build the email payload
            JSONObject to = new JSONObject();
            to.put("email", recipientEmail);

            JSONArray personalizations = new JSONArray();
            JSONObject personalizationObject = new JSONObject();
            personalizationObject.put("to", new JSONArray().put(to));
            personalizations.put(personalizationObject);

            emailData.put("personalizations", personalizations);
            emailData.put("from", new JSONObject().put("email", "wczsantiago@gmail.com")); // Replace with verified email
            emailData.put("subject", subject);
            emailData.put("content", new JSONArray().put(new JSONObject()
                    .put("type", "text/plain")
                    .put("value", content)));

        } catch (Exception e) {
            e.printStackTrace();
            callback.onFailure(e);
            return;
        }


        RequestBody body = RequestBody.create(emailData.toString(), MediaType.get("application/json"));


        Request request = new Request.Builder()
                .url(SENDGRID_API_URL)
                .post(body)
                .addHeader("Authorization", "Bearer " + SENDGRID_API_KEY)
                .addHeader("Content-Type", "application/json")
                .build();


        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                e.printStackTrace(); // Log the failure
                callback.onFailure(e);
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (response.isSuccessful()) {
                    callback.onSuccess("Email sent successfully!");
                } else {
                    String errorBody = response.body().string();  // Read the response error
                    System.out.println("Error Response: " + errorBody);  // Log the error response
                    callback.onFailure(new Exception("Error: " + errorBody));
                }
            }
        });
    }

    public interface EmailCallback {
        void onSuccess(String message);
        void onFailure(Exception e);
    }
}
