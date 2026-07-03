package com.flowpay.backend.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Setter;
import java.util.UUID;

@Entity
@Table(name = "attendant")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Attendant {
    
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id")
    private UUID id;

    @Column(name = "name")
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(name = "team")
    private Team team;
}
