# JavaSpringReactWebsocketAISupportChat
A demo project showing AI-Toolcalling using Java Spring React and Websocket.

## Scope and goals of this demo project
- Everything shall be delivered as a docker compose container
- This demo shall show how to integrate a Ollama-AI-Model into a demo SP-App etc. with MCP Tooling
- For example the user should ask the chat AI which options are there to help
- The user shall be able to commission the chat AI to execute a demo business case.

## Configuration / How to use
### environmentconfigurations.env 
- Contains all notes and parameters
- Is preconfigured for usage with docker compose (local build is commented out)

<table>
<thead><tr>
<td>Value</td>
<td>Example</td>
<td>Description</td>
</tr></thead>
<tbody>
<tr>
<td>APPLICATION_DB</td>
<td>demo_app_mcp</td>
<td>The database name</td>
</tr>
<tr>
<td>APP_POSTGRES_HOST</td>
<td>db</td>
<td>The database host (e.g. docker compose name or localhost for dev)</td>
</tr>
<tr>
<td>POSTGRES_USER</td>
<td>postgres</td>
<td>The database user</td>
</tr>
<tr>
<td>POSTGRES_PASSWORD</td>
<td>yourSafe1!Pass</td>
<td>Change the password!</td>
</tr>
<tr>
<td>OLLAMA_URL</td>
<td>http://aimodell:11434</td>
<td>The ollama URL (host is docker compose name or localhost for dev)</td>
</tr>
<tr>
<td>OLLAMA_MODEL</td>
<td>qwen2.5:1.5b</td>
<td><b>Also set in compose.yaml --> - command: ["ollama", "pull" ,"qwen2.5:1.5b" ...</b> <div>Use small LLMs for local runs so response only takes < 20 seconds.</div></td>
</tr>
</tbody>
</table>