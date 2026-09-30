import './ChatBotProducts.css'
import {useState, useEffect, useImperativeHandle, type RefObject} from "react";
import type {OnAppInitFinishedRef} from "../types/References.tsx";
import {type JSONMessage, JSONMessageSTATE_UPDATE} from "../types/JSONMessage.tsx";
import {JSONMessageSTATE_DATA, JSONMessageAUTHOR_USER, JSONMessageAUTHOR_ASSISTANT} from "../types/JSONMessage.tsx";
import ChatBotProductsMessage from './ChatBotProducts/ChatBotProductsMessage.tsx'



function ChatBotProducts(wrapper:{socket:WebSocket, OnAppInitFinished:RefObject<OnAppInitFinishedRef | null>}) {
    const endpointChatProducts = '/api/chatbot/products';
    const endpointChatProductMessages:string = endpointChatProducts + '/message';

    type ChatMessage = {
        text:string,
        author:string,
    };

    const [chatMessages, setMessages] = useState([
        {
            text:'Hi, I\'m your assistant! How can I support?',
            author: JSONMessageAUTHOR_ASSISTANT
        }]
    );
    const chatId: string = "chat-input";

    const [chatModelName, setModelName] = useState('llm');




    const sendMesageAndFetchChatBotReply = async (/* OLD-CODE REST-API  API_URL: RequestInfo | URL,*/ message: any) => {
        /* OLD-CODE REST-API
        const request: RequestInit = {
            method: "POST",
            body: message
        }*/

        const userMsg:ChatMessage = {
            text: message,
            author: JSONMessageAUTHOR_USER
        }

        setMessages(chatMessages => [...chatMessages, (userMsg)]);
        const inp: null|HTMLElement = document.getElementById(chatId);
        // send to mcp api and show result in chat
        if(inp) {
            (inp as HTMLInputElement).value = "";
        }

        const wsMsg:JSONMessage = {
            api_path: endpointChatProductMessages,
            state: JSONMessageSTATE_DATA,
            author: JSONMessageAUTHOR_USER,
            data: message
        }

        /* OLD CODE REST-API
        const response = await fetch(API_URL, request);
        const fetchedMessage = await response.text();*/

        // Send the chatbot request over WebSocket
        wrapper.socket.send(JSON.stringify(wsMsg));

        /* OLD CODE REST-API
        const msg:ChatMessage = {
            text: fetchedMessage,
            author: 'system'
        }

        // Push fetchedMessage
        setMessages(chatMessages => [...chatMessages, msg]);*/

    }

    const fetchModelName = async (API_URL: RequestInfo | URL) =>
    {
        const response = await fetch(API_URL);
        return await response.json();
    }


    const loadChatHistory = async () => {


        console.log('Trigger WS: Load chat history');
        const wsMsg:JSONMessage = {
            api_path: endpointChatProductMessages,
            state: JSONMessageSTATE_UPDATE, /* Send an update request for all messages (history-messages) */
            author: JSONMessageAUTHOR_USER,
            data: null
        }
        //Send the chatbot request over WebSocket
        wrapper.socket.send(JSON.stringify(wsMsg));

    }
    //initial Chat load history, after App initialization has finished.
    useImperativeHandle(wrapper.OnAppInitFinished, () => ({
        afterAppInit() {
            loadChatHistory().then(()=>{});
        }
    }));


    useEffect(()=>{
        fetchModelName(endpointChatProducts + "/model").then((result:any)=>{
            setModelName(result.model_name);
        })

        // This needs to run in useEffect otherwise the event listener will be added 2 or 6 times.
        wrapper.socket.addEventListener("message", function(event) {
            const message:JSONMessage = JSON.parse(event.data);
            /*console.log(message);*/
            if(message)
            {
                /* Handle the messages for the chatbot */
                if(message.api_path == endpointChatProductMessages) {
                    /* The server sent a response for the inquiry */
                    if (message.state == JSONMessageSTATE_DATA) {
                        console.log("Parsed WebSocket chatbot message response.");
                        const msg: ChatMessage = {
                            text: message.data,
                            author: JSONMessageAUTHOR_ASSISTANT /* The author is always the assistant bco. reply to inquiry*/
                        }

                        // Push fetchedMessage
                        setMessages(chatMessages => [...chatMessages, msg]);
                    }
                    // todo the (especial history) messages needs a created and need to be ordered by this created
                    /* The server sent a response for the on load requested history messages */
                    if (message.state == JSONMessageSTATE_UPDATE) {
                        console.log("Parsed WebSocket chatbot message history.");
                        const msg: ChatMessage = {
                            text: message.data,
                            author: message.author /* The author depends on the incoming message */
                        }

                        // Push fetchedMessage
                        setMessages(chatMessages => [...chatMessages, msg]);
                    }
                }
            }
            else
            {
                console.error("Received WebSocket message has wrong format! Expected: JSONMessage.");
            }
        });


    },[])


    return (
        <>
            <h3>Use the following {chatModelName} bot to add or remove products (using mcp)!</h3>
            <div id="chat-response-window" className={"chat-window"}>
                {chatMessages.map((message: ChatMessage) => (
                    <ChatBotProductsMessage text={message.text} author={message.author}></ChatBotProductsMessage>
                ))}
            </div>
            <div className={"chat-input-container"}>
            <input onKeyDown={(event) => {
                if (event.key === "Enter") {
                    // send to mcp api and show result in chat
                    sendMesageAndFetchChatBotReply(/*endpointChatProductMessages,*/ event.currentTarget.value);
                }
            }

            }
            id={chatId} className={"chat-input"}/>
            <button onClick={
                () => {
                    const inp: null|HTMLElement = document.getElementById(chatId);
                    // send to mcp api and show result in chat
                    if(inp) {
                        sendMesageAndFetchChatBotReply(/*endpointChatProductMessages,*/ (inp as HTMLInputElement).value);
                    }
                    else
                    {
                        console.error("Element " + chatId + " not found.");
                    }
                }

            }

            >SND</button>
            </div>
        </>
    );
}

export default ChatBotProducts;