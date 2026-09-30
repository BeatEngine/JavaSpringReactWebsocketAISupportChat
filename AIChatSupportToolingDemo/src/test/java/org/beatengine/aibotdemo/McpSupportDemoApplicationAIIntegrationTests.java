package org.beatengine.aibotdemo;

import org.beatengine.aibotdemo.aiintegration.AIChatClientService;
import org.beatengine.aibotdemo.aiintegration.tools.ProductRecord;
import org.beatengine.aibotdemo.aiintegration.tools.ProductTools;
import org.beatengine.aibotdemo.entity.Product;
import org.beatengine.aibotdemo.sharedservices.ChatBotService;
import org.beatengine.aibotdemo.sharedservices.ProductService;
import org.beatengine.aibotdemo.websocket.JSONMessage;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.springframework.ai.util.JacksonUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.util.Assert;
import tools.jackson.databind.ObjectMapper;

import java.util.List;

@SpringBootTest
class McpSupportDemoApplicationAIIntegrationTests {

    //todo In a real project this test would use a test database also for the chatBotMemory when not in-memory memory

    @Autowired
    AIChatClientService chatClient;

    @Autowired
    ProductTools productTools;

    @Autowired
    ProductService productService;

    @Autowired
    ChatBotService chatBotService;

    @Test
    @Order(1)
    void TestChatBotMemoryFormat()
    {
        final String chatSession = "tm-328hfh9823jnjwd98jjd892";

        final List<JSONMessage> messagesBefore = chatBotService.queryChatbotMemoryMessages(chatSession);
        Assert.isTrue(messagesBefore.isEmpty(), "There are already messages in this chat! Fix test!");
        // Used incomingChatbotMessage bco. the chat prefix in queryChatbotMemoryMessages!
        final String userInquiry = "Hello! Are you available?";
        chatBotService.incomingChatbotMessage(userInquiry, chatSession);
        final List<JSONMessage> jsonMessages = chatBotService.queryChatbotMemoryMessages(chatSession);

        Assert.isTrue(/* For the bots optional 'hello' Message + 2 messages */ jsonMessages.size() == 2,
                "Message size out of scope 2 size: " + jsonMessages.size());

        // The last 2 messages should be inquiry and response (mind bots optional 'hello' Message)
        final JSONMessage previous = jsonMessages.get(jsonMessages.size()-2);
        final JSONMessage last = jsonMessages.getLast();


        Assert.isTrue(JSONMessage.AUTHOR_USER.equals(previous.author), "Expected user author is different! '"
                + JSONMessage.AUTHOR_USER + "' != '" + previous.author + "'");
        Assert.isTrue(JSONMessage.AUTHOR_ASSISTANT.equals(last.author), "Expected system author is different! '"
                + JSONMessage.AUTHOR_ASSISTANT + "' != '" + last.author + "'");

        Assert.isTrue(userInquiry.equals(previous.data), "Expected user inquiry in data '"
                + userInquiry + "' != '" + previous.data + "'");
        Assert.isTrue(!String.valueOf(last.data).isBlank(), "Expected assist response is not blank");

    }

    @Test
    @Order(2)
    void TestJSONMappingProductRecord()
    {
        ObjectMapper mapper = JacksonUtils.getDefaultJsonMapper();
        String json = """
        {
          "id": "123",
          "name": "Coffee",
          "description": "A nice coffee",
          "basePrice": 4.99
        }
        """;

        ProductRecord record = mapper.readValue(json, ProductRecord.class);

        Assert.isTrue(record.getId().equals(123L), "ID not serialized !");
        Assert.isTrue(record.getName().equals("Coffee"), "name not serialized !");
        Assert.isTrue(record.getDescription().equals("A nice coffee"), "description not serialized !");
        Assert.isTrue(record.getBasePrice().equals(4.99f), "basePrice not serialized !");
    }

    @Test
    @Order(3)
    void TestOllamaToolCallQuery()
    {
        List<ProductRecord> light = productTools.getAllProductsByNameOrDescriptionContains("Battery", "Battery");
        Assert.isTrue(!light.isEmpty(), "ProductTools::getAllByNameContains empty result.");

        String s = chatClient.promptToOllama("Search for products with name like Battery.",
                "chat23462347642792932758720384828723");
        System.getLogger(this.getClass().getSimpleName()).log(System.Logger.Level.INFO,"Bot: " + s);
        Assert.isTrue(!s.isBlank(), "ChatClient reply is blank.");
        Assert.isTrue(s.contains(light.getFirst().getId().toString()), "Prompt result doesn't match with manual query.");
        // Was completed successfully at my test ollama and alpine ollama qwen2.5:1.5b
    }

    @Test
    @Order(4)
    void TestOllamaToolCallCreateProduct()
    {
        long countBefore = productService.count();

        String s = chatClient.promptToOllama("Insert a new product with name Foxhead, description 'A foxhead mask' for 9.99.",
                "chat23462347642792932758720384828723");
        System.getLogger(this.getClass().getSimpleName()).log(System.Logger.Level.INFO,"Bot: " + s);

        long countAfter = productService.count();
        Assert.isTrue(countAfter > countBefore, "No product was inserted (equal count): " + s);
        // Was completed successfully at my test ollama qwen2.5:1.5b
        Assert.isTrue(!s.isBlank(), "ChatClient reply is blank.");

    }

    @Test
    @Order(5)
    void TestOllamaToolCallRemoveProductById()
    {
        long countBefore = productService.count();
        List<ProductRecord> light = productTools.getAllProductsByNameOrDescriptionContains("Light", "Light");
        long id = light.getFirst().getId();
        System.getLogger(this.getClass().getSimpleName()).log(System.Logger.Level.INFO,
                "Removing " + id + " (" + light.getFirst().getName() + ")");

        String s = chatClient.promptToOllama("Remove the product with id " + id + ".",
                "chat23462347642792932758720384828723");
        System.getLogger(this.getClass().getSimpleName()).log(System.Logger.Level.INFO,"Bot: " + s);

        try {
            Product p = productService.getById(id);
            Assert.isTrue(p == null, "Product was not deleted: " + p.getName());
        }
        catch (Product.ProductNotFoundException notFoundException)
        {
            System.getLogger(this.getClass().getSimpleName()).log(System.Logger.Level.INFO,"Deleted successfully: " + notFoundException.getMessage());
        }

        // Was completed successfully at my test ollama qwen2.5:1.5b
        Assert.isTrue(!s.isBlank(), "ChatClient reply is blank.");

    }

    @Test
    @Order(6)
    void TestOllamaToolCallRemoveProductByName()
    {
        final String name = "Infotainment";
        long id = productService.getAllByNameContains(name).getFirst().getId();
        String s = chatClient.promptToOllama("Remove the product with name "+name+".",
                "chat23462347642792932758720384828723");
        System.getLogger(this.getClass().getSimpleName()).log(System.Logger.Level.INFO,"Bot: " + s);

        try {
            Product p = productService.getById(id);
            Assert.isTrue(p == null, "Product was not deleted: " + p.getName());
        }
        catch (Product.ProductNotFoundException notFoundException)
        {
            System.getLogger(this.getClass().getSimpleName()).log(System.Logger.Level.INFO,"Deleted successfully: " + notFoundException.getMessage());
        }

        // Was completed successfully at my test ollama qwen2.5:1.5b
        Assert.isTrue(!s.isBlank(), "ChatClient reply is blank.");

    }

}
