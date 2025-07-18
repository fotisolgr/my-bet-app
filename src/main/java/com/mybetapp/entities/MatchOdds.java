package com.mybetapp.entities;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.mybetapp.enums.Specifier;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Entity
@Table(name = "match_odds")
@Getter
@Setter
@NoArgsConstructor
@ToString(exclude = "match")
public class MatchOdds {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	// In MatchOdds.java
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "match_id", nullable = false)
	@JsonBackReference
	private Match match;

	@Enumerated(EnumType.STRING)
	private Specifier specifier;

	@Column(nullable = false)
	private Double odd;
}
