package com.cloud.userauth.interfaces.rest;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.cloud.framework.core.AuthenticatedSessionRequestContext;
import com.cloud.framework.core.ClientChannelRequest;
import com.cloud.framework.core.ClientRequest;
import com.cloud.framework.core.PageResult;
import jakarta.validation.Valid;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.Arrays;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.core.annotation.AnnotatedElementUtils;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

class ControllerClientRequestContractTest {

    @Test
    void shouldInjectGatewayClientContextIntoEveryRequestBody() {
        List<Class<?>> controllers = List.of(
                UserAuthenticationController.class,
                UserAuthorizationController.class,
                WebViewHandoffController.class);

        controllers.stream()
                .flatMap(controller -> List.of(controller.getDeclaredMethods()).stream())
                .flatMap(method -> List.of(method.getParameters()).stream()
                        .filter(ControllerClientRequestContractTest::isRequestBody)
                        .map(parameter -> new RequestBodyParameter(method, parameter)))
                .forEach(requestBody -> assertTrue(
                        ClientRequest.class.isAssignableFrom(requestBody.parameter().getType()),
                        () -> "%s#%s request body %s must extend ClientRequest"
                                .formatted(
                                        requestBody.method().getDeclaringClass().getSimpleName(),
                                        requestBody.method().getName(),
                                        requestBody.parameter().getType().getSimpleName())));
    }

    @Test
    void shouldTriggerBeanValidationForEveryClientRequestParameter() {
        controllers().stream()
                .flatMap(controller -> Arrays.stream(controller.getDeclaredMethods()))
                .flatMap(method -> Arrays.stream(method.getParameters())
                        .filter(parameter -> ClientRequest.class.isAssignableFrom(parameter.getType()))
                        .map(parameter -> new ClientRequestParameter(method, parameter)))
                .forEach(clientRequest -> assertTrue(
                        clientRequest.parameter().isAnnotationPresent(Valid.class),
                        () -> "%s#%s parameter %s must be annotated with @Valid"
                                .formatted(
                                        clientRequest.method().getDeclaringClass().getSimpleName(),
                                        clientRequest.method().getName(),
                                        clientRequest.parameter().getType().getSimpleName())));
    }

    @Test
    void shouldNotExposeChannelCodeInChannelPolicyPaths() {
        Arrays.stream(UserAuthorizationController.class.getDeclaredMethods())
                .filter(method -> method.getName().endsWith("ChannelPolicy"))
                .map(method -> AnnotatedElementUtils.findMergedAnnotation(method, RequestMapping.class))
                .forEach(mapping -> assertTrue(
                        mapping != null
                                && Arrays.stream(mapping.path()).noneMatch(path -> path.contains("{channelCode}")),
                        "admin targetChannelCode must come from the request body"));
    }

    @Test
    void shouldReceiveGatewayClientContextForEveryAuthorizationEndpoint() {
        Arrays.stream(UserAuthorizationController.class.getDeclaredMethods())
                .filter(method -> AnnotatedElementUtils.hasAnnotation(method, RequestMapping.class))
                .forEach(method -> assertTrue(
                        Arrays.stream(method.getParameterTypes())
                                .anyMatch(ClientRequest.class::isAssignableFrom),
                        () -> "UserAuthorizationAdminController#%s must receive ClientRequest context"
                                .formatted(method.getName())));
    }

    @Test
    void shouldNotUseGatewayChannelOrUserContextAsAdminTarget() {
        Arrays.stream(UserAuthorizationController.class.getDeclaredMethods())
                .filter(method -> AnnotatedElementUtils.hasAnnotation(method, RequestMapping.class))
                .flatMap(method -> Arrays.stream(method.getParameterTypes()))
                .filter(ClientRequest.class::isAssignableFrom)
                .forEach(requestType -> {
                    assertFalse(ClientChannelRequest.class.isAssignableFrom(requestType),
                            "admin target channel must not use X-Channel-Code context");
                    assertFalse(AuthenticatedSessionRequestContext.class.isAssignableFrom(requestType),
                            "admin target user must not use X-User-Id context");
                });
    }

    @Test
    void shouldUseExplicitTargetRequestForUserAuthorizationQuery() throws NoSuchMethodException {
        Method method = UserAuthorizationController.class.getDeclaredMethod(
                "getUserAuthorization", UserAuthorizationController.UserTargetRequest.class);
        RequestMapping mapping = AnnotatedElementUtils.findMergedAnnotation(method, RequestMapping.class);

        assertTrue(mapping != null
                        && Arrays.stream(mapping.path()).noneMatch(path -> path.contains("{userId}")),
                "admin targetUserId must come from the request body");
    }

    @Test
    void shouldUsePagedContractForAuthorizationCatalogQueries() throws NoSuchMethodException {
        Method permissions = UserAuthorizationController.class.getDeclaredMethod(
                "getPermissions", UserAuthorizationController.CatalogPageRequest.class);
        Method roles = UserAuthorizationController.class.getDeclaredMethod(
                "getRoles", UserAuthorizationController.CatalogPageRequest.class);

        assertTrue(PageResult.class.equals(permissions.getReturnType()),
                "permission catalog must return PageResult");
        assertTrue(PageResult.class.equals(roles.getReturnType()),
                "role catalog must return PageResult");
    }

    private static boolean isRequestBody(Parameter parameter) {
        return parameter.isAnnotationPresent(RequestBody.class);
    }

    private static List<Class<?>> controllers() {
        return List.of(
                UserAuthenticationController.class,
                UserAuthorizationController.class,
                WebViewHandoffController.class);
    }

    private record RequestBodyParameter(Method method, Parameter parameter) {
    }

    private record ClientRequestParameter(Method method, Parameter parameter) {
    }
}
