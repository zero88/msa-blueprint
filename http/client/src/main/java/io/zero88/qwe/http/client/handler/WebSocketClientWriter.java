package io.zero88.qwe.http.client.handler;

import io.vertx.core.Future;
import io.vertx.core.http.WebSocket;
import io.vertx.core.json.JsonObject;
import io.zero88.qwe.event.EBContract;
import io.zero88.qwe.event.EventListener;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public final class WebSocketClientWriter implements EventListener {

    private final WebSocket webSocket;

    @EBContract(action = "SEND")
    public Future<Boolean> send(JsonObject data) {
        return webSocket.writeTextMessage(data.encode())
                        .flatMap(r -> Future.succeededFuture(true))
                        .otherwise(err -> false);
    }

}
