package com.neu.info7255.bigdata_proj.service;

import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier;
import com.google.api.client.http.apache.ApacheHttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.jackson2.JacksonFactory;
import org.springframework.stereotype.Service;

import java.util.Collections;

@Service
public class AuthorizationService {

    //parse client id
    private static final JacksonFactory jacksonFactory = new JacksonFactory();

    // google.client-id
    private String GOOGLE_CLIENT_ID = "232746207217-g92g9ik894s20or6qve0klgormhaph07.apps.googleusercontent.com";

    GoogleIdTokenVerifier verifier = new GoogleIdTokenVerifier.Builder(new ApacheHttpTransport(), jacksonFactory)
            .setAudience(Collections.singletonList(GOOGLE_CLIENT_ID))
            // Or, if multiple clients access the backend:
            //.setAudience(Arrays.asList(CLIENT_ID_1, CLIENT_ID_2, CLIENT_ID_3))
            .build();

    //verify google token
    public boolean authorize(String idTokenString) {

        try {
            GoogleIdToken idToken = verifier.verify(idTokenString);
            if (idToken != null) return true;
            return false;
        }
        catch (Exception e) {
            e.printStackTrace();
            return false;}
    }
}