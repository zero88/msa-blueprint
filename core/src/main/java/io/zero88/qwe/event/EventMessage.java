package io.zero88.qwe.event;

import java.io.Serializable;
import java.util.Map;
import java.util.Objects;

import io.github.zero88.exceptions.ErrorCode;
import io.vertx.core.eventbus.Message;
import io.vertx.core.json.JsonObject;
import io.zero88.qwe.dto.ErrorMessage;
import io.zero88.qwe.dto.JsonData;
import io.zero88.qwe.dto.msg.DataTransferObject.StandardKey;
import io.zero88.qwe.dto.msg.RequestData;
import io.zero88.qwe.exceptions.QWEException;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Getter;
import lombok.NonNull;
import lombok.ToString;

/**
 * Represents for data transfer object in event bus system.
 *
 * @see EventStatus
 * @see EventAction
 * @see ErrorMessage
 * @since 1.0.0
 */
@ToString
@JsonInclude(Include.NON_NULL)
public final class EventMessage implements Serializable, JsonData {

    @Getter
    private final EventStatus status;
    @Getter
    private final EventAction action;
    @Getter
    private final EventAction prevAction;
    private final Map<String, Object> data;
    @JsonIgnore
    private final Class<? extends JsonData> dataClass;
    @Getter
    @JsonProperty
    private final ErrorMessage error;

    @JsonCreator
    private EventMessage(@JsonProperty(value = "status", defaultValue = "INITIAL") EventStatus status,
                         @NonNull @JsonProperty(value = "action", required = true) EventAction action,
                         @JsonProperty(value = "prevAction") EventAction prevAction,
                         @JsonProperty(value = "data") Map<String, Object> data,
                         @JsonProperty(value = "dataClass") Class<? extends JsonData> dataClass,
                         @JsonProperty(value = "error") ErrorMessage error) {
        this.status = Objects.isNull(status) ? EventStatus.INITIAL : status;
        this.action = action;
        this.prevAction = prevAction;
        this.data = data;
        this.dataClass = Objects.isNull(dataClass) ? DefaultJsonData.class : dataClass;
        this.error = error;
    }

    private EventMessage(EventStatus status, EventAction action) {
        this(status, action, null, null, null, null);
    }

    private EventMessage(EventStatus status, EventAction action, @NonNull ErrorMessage error) {
        this(status, action, null, error);
    }

    private EventMessage(EventStatus status, EventAction action, EventAction prevAction, @NonNull ErrorMessage error) {
        this(status, action, prevAction, null, null, error);
    }

    private EventMessage(EventStatus status, EventAction action, @NonNull JsonData data) {
        this(status, action, null, data.toJson().getMap(), data.getClass(), null);
    }

    private EventMessage(EventStatus status, EventAction action, JsonObject data) {
        this(status, action, null, data);
    }

    private EventMessage(EventStatus status, EventAction action, EventAction prevAction, JsonObject data) {
        this(status, action, prevAction, Objects.isNull(data) ? null : data.getMap(), null, null);
    }

    public static EventMessage error(EventAction action, @NonNull Throwable throwable) {
        return new EventMessage(EventStatus.FAILED, action, ErrorMessage.parse(throwable));
    }

    public static EventMessage error(@NonNull EventAction action, @NonNull ErrorCode code, String message) {
        return new EventMessage(EventStatus.FAILED, action, ErrorMessage.parse(code, message));
    }

    public static EventMessage error(@NonNull EventAction action, @NonNull ErrorMessage message) {
        return new EventMessage(EventStatus.FAILED, action, message);
    }

    public static EventMessage error(@NonNull EventAction action, EventAction prevAction,
                                     @NonNull ErrorMessage message) {
        return new EventMessage(EventStatus.FAILED, action, prevAction, message);
    }

    public static EventMessage replyError(EventAction action, @NonNull Throwable throwable) {
        return new EventMessage(EventStatus.FAILED, EventAction.REPLY, action, ErrorMessage.parse(throwable));
    }

