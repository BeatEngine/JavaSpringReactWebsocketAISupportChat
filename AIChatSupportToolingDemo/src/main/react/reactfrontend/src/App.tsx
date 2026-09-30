import {useState, useEffect, useRef, type RefObject} from 'react'
import type {OnAppInitFinishedRef} from "./types/References.tsx";
import type {JSONMessage} from "./types/JSONMessage.tsx";
import {JSONMessageSTATE_UPDATE} from "./types/JSONMessage.tsx";
import ChatBotProducts from './components/ChatBotProducts.tsx';
import './App.css'

const endpointWebsocket = 'ws://localhost:8080/api';
const socket = new WebSocket(endpointWebsocket);



function App() {
  const endpointProducts = '/api/products';

    const OnAppInitFinished:RefObject<OnAppInitFinishedRef | null> = useRef<OnAppInitFinishedRef|null>(null);

    const [products, setProducts] = useState([{}]);

    const fetchProducts = async (API_URL: RequestInfo | URL) => {
      const response = await fetch(API_URL);
      return await response.json();
    }

    const updateProducts = () => {
        console.log("Called updateProducts()");
        fetchProducts(endpointProducts).then((result: any) => {
            setProducts(result);
        });
    }

    socket.onopen = function() {
        console.log("Connected to WebSocket-Server.");

        //Run the method of child component after finish
        OnAppInitFinished.current?.afterAppInit();


    };

    socket.addEventListener("message", function(event) {
        const message:JSONMessage = JSON.parse(event.data);
        if(message)
        {
            /* The server tells to refetch products */
            if(message.api_path == endpointProducts && message.state == JSONMessageSTATE_UPDATE)
            {
                updateProducts();
            }
        }
        else
        {
            console.error("Received WebSocket message has wrong format! Expected: JSONMessage.");
        }
    });

    socket.onclose = function() {
        console.log("Connected from WebSocket-Server.");

    };
    socket.onerror = function(error) {
        console.error("WebSocket Error: ", error);
    };

    useEffect(() => {
        console.log('App init!');


        /*function sendMessage(message:string) {
            if (message && socket.readyState === WebSocket.OPEN) {
                socket.send(message); //Send text message
                messageInput.value = "";
            }
        }*/

        //Initial fetch products, after that further updates triggered by websocket
        updateProducts();
        //const interval = setInterval(updateproducts, 4000);

        return () => {};
    },[])

  // @ts-ignore
  if (!products || products.length === 0) {
    return <p>No products available :( </p>;
  }


  return (
    <>
      <h2>Welcome to the React Java Spring Boot websocket AI-Tool-Chat helper demo!</h2>
      <h3>View Products</h3>
      <table>
        <thead>
        <tr>
          {Object.keys(products[0]).map((key) => (
              <th key={key}>{key}</th>
          ))}
        </tr>
        </thead>
        <tbody>
        {products.map((product, index) => (
            <tr key={index}>
              {Object.keys(product).map((key) => (
                  // @ts-ignore
                  <td key={key}>{product[key]}</td>
              ))}
            </tr>
        ))}
        </tbody>
      </table>
      <ChatBotProducts socket={socket} OnAppInitFinished={OnAppInitFinished}></ChatBotProducts>
    </>
  )
}

export default App
