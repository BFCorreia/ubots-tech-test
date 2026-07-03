package com.flowpay.backend.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Setter;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "ticket")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Ticket {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id")
    private UUID id;

    @Column(name = "client_name")
    private String clientName;
    
    @Column(name = "subject")
    private String subject;

    @Column(name = "description", length = 1000)
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(name = "team")
    private Team team;

    @Enumerated(EnumType.STRING)
    @Column(name = "status")
    @Builder.Default
    private TicketStatus status = TicketStatus.WAITING;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "attendant_id")
    private Attendant attendant;

    @Column(name = "created_at")
    @Builder.Default
    private Instant createdAt = Instant.now();
    
    @Column(name = "started_at")
    private Instant startedAt;
    
    @Column(name = "finished_at")
    private Instant finishedAt;
}
