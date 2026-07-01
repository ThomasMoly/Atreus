package com.example.Ai;

import com.fasterxml.jackson.core.JsonProcessingException;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.stereotype.Service;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.transaction.annotation.Transactional;
import java.util.ArrayList;
import java.util.List;

@Service
public class AiService {

    private final ChatClient chatClient;
    private final AiRepo aiRepo;
    private final RecommendedCardRepository recommendedCardRepository;
    private final ObjectMapper objectMapper = new ObjectMapper();

    private static final String SYSTEM_PROMPT = """
You are an expert FinTech Recommendation Engine for a Synchrony hackathon.

Your task is to analyze a user's historical credit card statement data and recommend exactly four Synchrony credit cards ranked by estimated rewards value.

INPUT DATA FORMAT:
You will receive raw text lines like:
"[ACCOUNT: BANK / CARD TYPE] BALANCES... || LEDGER: DATE BRAND_NAME EXPENSE;"

ANALYTICAL RULES:
1. Only analyze the LEDGER segment.
2. Match merchant names case-insensitively.
3. Ignore punctuation, store numbers, and location identifiers.
4. Aggregate total dollar spend per eligible merchant group.
5. Calculate estimated_rewards_value as eligible_spend * cashback_rate.
6. Round all monetary values to two decimal places.
7. Return exactly four recommendations.
8. Sort recommendations by estimated_rewards_value from highest to lowest.
9. Never recommend the same card twice.
10. Do not invent transactions, merchant names, card benefits, rewards, or cashback rates.
11. If fewer than four merchant groups have direct matches, fill the remaining slots with the next most relevant cards based on similar spending behavior, but set eligible_spend and estimated_rewards_value to 0 if no matching spend exists.

MERCHANT TO CARD MAPPING:

TJ MAXX / MARSHALLS / SIERRA / HOMEGOODS
Card: TJX Rewards® Platinum Mastercard®
Cashback Rate: 0.05
Card Details: Perfect for frequent TJX-family shoppers. Earn 5% back in Rewards at T.J.Maxx, Marshalls, HomeGoods, Sierra, and Homesense, plus 2% back on eligible gas, grocery, and restaurant purchases and 1% back everywhere else Mastercard is accepted. This card is ideal for customers who regularly shop for apparel, home décor, and seasonal merchandise while earning rewards on everyday spending.

LOWE'S
Card: Lowe's Advantage Card
Cashback Rate: 0.05
Card Details: A great choice for homeowners and DIY enthusiasts. Receive an everyday 5% discount on eligible Lowe's purchases or choose special financing on qualifying purchases. Ideal for customers who frequently invest in home improvement projects, appliances, or building materials.

PAYPAL
Card: PayPal Cashback Mastercard®
Cashback Rate: 0.03
Card Details: Best suited for customers who frequently shop online using PayPal. Earn unlimited 3% cash back on purchases made through PayPal Checkout and 1.5% cash back on all other eligible purchases wherever Mastercard is accepted.

SAM'S CLUB
Card: Sam's Club® Mastercard®
Cashback Rate: 0.03
Card Details: Designed for Sam's Club members looking to maximize warehouse shopping rewards. Earn 3% back on Sam's Club purchases for Plus members, 5% back on eligible gas purchases (up to the annual cap), 3% back on dining and takeout, and 1% back on all other purchases.

WALGREENS / CVS
Card: myWalgreens® Mastercard®
Cashback Rate: 0.05
Card Details: A strong option for customers who regularly purchase pharmacy, health, and wellness products. Earn 10% Walgreens Cash rewards on Walgreens-branded products, 5% on other Walgreens purchases, 3% on grocery and health & wellness purchases outside Walgreens, and 1% everywhere else Mastercard is accepted.

AMAZON
Card: Amazon Store Card
Cashback Rate: 0.05
Card Details: Ideal for frequent Amazon shoppers. Eligible Prime members can earn 5% back on Amazon purchases, making this card especially valuable for recurring online shopping and household purchases.

JCPENNEY
Card: JCPenney Credit Card
Cashback Rate: 0.05
Card Details: Designed for loyal JCPenney customers. Cardholders can earn rewards on eligible JCPenney purchases while receiving access to exclusive discounts, special financing offers, and promotional shopping events.

DISCOUNT TIRE
Card: Discount Tire Credit Card
Cashback Rate: 0.05
Card Details: Perfect for drivers who frequently purchase tires or vehicle maintenance. Cardholders receive promotional financing options and special savings on eligible tire and automotive service purchases, helping reduce long-term vehicle maintenance costs.

OUTPUT:
Return ONLY raw valid JSON.
Do not use markdown.
Do not wrap the response in ```json.
The first character must be { and the last character must be }.

{
  "recommendations": [
    {
      "order": 1,
      "card_name": "string",
      "brand_affinity": "string",
      "eligible_spend": 0.00,
      "cashback_rate": 0.00,
      "estimated_rewards_value": 0.00,
      "card_details": "string"
    }
  ]
}
""";

    public AiService(ChatClient chatClient, AiRepo aiRepo, RecommendedCardRepository userRepo) {
        this.chatClient = chatClient;
        this.aiRepo = aiRepo;
        this.recommendedCardRepository = userRepo;
    }

    @Transactional
    public List<RecommendedCard> generateRecommendationsForUser(String username) throws Exception {

        List<RecommendedCard> existingCards =
                recommendedCardRepository.findByUsernameOrderByRecommendationOrderAsc(username);

        if (!existingCards.isEmpty()) {
            return existingCards;
        }

        List<StatementData> statements = aiRepo.findByUsername(username);

        if (statements.isEmpty()) {
            throw new RuntimeException("No statements found for username: " + username);
        }

        String combinedStatementData = statements.stream()
                .map(StatementData::getStatementData)
                .reduce("", (a, b) -> a + "\n" + b);

        String aiJson = chatClient.prompt()
                .system(SYSTEM_PROMPT)
                .user(combinedStatementData)
                .call()
                .content();

        aiJson = aiJson
                .replace("```json", "")
                .replace("```", "")
                .trim();

        ObjectMapper objectMapper = new ObjectMapper();
        JsonNode root = objectMapper.readTree(aiJson);
        JsonNode recommendations = root.get("recommendations");

        List<RecommendedCard> savedCards = new ArrayList<>();

        for (JsonNode rec : recommendations) {
            RecommendedCard card = new RecommendedCard();

            card.setUsername(username);
            card.setRecommendationOrder(rec.get("order").asInt());
            card.setCardName(rec.get("card_name").asText());
            card.setEstimatedRewardsValue(rec.get("estimated_rewards_value").asDouble());
            card.setCardDetails(rec.get("card_details").asText());

            card.setInfoOfUser(
                    String.format(
                            "You could have saved $%.2f if you used the %s.",
                            card.getEstimatedRewardsValue() * 6,
                            card.getCardName()
                    )
            );

            card.setExtendedInfoOfUser(
                    String.format(
                            "Based on your spending, you are projected to save approximately $%.2f over the next 6 months with the %s.",
                            card.getEstimatedRewardsValue() * 6,
                            card.getCardName()
                    )
            );

            savedCards.add(recommendedCardRepository.save(card));
        }

        return savedCards;
    }

    public List<?> getStatementsByUserName(String username) {
        return aiRepo.findByUsername(username);
    }
}