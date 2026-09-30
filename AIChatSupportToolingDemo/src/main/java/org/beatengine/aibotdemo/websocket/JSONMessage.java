package org.beatengine.aibotdemo.websocket;

import java.util.Objects;

public class JSONMessage {
    public static final String STATE_UPDATE = "update";
    public static final String STATE_DATA = "data";
    //todo STATE_USERMAPPING = "usermapping"; //This would be for mapping the user to websocket session on site load.
    public static final String AUTHOR_USER = "user";
    public static final String AUTHOR_ASSISTANT = "assistant";
    // todo the (especial history) messages needs a created public LocalDateTime created;

    public String api_path;
    public String state;
    public String author = null;
    public Object data = null;

    public JSONMessage()
    {

    }

    public JSONMessage(final String api_path, final String state, final Object data, final String author) {
        this.api_path = api_path;
        this.state = state;
        this.author = author;
        this.data = data;
    }

    public JSONMessage(final String api_path, final String state, final Object data) {
        this.api_path = api_path;
        this.state = state;
        this.data = data;
    }

    public JSONMessage(final String api_path, final String state) {
        this.api_path = api_path;
        this.state = state;
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof JSONMessage that)) return false;
        return Objects.equals(api_path, that.api_path) && Objects.equals(state, that.state)
                && Objects.equals(author, that.author) && Objects.equals(data, that.data);
    }

    @Override
    public int hashCode() {
        return Objects.hash(api_path, state, author, data);
    }
}
