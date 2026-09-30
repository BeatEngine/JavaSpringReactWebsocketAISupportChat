export type JSONMessage = {
    api_path:string, // todo the (especial history) messages needs a created and need to be ordered by this created
    state:string,
    author:string,
    data:any
};

export const JSONMessageSTATE_UPDATE:string = "update";
export const JSONMessageSTATE_DATA:string = "data";
export const JSONMessageAUTHOR_USER:string = "user";
export const JSONMessageAUTHOR_ASSISTANT:string = "assistant";