    public static EventMessage initial(EventAction action) {
        return new EventMessage(EventStatus.INITIAL, action);
    }

    public static EventMessage initial(EventAction action, JsonObject data) {
        return new EventMessage(EventStatus.INITIAL, action, data);
    }

    public static EventMessage initial(EventAction action, JsonData data) {
        return new EventMessage(EventStatus.INITIAL, action, data);
    }

    public static EventMessage success(EventAction action) {
        return new EventMessage(EventStatus.SUCCESS, action);
    }

    public static EventMessage success(EventAction action, JsonObject data) {
        return new EventMessage(EventStatus.SUCCESS, action, data);
    }

    public static EventMessage success(EventAction action, EventAction prevAction, JsonObject data) {
        return from(EventStatus.SUCCESS, action, prevAction, data);
    }

    public static EventMessage replySuccess(EventAction action, JsonObject data) {
        return from(EventStatus.SUCCESS, EventAction.REPLY, action, data);
    }

    public static EventMessage success(EventAction action, JsonData data) {
        return new EventMessage(EventStatus.SUCCESS, action, data);
    }

    public static EventMessage from(@NonNull EventStatus status, @NonNull EventAction action, EventAction prevAction) {
        return new EventMessage(status, action, prevAction, (JsonObject) null);
    }

    public static EventMessage from(@NonNull EventStatus status, @NonNull EventAction action, EventAction prevAction,
                                    JsonObject data) {
        return new EventMessage(status, action, prevAction, data);
    }

    public static EventMessage override(@NonNull EventMessage message, @NonNull EventAction action) {
        if (message.isError()) {
            return error(action, message.getAction(), message.getError());
        }
        return from(message.getStatus(), action, message.getAction(), message.getData());
    }

    /**
     * Try parse given object to {@link EventMessage}
     *
     * @param object any non null object
     * @return event message instance
     * @throws QWEException if wrong format
     */
    public static EventMessage tryParse(@NonNull Object object) {
        return tryParse(object, false);
    }

    /**
     * Try parse with fallback data
     *
     * @param object  any non null object
     * @param lenient {@code true} if want to force given object to data json with {@code action} is {@link
     *                EventAction#UNKNOWN}
     * @return event message instance
     */
    public static EventMessage tryParse(@NonNull Object object, boolean lenient) {
        try {
            return JsonData.from(object, EventMessage.class, "Invalid event message format");
        } catch (QWEException e) {
            if (lenient) {
                return EventMessage.initial(EventAction.UNKNOWN, JsonData.tryParse(object));
            }
            throw e;
        }
    }

    public static EventMessage convert(Message<Object> message) {
        if (Objects.isNull(message)) {
            return EventMessage.initial(EventAction.UNKNOWN);
        }
        final EventMessage msg = tryParse(message.body());
        return message.headers().contains("action")
               ? override(msg, EventAction.parse(message.headers().get("action")))
               : msg;
    }

    /**
     * Event message data
     *
     * @return event message data in json
     * @apiNote If {@link EventMessage} is used in request mode, then as {@code QWE} standard message, the json data
     *     will be {@link RequestData} with one of known key data in {@link StandardKey}
     * @see RequestData
     */
    @JsonInclude(Include.NON_EMPTY)
    @JsonProperty
    public JsonObject getData() {
        return Objects.isNull(data) ? null : JsonData.from(data, dataClass).toJson();
    }

    @SuppressWarnings("unchecked")
    public <T> T data() {
        return Objects.isNull(data) ? null : (T) JsonData.from(data, dataClass);
    }

    @JsonIgnore
    public boolean isSuccess() {
        return this.status == EventStatus.SUCCESS;
    }

    @JsonIgnore
    public boolean isError() {
        return this.status == EventStatus.FAILED;
    }

}
