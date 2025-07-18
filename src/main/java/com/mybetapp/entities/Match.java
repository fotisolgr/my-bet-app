package com.mybetapp.entities;

import com.fasterxml.jackson.annotation.JsonManagedReference;
import com.mybetapp.enums.Sport;
import jakarta.persistence.*;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Entity
@Table(name = "matches") // "match" is a reserved keyword in some DBs
@Getter
@Setter
@NoArgsConstructor
@ToString
public class Match {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(nullable = false)
	private String owner;

	private String description;

	@Column(nullable = false)
	private LocalDate matchDate;

	@Column(nullable = false)
	private LocalTime matchTime;

	@Column(nullable = false)
	private String teamA;

	@Column(nullable = false)
	private String teamB;

	@Enumerated(EnumType.STRING)
	private Sport sport;

	@OneToMany(mappedBy = "match", cascade = CascadeType.ALL, orphanRemoval = true)
	@JsonManagedReference
	private List<MatchOdds> odds = new ArrayList<>();
}
