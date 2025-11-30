package com.example;

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
        EntityList<SampleUser> sampleUserList = findUser();
        getCount(sampleUserList.size());
        return sampleUserList;
    }

    @WithSpan
    private int getCount(@SpanAttribute("userCount") int userCount) {
        SpanContext ctx = Span.fromContext(Context.current()).getSpanContext();
        LOGGER.logInfo("Trace ID Test", Map.of(
            "traceId", ctx.getTraceId(),
            "spanId", ctx.getSpanId(),
            "userCount", userCount
        ));
        return userCount;
    }

    @WithSpan
    private EntityList<SampleUser> findUser() {
        SpanContext ctx = Span.fromContext(Context.current()).getSpanContext();
        LOGGER.logInfo("Trace ID Test_2", Map.of(
            "traceId", ctx.getTraceId(),
            "spanId", ctx.getSpanId()
        ));
        return UniversalDao.findAll(SampleUser.class);
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
