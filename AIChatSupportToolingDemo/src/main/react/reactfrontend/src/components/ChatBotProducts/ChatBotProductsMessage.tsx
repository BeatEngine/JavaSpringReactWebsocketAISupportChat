import './ChatBotProductsMessage.css'
import {JSONMessageAUTHOR_ASSISTANT} from "../../types/JSONMessage.tsx";

function ChatBotProductsMessage(wrapper:{text:string, author:string}) {

    let classesMessage:string = "chat-bot-products-message"

    if(wrapper.author == JSONMessageAUTHOR_ASSISTANT)
    {
        classesMessage +=" chat-bot-products-message-assistant";
    }
    else
    {
        classesMessage += " chat-bot-products-message-user";
    }

    return (
        <>
            <div className={classesMessage}>{ wrapper.text }</div>
        </>
    )
}

export default ChatBotProductsMessage;