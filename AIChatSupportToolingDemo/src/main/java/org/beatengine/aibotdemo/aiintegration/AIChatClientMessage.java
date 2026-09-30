package org.beatengine.aibotdemo.aiintegration;

import org.jspecify.annotations.Nullable;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.MessageType;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.util.Map;

public class AIChatClientMessage implements Message {

    private LocalDateTime created = LocalDateTime.now();
    private String text = "";
    private MessageType type;

    public AIChatClientMessage(final String text, final MessageType type)
    {
        this.text = text;
        this.type = type;
    }

    @Override
    public MessageType getMessageType() {
        return type;
    }

    @Override
    public @Nullable String getText() {
        return text;
    }

    @Override
    public Map<String, Object> getMetadata() {
        return Map.of("created", DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm:ss").format(created));
    }
}
