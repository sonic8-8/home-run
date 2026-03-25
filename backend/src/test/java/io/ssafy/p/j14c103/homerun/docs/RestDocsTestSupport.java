package io.ssafy.p.j14c103.homerun.docs;

import static org.springframework.restdocs.headers.HeaderDocumentation.headerWithName;
import static org.springframework.restdocs.operation.preprocess.Preprocessors.preprocessRequest;
import static org.springframework.restdocs.operation.preprocess.Preprocessors.preprocessResponse;
import static org.springframework.restdocs.operation.preprocess.Preprocessors.prettyPrint;
import static org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath;
import static org.springframework.restdocs.payload.PayloadDocumentation.relaxedResponseFields;
import static org.springframework.restdocs.payload.PayloadDocumentation.responseFields;

import io.ssafy.p.j14c103.homerun.domain.user.auth.AuthenticatedUser;
import java.util.List;
import org.springframework.http.HttpHeaders;
import org.springframework.restdocs.headers.HeaderDescriptor;
import org.springframework.restdocs.mockmvc.MockMvcRestDocumentation;
import org.springframework.restdocs.mockmvc.RestDocumentationResultHandler;
import org.springframework.restdocs.payload.FieldDescriptor;
import org.springframework.restdocs.payload.JsonFieldType;
import org.springframework.restdocs.payload.ResponseFieldsSnippet;
import org.springframework.restdocs.snippet.Snippet;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.web.servlet.request.RequestPostProcessor;

public abstract class RestDocsTestSupport {

    protected RestDocumentationResultHandler document(String identifier, Snippet... snippets) {
        return MockMvcRestDocumentation.document(
                identifier,
                preprocessRequest(prettyPrint()),
                preprocessResponse(prettyPrint()),
                snippets
        );
    }

    protected ResponseFieldsSnippet apiResponseFields(
            String dataDescription,
            FieldDescriptor... dataFields
    ) {
        return responseFields(
                fieldWithPath("status").type(JsonFieldType.NUMBER).description("HTTP 상태 코드"),
                fieldWithPath("message").type(JsonFieldType.STRING).description("응답 메시지"),
                fieldWithPath("data").type(JsonFieldType.OBJECT).description(dataDescription)
        ).andWithPrefix("data.", dataFields);
    }

    protected ResponseFieldsSnippet relaxedApiResponseFields(
            String dataDescription,
            FieldDescriptor... dataFields
    ) {
        return relaxedResponseFields(
                fieldWithPath("status").type(JsonFieldType.NUMBER).description("HTTP 상태 코드"),
                fieldWithPath("message").type(JsonFieldType.STRING).description("응답 메시지"),
                fieldWithPath("data").type(JsonFieldType.OBJECT).description(dataDescription)
        ).andWithPrefix("data.", dataFields);
    }

    protected ResponseFieldsSnippet basicErrorResponseFields() {
        return responseFields(
                fieldWithPath("code").type(JsonFieldType.STRING).description("애플리케이션 에러 코드"),
                fieldWithPath("message").type(JsonFieldType.STRING).description("에러 메시지"),
                fieldWithPath("errors").type(JsonFieldType.ARRAY).description("추가 에러 정보 목록")
        );
    }

    protected ResponseFieldsSnippet validationErrorResponseFields() {
        return responseFields(
                fieldWithPath("code").type(JsonFieldType.STRING).description("애플리케이션 에러 코드"),
                fieldWithPath("message").type(JsonFieldType.STRING).description("에러 메시지"),
                fieldWithPath("errors").type(JsonFieldType.ARRAY).description("입력 검증 에러 목록"),
                fieldWithPath("errors[].field").type(JsonFieldType.STRING).description("검증 실패 필드명"),
                fieldWithPath("errors[].message").type(JsonFieldType.STRING).description("검증 실패 사유")
        );
    }

    protected HeaderDescriptor authorizationHeader() {
        return headerWithName(HttpHeaders.AUTHORIZATION)
                .description("Bearer 액세스 토큰");
    }

    protected HeaderDescriptor refreshAuthorizationHeader() {
        return headerWithName(HttpHeaders.AUTHORIZATION)
                .description("Bearer 리프레시 토큰");
    }

    protected RequestPostProcessor currentUser() {
        Authentication authentication = new UsernamePasswordAuthenticationToken(
                new AuthenticatedUser(1L, "user@example.com"),
                null,
                List.of()
        );

        return request -> {
            SecurityContext context = SecurityContextHolder.createEmptyContext();
            context.setAuthentication(authentication);
            SecurityContextHolder.setContext(context);
            request.setUserPrincipal(authentication);
            return request;
        };
    }
}
