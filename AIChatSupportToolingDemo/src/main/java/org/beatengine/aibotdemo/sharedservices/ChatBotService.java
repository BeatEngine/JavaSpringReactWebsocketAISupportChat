package org.beatengine.aibotdemo.sharedservices;

import org.beatengine.aibotdemo.aiintegration.AIChatClientMessage;
import org.beatengine.aibotdemo.aiintegration.AIChatClientService;
import org.beatengine.aibotdemo.restcontroller.ChatBotController;
import org.beatengine.aibotdemo.restcontroller.ProductsController;
import org.beatengine.aibotdemo.websocket.JSONMessage;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.SystemMessage;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
public class ChatBotService {
    private static ChatBotService SINGLETON;
    public ChatBotService()
    {
        SINGLETON = this;
    }

    @Autowired
    private AIChatClientService chatClientService;

    private static String ChatConversationPrefix = "ProdChat";

    public static ChatBotService getInstance() {
        return SINGLETON;
    }

    /**
     * <h6>Retrieve LLM response for the inquiry.</h6>
     * @param message inquiry for the LLM.
     * @param chatSession The chatSession of the Chat. //todo in a real project this is the userSessionId
     * @return The LLM reply.
     */
    public synchronized String incomingChatbotMessage(final String message, final String chatSession)
    {
        //System.getLogger(this.getClass().getSimpleName()).log(System.Logger.Level.INFO, "Cookie: " + chatSession);
        final String s = chatClientService.promptToOllama(message, ChatConversationPrefix+chatSession);
        System.getLogger(this.getClass().getSimpleName()).log(System.Logger.Level.INFO,
                s.substring(0, Math.min(50, s.length())));
        return s;
    }

    /**
     * Query all history messages form the chatbots 'Memory'
     * @param chatSession the chatSession  //todo in a real project this is the userSessionId
     * @return the List of JSONMessages from history (memory).
     */
    public synchronized List<JSONMessage> queryChatbotMemoryMessages(final String chatSession)
    {
        final List<JSONMessage> transportMessages = new ArrayList<>();
        final List<Message> messages = chatClientService.queryChatbotMemoryMessages(ChatConversationPrefix+chatSession);
        for(final Message m : messages)
        {
            if(m instanceof AIChatClientMessage /* Only our type of Message no tool calls etc. duplicated messages */ ) {
                final AIChatClientMessage message = (AIChatClientMessage) m;
                final String author = message.getMessageType().getValue();
                final String data = message.getText();
                //todo final LocalDateTime created = (LocalDateTime) message.getMetadata().get("created"); //implement date for ordering
                transportMessages.add(new JSONMessage(ChatBotController.API_MESSAGE, JSONMessage.STATE_UPDATE, data, author));
            }
        }
        return transportMessages;
    }

}
