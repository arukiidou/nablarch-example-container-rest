package com.example;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.example.dto.SampleUserListDto;
import com.example.entity.SampleUser;

import io.opentelemetry.instrumentation.annotations.SpanAttribute;
import io.opentelemetry.instrumentation.annotations.WithSpan;
import io.opentelemetry.api.trace.Span;
import io.opentelemetry.api.trace.SpanContext;
import io.opentelemetry.context.Context;
import nablarch.common.dao.EntityList;
import nablarch.common.dao.UniversalDao;
import nablarch.core.log.Logger;
import nablarch.core.log.LoggerManager;
import nablarch.fw.web.HttpRequest;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;

import jakarta.ws.rs.client.ClientBuilder;

/**
 * 疎通確認用のアクションクラス。
 *
 * @deprecated TODO 疎通確認用のクラスです。確認完了後、削除してください。
 */
@Path("/find")
public class SampleAction {

    private static final Logger LOGGER = LoggerManager.get(SampleAction.class);

    /**
     * 検索処理。
     * <p>
     * 応答にJSONを使用する。
     * </p>
     *
     * @param req HTTPリクエスト
     * @return ユーザ情報(JSON)
     */
    @GET
    @Path("/json")
    @Produces(MediaType.APPLICATION_JSON)
    public EntityList<SampleUser> findProducesJson(HttpRequest req) {
        SpanContext ctx = Span.fromContext(Context.current()).getSpanContext();
        var headers = new HashMap<>(req.getHeaderMap());
        headers.putAll(Map.of(
            "traceId", ctx.getTraceId(),
            "spanId", ctx.getSpanId()
        ));
        LOGGER.logInfo("これはサービス間伝播のテストです。", headers);

        EntityList<SampleUser> sampleUserList = findUser();
        getCount(sampleUserList.size());
        return sampleUserList;
    }

    @WithSpan
    private int getCount(@SpanAttribute("userCount") int userCount) {
        SpanContext ctx = Span.fromContext(Context.current()).getSpanContext();
        LOGGER.logInfo("これはトレースIDと任意属性を添加するテストです。", Map.of(
            "traceId", ctx.getTraceId(),
            "spanId", ctx.getSpanId(),
            "userCount", userCount
        ));
        return userCount;
    }

    @WithSpan
    private EntityList<SampleUser> findUser() {
        SpanContext ctx = Span.fromContext(Context.current()).getSpanContext();
        LOGGER.logInfo("これはトレースIDのテストです。", Map.of(
            "traceId", ctx.getTraceId(),
            "spanId", ctx.getSpanId()
        ));
        return UniversalDao.findAll(SampleUser.class);
    }

    @SuppressWarnings("unchecked")
    @GET
    @Path("/jsonClient")
    @Produces(MediaType.APPLICATION_JSON)
    public List<SampleUser> findProducesJsonAsClient(HttpRequest req) {
        try (var client = ClientBuilder.newClient()) {
            List<SampleUser> sampleUserList = client.target("http://example-rest:8080")
                .path("/find/json")
                .request(MediaType.APPLICATION_JSON)
                .get(List.class);
            getCount(sampleUserList.size());
            return sampleUserList;
        } catch (Exception e) {
            var ctx = Context.current();
            Span.fromContext(ctx).recordException(e);
            SpanContext span = Span.fromContext(ctx).getSpanContext();
            LOGGER.logInfo("これはトレースIDのテストです。", Map.of(
                "traceId", span.getTraceId(),
                "spanId", span.getSpanId()
            ));
            throw e;
        }
    }

    /**
     * 検索処理。
     * <p>
     * 応答にXMLを使用する。
     * </p>
     *
     * @param req HTTPリクエスト
     * @return ユーザ情報(XML)
     */
    @GET
    @Path("/xml")
    @Produces(MediaType.APPLICATION_XML)
    public SampleUserListDto findProducesXml(HttpRequest req) {
        EntityList<SampleUser> sampleUserList = findUser();
        return new SampleUserListDto(sampleUserList);
    }

}
