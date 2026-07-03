package com.flowpay.backend.service;

import com.flowpay.backend.domain.Attendant;
import com.flowpay.backend.domain.Team;
import com.flowpay.backend.domain.Ticket;
import com.flowpay.backend.domain.TicketStatus;
import com.flowpay.backend.repository.AttendantRepository;
import com.flowpay.backend.repository.TicketRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.TestPropertySource;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Import(TicketDispatcherService.class)
@TestPropertySource(properties = {
    "spring.datasource.url=jdbc:tc:postgresql:15-alpine:///test",
    "spring.datasource.driver-class-name=org.testcontainers.jdbc.ContainerDatabaseDriver"
})
class TicketDispatcherServiceTest {

    @Autowired
    private TicketDispatcherService dispatcherService;

    @Autowired
    private AttendantRepository attendantRepository;

    @Autowired
    private TicketRepository ticketRepository;

    @BeforeEach
    void setUp() {
        ticketRepository.deleteAll();
        attendantRepository.deleteAll();
    }

    @Test
    void shouldAssignTicketToAvailableAttendantImmediately() {
        // Arrange: One available attendant in CARDS team
        Attendant attendant = Attendant.builder().name("John").team(Team.CARDS).build();
        attendantRepository.save(attendant);

        Ticket ticket = Ticket.builder().clientName("Alice").subject("Lost Card").team(Team.CARDS).build();
        ticket = ticketRepository.save(ticket);

        // Act
        dispatcherService.dispatchNext(Team.CARDS);

        // Assert
        Ticket updatedTicket = ticketRepository.findById(ticket.getId()).orElseThrow();
        assertThat(updatedTicket.getStatus()).isEqualTo(TicketStatus.IN_PROGRESS);
        assertThat(updatedTicket.getAttendant().getId()).isEqualTo(attendant.getId());
        assertThat(updatedTicket.getStartedAt()).isNotNull();
    }

    @Test
    void shouldRespectFifoOrderWhenAttendantBecomesAvailable() throws InterruptedException {
        // Arrange
        Attendant attendant = Attendant.builder().name("John").team(Team.CARDS).build();
        attendantRepository.save(attendant);

        // Fill capacity (3 in progress)
        for (int i = 0; i < 3; i++) {
            Ticket t = Ticket.builder()
                .clientName("Client " + i)
                .subject("Subject")
                .team(Team.CARDS)
                .status(TicketStatus.IN_PROGRESS)
                .attendant(attendant)
                .build();
            ticketRepository.save(t);
        }

        // Create 3 waiting tickets (T4, T5, T6) with explicit delays to guarantee createdAt order
        Ticket t4 = ticketRepository.save(Ticket.builder().clientName("Client 4").subject("S4").team(Team.CARDS).build());
        Thread.sleep(10);
        Ticket t5 = ticketRepository.save(Ticket.builder().clientName("Client 5").subject("S5").team(Team.CARDS).build());
        Thread.sleep(10);
        Ticket t6 = ticketRepository.save(Ticket.builder().clientName("Client 6").subject("S6").team(Team.CARDS).build());

        // Act - Attendant is busy, dispatch should not assign
        dispatcherService.dispatchNext(Team.CARDS);
        
        assertThat(ticketRepository.findById(t4.getId()).orElseThrow().getStatus()).isEqualTo(TicketStatus.WAITING);

        // Free up one slot by finishing the first ticket
        Ticket firstInProgress = ticketRepository.findAll().stream()
                .filter(t -> t.getStatus() == TicketStatus.IN_PROGRESS).findFirst().orElseThrow();
        firstInProgress.setStatus(TicketStatus.FINISHED);
        ticketRepository.save(firstInProgress);

        // Dispatch again
        dispatcherService.dispatchNext(Team.CARDS);

        // Assert - T4 should be assigned, T5 and T6 still waiting
        Ticket updatedT4 = ticketRepository.findById(t4.getId()).orElseThrow();
        assertThat(updatedT4.getStatus()).isEqualTo(TicketStatus.IN_PROGRESS);
        assertThat(updatedT4.getAttendant().getId()).isEqualTo(attendant.getId());

        assertThat(ticketRepository.findById(t5.getId()).orElseThrow().getStatus()).isEqualTo(TicketStatus.WAITING);
        assertThat(ticketRepository.findById(t6.getId()).orElseThrow().getStatus()).isEqualTo(TicketStatus.WAITING);
    }

    @Test
    void shouldLeaveTicketInWaitingWhenNoAttendantsAvailable() {
        // Arrange: NO attendants at all
        Ticket ticket = Ticket.builder().clientName("No Attendant").subject("S").team(Team.OTHER).build();
        ticket = ticketRepository.save(ticket);
        
        // Act
        dispatcherService.dispatchNext(Team.OTHER);
        
        // Assert
        assertThat(ticketRepository.findById(ticket.getId()).orElseThrow().getStatus()).isEqualTo(TicketStatus.WAITING);
    }
}
