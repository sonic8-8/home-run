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

    private String category;

    private String sentiment;

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
        String category,
        String sentiment,
        Map<String, Object> sectorImpact,
        Integer exchangeRateImpact,
        Integer realEstateImpact,
        Map<String, Object> jobImpact
    ) {
        this.newsId = newsId;
        this.title = title;
        this.category = category;
        this.sentiment = sentiment;
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
            category,
            sentiment,
            sectorImpact,
            exchangeRateImpact,
            realEstateImpact,
            jobImpact
        );
    }
}
