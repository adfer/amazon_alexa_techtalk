package com.epam;

import com.amazon.speech.speechlet.lambda.SpeechletRequestStreamHandler;

import java.util.HashSet;
import java.util.Set;

/**
 * Created by Adrian Ferenc on 22.07.2017.
 */
public class SimpleSpeechletRequestStreamHandler extends SpeechletRequestStreamHandler {

    private static final Set<String> supportedApplicationIds = new HashSet<String>();

    static {
        supportedApplicationIds.add("amzn1.ask.skill.10c8f012-5755-4e2a-989e-1ad9e9300bcb");
    }

    public SimpleSpeechletRequestStreamHandler() {
        super(new MyCustomSpeechlet(), supportedApplicationIds);
    }
}
