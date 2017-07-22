package com.epam.service;

import com.google.api.client.auth.oauth2.BearerToken;
import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.client.util.DateTime;
import com.google.api.services.calendar.Calendar;
import com.google.api.services.calendar.model.Event;
import com.google.api.services.calendar.model.Events;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.Optional;

/**
 * Created by Adrian Ferenc on 22.07.2017.
 */
public class GoogleCalendarService {

    private Calendar getGoogleCalendar(String accessToken) throws GeneralSecurityException, IOException {
        Credential credential = new Credential(BearerToken.authorizationHeaderAccessMethod()).setAccessToken(accessToken);
        NetHttpTransport httpTransport = GoogleNetHttpTransport.newTrustedTransport();
        JsonFactory jsonFactory = JacksonFactory.getDefaultInstance();
        return new Calendar.Builder(httpTransport, jsonFactory, credential)
                .setApplicationName("Alexa and Google Calendar skill")
                .build();
    }

    public Optional<Event> getNextEvent(DateTime startDateTime, String accessToken) throws IOException, GeneralSecurityException {
        Calendar service =
                getGoogleCalendar(accessToken);

        Events events = service.events().list("primary")
                .setMaxResults(1)
                .setTimeMin(startDateTime)
                .setOrderBy("startTime")
                .setSingleEvents(true)
                .execute();
        return events.getItems().stream()
                .findFirst();
    }
}
