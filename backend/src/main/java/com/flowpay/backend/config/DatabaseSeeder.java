package com.flowpay.backend.config;

import com.flowpay.backend.domain.Attendant;
import com.flowpay.backend.domain.Team;
import com.flowpay.backend.repository.AttendantRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
public class DatabaseSeeder implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(DatabaseSeeder.class);
    private final AttendantRepository attendantRepository;

    public DatabaseSeeder(AttendantRepository attendantRepository) {
        this.attendantRepository = attendantRepository;
    }

    @Override
    public void run(String... args) {
        if (attendantRepository.count() == 0) {
            log.info("Seeding initial attendants...");
            attendantRepository.save(Attendant.builder().name("Ana").team(Team.CARDS).build());
            attendantRepository.save(Attendant.builder().name("Beto").team(Team.LOANS).build());
            attendantRepository.save(Attendant.builder().name("Carlos").team(Team.OTHER).build());
            log.info("Seeding completed.");
        }
    }
}
