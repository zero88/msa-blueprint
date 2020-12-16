package io.github.zero88.msa.bp.micro.discovery;

import org.junit.Before;
import org.junit.Test;

import io.github.zero88.msa.bp.TestHelper;
import io.github.zero88.msa.bp.TestHelper.JsonHelper;
import io.github.zero88.msa.bp.dto.msg.DataTransferObject.Headers;
import io.github.zero88.msa.bp.dto.msg.RequestData;
import io.github.zero88.msa.bp.event.EventAction;
import io.github.zero88.msa.bp.exceptions.BlueprintException;
import io.github.zero88.msa.bp.exceptions.ErrorCode;
import io.github.zero88.msa.bp.micro.BaseMicroServiceTest;
import io.github.zero88.msa.bp.micro.discovery.mock.MockServiceInvoker;
import io.github.zero88.msa.bp.micro.discovery.mock.MockServiceListener;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.unit.Async;
import io.vertx.ext.unit.TestContext;

public class GatewayServiceInvokerTest extends BaseMicroServiceTest {

    @Before
    public void setup(TestContext context) {
        super.setup(context);
        eventClient.register(EVENT_ADDRESS_1, new MockServiceListener());
    }

    @Test
    public void test_get_not_found_service(TestContext context) {
        Async async = context.async();
        MockServiceInvoker invoker = new MockServiceInvoker(config.getGatewayConfig().getIndexAddress(), eventClient,
                                                            EVENT_RECORD_1 + "...");
        invoker.execute(EventAction.CREATE, RequestData.builder().build())
               .subscribe(d -> TestHelper.testComplete(async), t -> {
                   context.assertTrue(t instanceof BlueprintException);
                   assert t instanceof BlueprintException;
                   BlueprintException e = (BlueprintException) t;
                   context.assertEquals(ErrorCode.SERVICE_NOT_FOUND, e.errorCode());
                   context.assertEquals(invoker.serviceLabel() +
                                        " is not found or out of service. Try again later | Error: SERVICE_NOT_FOUND",
                                        e.getMessage());
                   TestHelper.testComplete(async);
               });
    }

    @Test
    public void test_get_not_found_action(TestContext context) {
        Async async = context.async();
        MockServiceInvoker invoker = new MockServiceInvoker(config.getGatewayConfig().getIndexAddress(), eventClient,
                                                            EVENT_RECORD_1);
        invoker.execute(EventAction.UNKNOWN, RequestData.builder().build())
               .subscribe(d -> TestHelper.testComplete(async), t -> {
                   context.assertTrue(t instanceof BlueprintException);
                   assert t instanceof BlueprintException;
                   BlueprintException e = (BlueprintException) t;
                   context.assertEquals(ErrorCode.SERVICE_NOT_FOUND, e.errorCode());
                   context.assertEquals(invoker.serviceLabel() +
                                        " is not found or out of service. Try again later | Error: SERVICE_NOT_FOUND",
                                        e.getMessage());
                   TestHelper.testComplete(async);
               });
    }

    @Test
    public void test_execute_service_failed(TestContext context) {
        Async async = context.async();
        MockServiceInvoker invoker = new MockServiceInvoker(config.getGatewayConfig().getIndexAddress(), eventClient,
                                                            EVENT_RECORD_1);
        invoker.execute(EventAction.UPDATE, RequestData.builder().build())
               .subscribe(d -> JsonHelper.assertJson(context, async,
                                                     new JsonObject().put("code", ErrorCode.INVALID_ARGUMENT.code())
                                                                     .put("message", "hey"), d), context::fail);
    }

    @Test
    public void test_execute_service_success(TestContext context) {
        Async async = context.async();
        MockServiceInvoker invoker = new MockServiceInvoker(config.getGatewayConfig().getIndexAddress(), eventClient,
                                                            EVENT_RECORD_1);
        final JsonObject expected = new JsonObject().put(Headers.X_REQUEST_BY, "service/" + invoker.requester())
                                                    .put("action", EventAction.CREATE.action());
        invoker.execute(EventAction.CREATE, RequestData.builder().build())
               .subscribe(d -> JsonHelper.assertJson(context, async, expected, d), context::fail);
    }

}