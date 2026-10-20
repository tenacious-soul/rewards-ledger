package com.rewards.ledger.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;

@Entity
@Table(name = "rewards")
public class Reward {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, updatable = false)
    private Long memberId;

    @Convert(converter = PointTypeConverter.class)
    @Column(name = "point_type_id", nullable = false, updatable = false)
    private PointType pointType;

    @Column(nullable = false, updatable = false)
    private long points;

    private String description;

    @Column(nullable = false, updatable = false)
    private Instant eventDate;

    protected Reward() {
    }

    public Reward(Long memberId, PointType pointType, long points, String description, Instant eventDate) {
        this.memberId = memberId;
        this.pointType = pointType;
        this.points = points;
        this.description = description;
        this.eventDate = eventDate;
    }

    public Long getId() {
        return id;
    }

    public Long getMemberId() {
        return memberId;
    }

    public PointType getPointType() {
        return pointType;
    }

    public long getPoints() {
        return points;
    }

    public String getDescription() {
        return description;
    }

    public Instant getEventDate() {
        return eventDate;
    }
}
