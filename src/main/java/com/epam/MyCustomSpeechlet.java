package com.epam;

import com.amazon.speech.json.SpeechletRequestEnvelope;
import com.amazon.speech.slu.Intent;
import com.amazon.speech.speechlet.*;
import com.amazon.speech.ui.PlainTextOutputSpeech;
import com.amazon.speech.ui.Reprompt;
import com.epam.service.GoogleCalendarService;
import com.google.api.client.util.DateTime;
import com.google.api.services.calendar.model.Event;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Optional;

/**
 * Created by Adrian Ferenc on 22.07.2017.
 */
public class MyCustomSpeechlet implements SpeechletV2 {

    private Logger LOGGER = LoggerFactory.getLogger(MyCustomSpeechlet.class);

    private GoogleCalendarService googleCalendarService = new GoogleCalendarService();

    public void onSessionStarted(SpeechletRequestEnvelope<SessionStartedRequest> speechletRequestEnvelope) {
        LOGGER.info("onSessionStarted -> requestId = {} and sessionId = {}", speechletRequestEnvelope.getRequest().getRequestId(), speechletRequestEnvelope.getSession().getSessionId());
    }

    public SpeechletResponse onLaunch(SpeechletRequestEnvelope<LaunchRequest> speechletRequestEnvelope) {
        LOGGER.info("onLunch -> requestId = {} and sessionId = {}", speechletRequestEnvelope.getRequest().getRequestId(), speechletRequestEnvelope.getSession().getSessionId());
        String speechText = "Welcome to the Alexa Skills Kit";
        return getPlainTextResponse(speechText);
    }

    public SpeechletResponse onIntent(SpeechletRequestEnvelope<IntentRequest> speechletRequestEnvelope) {
        LOGGER.info("onIntent-> requestId = {} and sessionId = {}", speechletRequestEnvelope.getRequest().getRequestId(), speechletRequestEnvelope.getSession().getSessionId());
        return handleIntents(speechletRequestEnvelope);
    }

    public void onSessionEnded(SpeechletRequestEnvelope<SessionEndedRequest> speechletRequestEnvelope) {
        LOGGER.info("onSessionEnded -> requestId = {} and sessionId = {}", speechletRequestEnvelope.getRequest().getRequestId(), speechletRequestEnvelope.getSession().getSessionId());
    }

    private SpeechletResponse getPlainTextResponse(String speechText) {
        PlainTextOutputSpeech outputSpeech = new PlainTextOutputSpeech();
        outputSpeech.setText(speechText);

        Reprompt reprompt = new Reprompt();
        reprompt.setOutputSpeech(outputSpeech);

        return SpeechletResponse.newAskResponse(outputSpeech, reprompt);
    }

    public SpeechletResponse handleIntents(SpeechletRequestEnvelope<IntentRequest> speechletRequestEnvelope) {
        Intent intent = speechletRequestEnvelope.getRequest().getIntent();
        String intentName = intent != null ? intent.getName() : null;

        switch (intentName) {
            case "AMAZON.StopIntent":
            case "AMAZON.CancelIntent":
                return getStopResponse();
            case "AMAZON.HelpIntent":
                return getHelpResponse();
            case "HelloIntent":
                return getHelloResponse(intent);
            case "CalendarEventIntent":
                Session session = speechletRequestEnvelope.getSession();
                return getCalendarEventResponse(session);
            default:
                return getExceptionResponse("Invalid intent.");
        }
    }

    private SpeechletResponse getStopResponse() {
        return getPlainTextResponse("Good bye!");
    }

    private SpeechletResponse getHelpResponse() {
        return getPlainTextResponse("No help implemented. Sorry!");
    }

    private SpeechletResponse getExceptionResponse(String exceptionMessage) {
        return getPlainTextResponse(exceptionMessage);
    }

    private SpeechletResponse getHelloResponse(Intent intent) {
        String greeting = Optional.ofNullable(intent.getSlot("name").getValue()).orElse("World");

        PlainTextOutputSpeech outputSpeech = new PlainTextOutputSpeech();
        outputSpeech.setText(String.format("Hello {}", greeting));

        return SpeechletResponse.newTellResponse(outputSpeech);
    }

    private SpeechletResponse getCalendarEventResponse(Session session){
            String accessToken = session.getUser().getAccessToken();
            String speechText;
            DateTime now = new DateTime(System.currentTimeMillis());
            Optional<Event> optionalEvent;
            try {
                optionalEvent = googleCalendarService.getNextEvent(now, accessToken);
            } catch (IOException | GeneralSecurityException e) {
                return getExceptionResponse(e.getMessage());
            }
            if(optionalEvent.isPresent()){
                Event event = optionalEvent.get();
                StringBuilder sb = new StringBuilder();
                sb.append("Your next event starts at ");
                sb.append(convertDateTime(event.getStart().getDateTime()));
                sb.append(". ");
                sb.append("Event details: ");
                sb.append(event.getSummary());
                speechText = sb.toString();
            }
            else{
                speechText = "No more events in your calendar.";
            }
            return getPlainTextResponse(speechText);
        }

        private String convertDateTime(DateTime dateTime) {
            ZonedDateTime zonedDateTime = ZonedDateTime.parse(dateTime.toStringRfc3339(), DateTimeFormatter.ISO_ZONED_DATE_TIME);
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("EEEE, d MMMM yyyy 'at' hh:mma");
            return formatter.format(zonedDateTime);
        }


}
