package org.beatengine.aibotdemo.restcontroller;

import org.beatengine.aibotdemo.aiintegration.AIChatClientService;
import org.beatengine.aibotdemo.sharedservices.ChatBotService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/** REST: <code> /api/chatbot/products <code/>
 *
 */
@RestController
public class ChatBotController {

    public final static String API_ROOT = "/api/chatbot/products";
    public final static String API_MESSAGE = "/api/chatbot/products/message";

    @Autowired
    private ChatBotService chatBotService;

    @Deprecated(forRemoval = true) //Messages coming over websocket RestWebsocketHandler
    @PostMapping (API_MESSAGE)
    public String incomingChatbotMessage(@RequestBody final String message)
    {
        return chatBotService.incomingChatbotMessage(message, ""/*//todo set session */);
    }

    @GetMapping (API_ROOT + "/testapi")
    public String incomingChatbotMessage()
    {
        return "/api/chatbot/products runs!";
    }


    @GetMapping (API_ROOT + "/model")
    public Object modelName()
    {
        return new Object(){ // Returns as deserialized JSON
            final public String model_name = System.getProperty("OLLAMA_MODEL");
        };
    }

}
