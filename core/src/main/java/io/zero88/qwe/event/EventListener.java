package io.zero88.qwe.event;

import io.vertx.core.eventbus.Message;
import io.zero88.qwe.HasLogger;
import io.zero88.qwe.SharedDataLocalProxy;
import io.zero88.qwe.dto.JsonData;
import io.zero88.qwe.event.EventLogSystem.EventListenerLogSystem;

import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.NonNull;

/**
 * Represents for a listener that receives and processes an {@code EventBus} message then response back to a caller
 *
 * @see EBContract
 * @see EventMessage
 * @see EventAction
 * @see EventPattern#REQUEST_RESPONSE
 */
public interface EventListener extends HasLogger, EventListenerLogSystem {

    /**
     * Jackson Object mapper for serialize/deserialize data
     *
     * @return Object mapper. Default: {@link JsonData#MAPPER}
     */
    default ObjectMapper mapper() { return JsonData.MAPPER; }

    /**
     * Fallback json key if output is {@code collection/primitive} value
     *
     * @return fallback json key. Default: {@code data}
     */
    default String fallback() { return JsonData.SUCCESS_KEY; }

    default @NonNull void handle(SharedDataLocalProxy sharedData, Message<Object> msg) {
        new EventListenerExecutorImpl(this, sharedData).execute(msg).onComplete(ar -> {
            if (ar.succeeded()) {
                msg.reply(ar.result().toJson());
            } else {
                msg.reply(EventMessage.replyError(EventAction.UNKNOWN, ar.cause()).toJson());
            }
        });
    }

}
