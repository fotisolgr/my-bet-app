package com.mybetapp.repositories;

import com.mybetapp.entities.Match;
import com.mybetapp.enums.Sport;
import java.time.LocalDate;
import java.time.LocalTime;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

@Repository
public interface MatchRepository extends JpaRepository<Match, Long>, JpaSpecificationExecutor<Match> {

	boolean existsByTeamAAndTeamBAndMatchDateAndMatchTimeAndSport(String teamA, String teamB, LocalDate matchDate,
			LocalTime matchTime, Sport sport);
}
