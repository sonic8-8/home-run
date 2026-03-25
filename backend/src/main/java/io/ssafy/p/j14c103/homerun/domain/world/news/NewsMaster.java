package io.ssafy.p.j14c103.homerun.domain.world.news;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.util.Map;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

@Getter
@Entity
@Table(name = "news_master")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class NewsMaster {

    @Id
    @Column(name = "news_id")
    private String newsId;

    private String title;

    private String sentiment;

    @Column(name = "source_name")
    private String sourceName;

    @Column(name = "article_text", columnDefinition = "text")
    private String articleText;

    @Column(name = "economic_cycle_type")
    private String economicCycleType;

    private String reason;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "sector_impact")
    private Map<String, Object> sectorImpact;

    @Column(name = "exchange_rate_impact")
    private Integer exchangeRateImpact;

    @Column(name = "real_estate_impact")
    private Integer realEstateImpact;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "job_impact")
    private Map<String, Object> jobImpact;

    private NewsMaster(
        String newsId,
        String title,
        String sentiment,
        String sourceName,
        String articleText,
        String economicCycleType,
        String reason,
        Map<String, Object> sectorImpact,
        Integer exchangeRateImpact,
        Integer realEstateImpact,
        Map<String, Object> jobImpact
    ) {
        this.newsId = newsId;
        this.title = title;
        this.sentiment = sentiment;
        this.sourceName = sourceName;
        this.articleText = articleText;
        this.economicCycleType = economicCycleType;
        this.reason = reason;
        this.sectorImpact = sectorImpact;
        this.exchangeRateImpact = exchangeRateImpact;
        this.realEstateImpact = realEstateImpact;
        this.jobImpact = jobImpact;
    }

    public static NewsMaster create(
        String newsId,
        String title,
        String category,
        String sentiment,
        Map<String, Object> sectorImpact,
        Integer exchangeRateImpact,
        Integer realEstateImpact,
        Map<String, Object> jobImpact
    ) {
        return new NewsMaster(
            newsId,
            title,
            sentiment,
            null,
            null,
            null,
            null,
            sectorImpact,
            exchangeRateImpact,
            realEstateImpact,
            jobImpact
        );
    }

    public static NewsMaster createAiNews(
        String newsId,
        String title,
        String sentiment,
        String sourceName,
        String articleText,
        String economicCycleType,
        String reason,
        Map<String, Object> sectorImpact,
        Integer exchangeRateImpact,
        Integer realEstateImpact,
        Map<String, Object> jobImpact
    ) {
        return new NewsMaster(
            newsId,
            title,
            sentiment,
            sourceName,
            articleText,
            economicCycleType,
            reason,
            sectorImpact,
            exchangeRateImpact,
            realEstateImpact,
            jobImpact
        );
    }
}
