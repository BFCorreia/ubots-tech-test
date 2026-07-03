package com.flowpay.backend.service;

import com.flowpay.backend.domain.Team;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertEquals;

class TeamRoutingStrategyTest {

    private final TeamRoutingStrategy strategy = new TeamRoutingStrategy();

    @Test
    void shouldRouteToCards() {
        assertEquals(Team.CARDS, strategy.determineTeam("Cartão de crédito"));
        assertEquals(Team.CARDS, strategy.determineTeam("cartao bloqueado"));
        assertEquals(Team.CARDS, strategy.determineTeam("CARTÃO"));
    }

    @Test
    void shouldRouteToLoans() {
        assertEquals(Team.LOANS, strategy.determineTeam("EMPRÉSTIMO consignado"));
        assertEquals(Team.LOANS, strategy.determineTeam("Quero um emprestimo"));
    }

    @Test
    void shouldRouteToOtherAndLog() {
        assertEquals(Team.OTHER, strategy.determineTeam("Problemas com a conta"));
        assertEquals(Team.OTHER, strategy.determineTeam(""));
        assertEquals(Team.OTHER, strategy.determineTeam(null));
    }
}
