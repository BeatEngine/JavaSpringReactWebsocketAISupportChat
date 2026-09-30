package org.beatengine.aibotdemo.websocket;

import org.beatengine.aibotdemo.restcontroller.ChatBotController;
import org.beatengine.aibotdemo.sharedservices.ChatBotService;
import org.springframework.ai.util.JacksonUtils;
import org.springframework.web.socket.*;
import org.springframework.web.socket.handler.AbstractWebSocketHandler;
import tools.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class RestWebsocketHandler extends AbstractWebSocketHandler {

    private ChatBotService chatBotService;

    final private ExecutorService executorServiceChat = Executors.newFixedThreadPool(8);

    //private final Set<WebSocketSession> sessions = new CopyOnWriteArraySet<>();

    private final Map<WebSocketSession, String> sessionsWithUserSessions = new ConcurrentHashMap<>();

    private final Map<String, Set<String>> websocketSessionsByUserId = new ConcurrentHashMap<>();

    final String USER_DEMO_SESSIONID = "user1"; //todo implement user-mapping

    private static RestWebsocketHandler SINGLETON;
    public RestWebsocketHandler()
    {
        SINGLETON = this;
    }

    public static RestWebsocketHandler getInstance()
    {
        return SINGLETON;
    }


    @Override
    public void afterConnectionEstablished(final WebSocketSession session) throws Exception {
        sessionsWithUserSessions.put(session, USER_DEMO_SESSIONID); //todo implement user mapping

        if(chatBotService == null) {
            // This is my current solution for cycle dependency problems with too many Spring objects
            chatBotService = ChatBotService.getInstance();
        }
        System.getLogger(this.getClass().getSimpleName()).log(System.Logger.Level.INFO, "WS connection opened: " + session.getId());

    }

    /**
     * <h6>ENTRY POINT for all textbased websocket messages.</h6>
     * @param session session
     * @param message will be deserialized into JSONMessage type
     * @throws Exception Deserialization exception
     */
    protected void handleTextMessage(final WebSocketSession session, final TextMessage message) throws Exception {
        System.getLogger(this.getClass().getSimpleName()).log(System.Logger.Level.INFO, "WS message text received: " + session.getId());
        final String payload = message.getPayload();

        try {
            handleJSONMessage(session, deserializeJSON(payload));
        }
        catch (Exception e)
        {
            System.getLogger(this.getClass().getSimpleName()).log(System.Logger.Level.ERROR, "WS handleJSONMessage failed " + session.getId() + " : " + e.getMessage());
        }

    }

    /**
     * <h6>Routing the users chat inquiry to the LLM LIKE A CONTROLLER</h6>
     * @param session session //todo In a real project here would be the userId not the websocketId to send the chatbot response to all user Instances
     * @param message JSONMessage The message containing the user chat inquiry.
     */
    private void handleJSONMessage(final WebSocketSession session, final JSONMessage message)
    {
        //Handle all the controller like types of requests

        //User chat inquiry
        handleJSONMessageChatInquiry(session, message);
        //Fetch chat history on load
        handleJSONMessageFetchChatHistory(session, message);

    }

    /**
     * Handles the message in the case ChatBotController.API_MESSAGE and JSONMessage.STATE_DATA
     * @param session ses
     * @param message msg
     */
    private void handleJSONMessageChatInquiry(final WebSocketSession session, final JSONMessage message)
    {
        if(ChatBotController.API_MESSAGE.equals(message.api_path) && JSONMessage.STATE_DATA.equals(message.state))
        {
            // For the case of incoming data from chatBot UI


            executorServiceChat.submit(new Runnable() {
                // Reply with a websocket message chatbot response in a new thread.
                @Override
                public void run() {
                    final String sessionId = session.getId();
                    final String userSessionId = sessionsWithUserSessions.get(session);

                    /* Passing the inquiry from the message to the LLM over ChatBotService  */
                    final String promptResponse = chatBotService.incomingChatbotMessage(String.valueOf(message.data), userSessionId);
                    try {
                        final String payload = serializeObject(new JSONMessage(ChatBotController.API_MESSAGE, JSONMessage.STATE_DATA, promptResponse, JSONMessage.AUTHOR_ASSISTANT));
                        System.getLogger(this.getClass().getSimpleName()).log(System.Logger.Level.INFO, "handleJSONMessage sendTextMessage: " + ChatBotController.API_MESSAGE);
                        //Send to all related userSessionIds not only to session!
                        sendTextMessage(payload, session);
                    } catch (Exception e) {
                        System.getLogger(this.getClass().getSimpleName()).log(System.Logger.Level.ERROR, "WS chatbot response failed " + sessionId + " : " + e.getMessage());
                    }
                }
            });
            System.getLogger(this.getClass().getSimpleName()).log(System.Logger.Level.INFO, "WS message chatbot received: " + session.getId());
        }
    }

    /**
     * Handles the message in the case ChatBotController.API_MESSAGE and JSONMessage.STATE_UPDATE
     * @param session ses IMPORTANT FOR this we only respond to the current chat not all user chats.
     * @param message msg
     */
    private void handleJSONMessageFetchChatHistory(final WebSocketSession session, final JSONMessage message)
    {
        if(ChatBotController.API_MESSAGE.equals(message.api_path) && JSONMessage.STATE_UPDATE.equals(message.state))
        {
            // For the case of incoming data from chatBot UI


            executorServiceChat.submit(new Runnable() {
                // Reply with a websocket message chatbot response in a new thread.
                @Override
                public void run() {
                    final String sessionId = session.getId();
                    final String userSessionId = sessionsWithUserSessions.get(session);

                    /* Fetching the chatbots memory aka. chat history messages for the first site load  */
                    final List<JSONMessage> historyMessages = chatBotService.queryChatbotMemoryMessages(userSessionId);
                    System.getLogger(this.getClass().getSimpleName()).log(System.Logger.Level.INFO, "handleJSONMessageFetchChatHistory sendTextMessages: " + historyMessages.size());
                    for(final JSONMessage jsonMessage : historyMessages) {
                        try {
                            final String payload = serializeObject(jsonMessage);
                            //ONLY SEND to current chat not every user chat
                            sendTextMessage(payload, session, false);
                        } catch (Exception e) {
                            System.getLogger(this.getClass().getSimpleName()).log(System.Logger.Level.ERROR, "WS chatbot response failed " + sessionId + " : " + e.getMessage());
                        }
                    }
                }
            });
            System.getLogger(this.getClass().getSimpleName()).log(System.Logger.Level.INFO, "WS message chatbot received: " + session.getId());
        }
    }

    /**
     * @param obj JSONMessage object
     * @return json String
     */
    private String serializeObject(final JSONMessage obj)
    {
        final ObjectMapper mapper = JacksonUtils.getDefaultJsonMapper();
        return mapper.writeValueAsString(obj);
    }

    /**
     * @param json json String
     * @return JSONMessage object
     */
    private JSONMessage deserializeJSON(final String json)
    {
        final ObjectMapper mapper = JacksonUtils.getDefaultJsonMapper();
        return mapper.readValue(json, JSONMessage.class);
    }

    /**
     * Broadcast a websocket message to all connections, that the serverside data has changed (so the frontend can refetch)
     * @param restApiPath
     */
    public void triggerRestUpdateEvent(final String restApiPath)
    {
        try {
            final String payload = serializeObject(new JSONMessage(restApiPath, JSONMessage.STATE_UPDATE));
            broadcastTextMessage(payload);
            System.getLogger(this.getClass().getSimpleName()).log(System.Logger.Level.INFO, "triggerRestUpdateEvent: " + restApiPath);
        }
        catch (Exception e)
        {
            System.getLogger(this.getClass().getSimpleName()).log(System.Logger.Level.ERROR, "triggerRestUpdateEvent failed: " + e.getMessage());
        }
    }

    /**
     * Sent to all open Websocket connections
     * @param payload
     * @throws Exception
     */
    public void broadcastTextMessage(final String payload) throws Exception
    {
        System.getLogger(this.getClass().getSimpleName()).log(System.Logger.Level.INFO, "broadcastTextMessage sessions: " + sessionsWithUserSessions.size());
        for (final Map.Entry<WebSocketSession, String> webSocketSessionWithUser : sessionsWithUserSessions.entrySet()) {
            executorServiceChat.submit(new Runnable() {
                // Reply with a websocket message chatbot response in a new thread.
                @Override
                public void run() {
                    final WebSocketSession session = webSocketSessionWithUser.getKey();
                    try{
                        System.getLogger(this.getClass().getSimpleName()).log(System.Logger.Level.INFO, "broadcastTextMessage to " + session.getId());
                        sendTextMessage(payload, session);
                    } catch (Exception e) {
                        System.getLogger(this.getClass().getSimpleName()).log(System.Logger.Level.ERROR, "WS broadcastTextMessage failed " + session.getId() + " : " + e.getMessage());
                    }
                }

            });
        }
    }

    /**
     * Send the websocket payload to all user-chats with sessionId (for example this should be changed to userId)
     * @param payload The websocket json-object to send. E.G. JSONMessage
     * @param session UserId has a mapping to session, so multiple tabs would receive the responses
     * @throws IOException Sending over websocket failed
     */
    public synchronized void sendTextMessage(final String payload, final WebSocketSession session) throws IOException
    {
        sendTextMessage(payload, session, true);
    }

    /**
     *
     * Send the websocket payload to all sessions with sessionId (for example this should be changed to userId)
     * @param payload The websocket json-object to send. E.G. JSONMessage
     * @param session session
     * @param broadcastToAllCorrespondingUserSessions IF false --> only send to current chat not all related user chats
     * @throws IOException Sending over websocket failed
     */
    public synchronized void sendTextMessage(final String payload, final WebSocketSession session,
                                             final boolean broadcastToAllCorrespondingUserSessions /* //todo implement in real project*/) throws IOException
    {

        if(broadcastToAllCorrespondingUserSessions)
        {
            final String userSessionId = sessionsWithUserSessions.get(session);
            for (final Map.Entry<WebSocketSession, String> webSocketSessionWithUser: sessionsWithUserSessions.entrySet()) {
                // If any websocketSessionId is related to the userSessionId related to the current websocketSessionId
                if(userSessionId.equals(webSocketSessionWithUser.getValue())) {
                    final WebSocketSession webSocketSession = webSocketSessionWithUser.getKey();
                    if (webSocketSession.isOpen()) {
                        webSocketSession.sendMessage(new TextMessage(payload));
                    }
                }
            }
        }
        else
        {
            if(session.isOpen())
            {
                session.sendMessage(new TextMessage(payload));
            }
        }

    }

    protected void handleBinaryMessage(final WebSocketSession session, final BinaryMessage message) throws Exception {
        System.getLogger(this.getClass().getSimpleName()).log(System.Logger.Level.INFO, "WS message binary received: " + session.getId());

    }

    protected void handlePongMessage(final WebSocketSession session, final PongMessage message) throws Exception {
        System.getLogger(this.getClass().getSimpleName()).log(System.Logger.Level.INFO, "WS pong received: " + session.getId());

    }

    @Override
    public void handleTransportError(final WebSocketSession session, final Throwable exception) throws Exception {
        System.getLogger(this.getClass().getSimpleName()).log(System.Logger.Level.WARNING, "WS transport failed: " + session.getId());
    }

    @Override
    public void afterConnectionClosed(final WebSocketSession session, final CloseStatus closeStatus) throws Exception {
        sessionsWithUserSessions.remove(session);
        System.getLogger(this.getClass().getSimpleName()).log(System.Logger.Level.INFO, "WS connection closed: " + session.getId());
        //System.getLogger(this.getClass().getSimpleName()).log(System.Logger.Level.INFO, "WS connection closed state=" + closeStatus.getCode() +" : " + session.getId());
    }

    @Override
    public boolean supportsPartialMessages() {
        return false;
    }
}
