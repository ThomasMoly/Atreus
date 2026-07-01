package com.example.Ai;

import jakarta.persistence.*;

@Entity
@Table(name = "recommended_cards")
public class RecommendedCard {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String username;

    public String getCardDetails() {
        return cardDetails;
    }

    public void setCardDetails(String cardDetails) {
        this.cardDetails = cardDetails;
    }

    @Column(name = "card_details")
    private String cardDetails;

    @Column(name = "\"Card_Name\"")
    private String cardName;

    @Column(name = "\"order\"")
    private Integer recommendationOrder;

    @Column(name = "\"InfoOfUser\"")
    private String infoOfUser;

    @Column(name = "\"ExtendedInfoOfUser\"")
    private String extendedInfoOfUser;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public Integer getRecommendationOrder() {
        return recommendationOrder;
    }

    public void setRecommendationOrder(Integer recommendationOrder) {
        this.recommendationOrder = recommendationOrder;
    }

    public String getCardName() {
        return cardName;
    }

    public void setCardName(String cardName) {
        this.cardName = cardName;
    }

    public String getInfoOfUser() {
        return infoOfUser;
    }

    public void setInfoOfUser(String infoOfUser) {
        this.infoOfUser = infoOfUser;
    }

    public String getExtendedInfoOfUser() {
        return extendedInfoOfUser;
    }

    public void setExtendedInfoOfUser(String extendedInfoOfUser) {
        this.extendedInfoOfUser = extendedInfoOfUser;
    }

    public Double getEstimatedRewardsValue() {
        return estimatedRewardsValue;
    }

    public void setEstimatedRewardsValue(Double estimatedRewardsValue) {
        this.estimatedRewardsValue = estimatedRewardsValue;
    }

    @Column(name = "estimated_rewards_value")
    private Double estimatedRewardsValue;

    // getters and setters
}