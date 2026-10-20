package com.rewards.ledger;

import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.jayway.jsonpath.JsonPath;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

@SpringBootTest
@AutoConfigureMockMvc
class RewardsLedgerApiTest {

    private static final Instant NOW = Instant.parse("2024-01-15T09:00:00Z");

    @Autowired
    private MockMvc mockMvc;

    @TestConfiguration
    static class FixedClockConfig {

        @Bean
        @Primary
        Clock fixedClock() {
            return Clock.fixed(NOW, ZoneOffset.UTC);
        }
    }

    @Test
    void createsMember() throws Exception {
        String email = uniqueEmail();

        postJson("/members", """
                {"name": "Alice Johnson", "email": "%s"}
                """.formatted(email))
                .andExpect(status().isCreated())
                .andExpect(header().exists("Location"))
                .andExpect(jsonPath("$.member_id").isNumber())
                .andExpect(jsonPath("$.name").value("Alice Johnson"))
                .andExpect(jsonPath("$.email").value(email))
                .andExpect(jsonPath("$.created_at").value("2024-01-15T09:00:00Z"));
    }

    @Test
    void computesBalanceFromAllRewardEntries() throws Exception {
        long memberId = createMember();
        createReward(memberId, 1, 500, "Purchase at Store A", "2024-02-01T14:22:10Z");
        createReward(memberId, 2, 200, "Referred user Bob", "2024-02-05T10:15:33Z");
        createReward(memberId, 3, 50, "Cashback on bill payment", "2024-02-10T08:45:00Z");
        createReward(memberId, 4, 300, "Redeemed for voucher", "2024-02-15T16:30:45Z")
                .andExpect(jsonPath("$.points").value(-300));

        mockMvc.perform(get("/members/{id}", memberId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.member_id").value(memberId))
                .andExpect(jsonPath("$.points_balance").value(450))
                .andExpect(jsonPath("$.created_at").value("2024-01-15T09:00:00Z"));
    }

    @Test
    void createsRewardWithExpectedShape() throws Exception {
        long memberId = createMember();

        createReward(memberId, 1, 500, "Purchase at Store A", "2024-02-01T14:22:10Z")
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.reward_id").isNumber())
                .andExpect(jsonPath("$.member_id").value(memberId))
                .andExpect(jsonPath("$.point_type_id").value(1))
                .andExpect(jsonPath("$.points").value(500))
                .andExpect(jsonPath("$.description").value("Purchase at Store A"))
                .andExpect(jsonPath("$.event_date").value("2024-02-01T14:22:10Z"));
    }

    @Test
    void defaultsEventDateToNow() throws Exception {
        long memberId = createMember();

        postJson("/rewards", """
                {"member_id": %d, "point_type_id": 3, "points": 50}
                """.formatted(memberId))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.event_date").value("2024-01-15T09:00:00Z"));
    }

    @Test
    void listsMemberRewardsInChronologicalOrder() throws Exception {
        long memberId = createMember();
        createReward(memberId, 2, 200, "Referred user Bob", "2024-02-05T10:15:33Z");
        createReward(memberId, 1, 500, "Purchase at Store A", "2024-02-01T14:22:10Z");

        mockMvc.perform(get("/members/{id}/rewards", memberId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].point_type_id").value(1))
                .andExpect(jsonPath("$[1].point_type_id").value(2));
    }

    @Test
    void listsNoRewardsForNewMember() throws Exception {
        long memberId = createMember();

        mockMvc.perform(get("/members/{id}/rewards", memberId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(0)));
    }

    @Test
    void rejectsDuplicateEmailIgnoringCase() throws Exception {
        String email = uniqueEmail();
        postJson("/members", memberJson(email)).andExpect(status().isCreated());

        postJson("/members", memberJson(email.toUpperCase()))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.detail").exists());
    }

    @Test
    void rejectsInvalidMemberPayload() throws Exception {
        postJson("/members", """
                {"name": "", "email": "not-an-email"}
                """)
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors.name").exists())
                .andExpect(jsonPath("$.errors.email").exists());
    }

    @Test
    void rejectsRedemptionExceedingBalance() throws Exception {
        long memberId = createMember();
        createReward(memberId, 1, 100, "Purchase", "2024-02-01T14:22:10Z");

        createReward(memberId, 4, 101, "Too much", "2024-02-02T14:22:10Z")
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.detail").value("Insufficient balance: requested 101 points but only 100 available"));

        mockMvc.perform(get("/members/{id}", memberId))
                .andExpect(jsonPath("$.points_balance").value(100));
    }

    @Test
    void rejectsRewardForUnknownMember() throws Exception {
        createReward(Long.MAX_VALUE, 1, 100, "Purchase", "2024-02-01T14:22:10Z")
                .andExpect(status().isNotFound());
    }

    @Test
    void rejectsUnknownPointType() throws Exception {
        long memberId = createMember();

        createReward(memberId, 5, 100, "Unknown", "2024-02-01T14:22:10Z")
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.detail").value("Invalid point_type_id 5"));
    }

    @Test
    void rejectsNonPositivePoints() throws Exception {
        long memberId = createMember();

        createReward(memberId, 1, 0, "Zero", "2024-02-01T14:22:10Z")
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors.points").exists());
    }

    @Test
    void returnsNotFoundForUnknownMember() throws Exception {
        mockMvc.perform(get("/members/{id}", Long.MAX_VALUE)).andExpect(status().isNotFound());
        mockMvc.perform(get("/members/{id}/rewards", Long.MAX_VALUE)).andExpect(status().isNotFound());
    }

    private long createMember() throws Exception {
        String body = postJson("/members", memberJson(uniqueEmail()))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        return ((Number) JsonPath.read(body, "$.member_id")).longValue();
    }

    private ResultActions createReward(long memberId, int pointTypeId, long points, String description,
                                       String eventDate) throws Exception {
        return postJson("/rewards", """
                {"member_id": %d, "point_type_id": %d, "points": %d, "description": "%s", "event_date": "%s"}
                """.formatted(memberId, pointTypeId, points, description, eventDate));
    }

    private ResultActions postJson(String path, String json) throws Exception {
        return mockMvc.perform(post(path).contentType(MediaType.APPLICATION_JSON).content(json));
    }

    private static String memberJson(String email) {
        return """
                {"name": "Alice Johnson", "email": "%s"}
                """.formatted(email);
    }

    private static String uniqueEmail() {
        return "user-" + UUID.randomUUID() + "@example.com";
    }
}
