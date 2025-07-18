import {Component, OnInit} from '@angular/core';
import {Match, MatchDTO, MatchService, SaveMatchRequest, Specifier, Sport} from './match.service';
import {NgForOf, NgIf} from "@angular/common";
import {FormsModule} from "@angular/forms";
import {KeycloakService} from "./keycloak.service";

@Component({
  selector: 'app-root',
  templateUrl: './app.component.html',
  styleUrls: ['./app.component.css'],
  standalone: true,
  imports: [
    NgForOf,
    NgIf,
    FormsModule
  ]
})
export class AppComponent implements OnInit {
  errorMessage = '';
  saveMatchErrorMessage = '';
  getMatchesErrorMessage = '';
  isLoggedIn = false;
  loggedInUsername: string | undefined;
  showAddMatchModal = false;
  successMessage: string = '';

  // For form binding
  match: SaveMatchRequest = {
    matchId: '',
    description: '',
    matchDate: '',
    matchTime: '',
    teamA: '',
    teamB: '',
    sport: Sport.BASKETBALL,  // or Sport.FOOTBALL as default
    odds: []
  };
  sports = Object.values(Sport);  // <-- add this line
  specifiers: Specifier[] = Object.values(Specifier);
  matchToUpdate: Match | null = null; // will hold the selected match to update
  isUpdatePopupOpen: boolean = false;

  constructor(
    private matchService: MatchService,
    private keycloakService: KeycloakService
  ) {}

  async ngOnInit(): Promise<void> {
    console.log('ngOnInit running');
    this.isLoggedIn = await this.keycloakService.init();
    if (this.isLoggedIn) {
      this.loggedInUsername = this.keycloakService.getUsername();
    }

    this.loadPaginatedMatches();
  }

  async deleteMatch(matchId: string): Promise<void> {
    if (!this.isLoggedIn) {
      this.errorMessage = 'You must be logged in to delete a match.';
      return;
    }

    const confirmed = confirm('Are you sure you want to delete this match?');
    if (!confirmed) {
      return;
    }

    try {
      const token = await this.keycloakService.getToken();

      this.matchService.deleteMatch(matchId, token).subscribe({
        next: () => {
          this.successMessage = 'Match deleted successfully.';
          this.errorMessage = '';
          // Reload matches or update the list locally
          this.loadPaginatedMatches();
        },
        error: (err) => {
          this.errorMessage = err.error?.error
          this.successMessage = '';
        }
      });
    } catch (err) {
      this.errorMessage = 'Could not get authentication token.';
      this.successMessage = '';
    }
  }



  onLogin(): void {
    this.keycloakService.login().catch(() => {
      console.error();
    });
  }

  onRegister(): void {
    this.keycloakService.register().catch(() => {
      console.error();
    });
  }

  onLogout(): void {
    this.keycloakService.logout().catch(() => {
      console.error();
    });
  }

  async saveMatch(): Promise<void> {
    if (!this.isLoggedIn) {
      this.saveMatchErrorMessage = "You must be logged in to save a match.";
      return;
    }

    const matchPayload: SaveMatchRequest = {
      matchId: this.match.matchId,
      description: this.match.description,
      matchDate: this.match.matchDate,
      matchTime: this.match.matchTime,
      teamA: this.match.teamA,
      teamB: this.match.teamB,
      sport: this.match.sport,
      odds: this.match.odds,
    };

    try {
      const token = await this.keycloakService.getToken();
      this.matchService.saveMatch(matchPayload, token).subscribe({
        next: () => {
          this.successMessage = `Match between ${matchPayload.teamA} and ${matchPayload.teamB} saved successfully!`;
          this.saveMatchErrorMessage = '';

          // Reset match object
          this.match = {
            matchId: '',
            description: '',
            matchDate: '',
            matchTime: '',
            teamA: '',
            teamB: '',
            sport: this.sports[0],
            odds: []
          };

          // Close the modal
          this.showAddMatchModal = false;

          // Optionally reload match list
          this.loadPaginatedMatches();
        },
        error: (err) => {
          this.saveMatchErrorMessage = err.error?.error;
          this.successMessage = '';
        },
      });
    } catch (err) {
      this.saveMatchErrorMessage = "Could not get authentication token.";
      this.successMessage = '';
      console.error(err);
    }
  }

  openUpdatePopup(match: Match) {
    // Clone the match object so you don't directly mutate the list item
    this.matchToUpdate = JSON.parse(JSON.stringify(match));
    this.isUpdatePopupOpen = true;
  }

  async updateMatch(): Promise<void> {
    if (!this.matchToUpdate) {
      return;
    }

    this.saveMatchErrorMessage = "";
    // Map odds to expected format (MatchOdds[])
    const mappedOdds = this.matchToUpdate.odds.map(o => ({
      specifier: o.specifier as Specifier, // cast if necessary
      odd: o.odd
    }));

    const matchPayload: SaveMatchRequest = {
      matchId: this.matchToUpdate.matchId,
      description: this.matchToUpdate.description,
      matchDate: this.matchToUpdate.matchDate,
      matchTime: this.matchToUpdate.matchTime?.slice(0, 5), // "17:13:00" → "17:13"
      teamA: this.matchToUpdate.teamA,
      teamB: this.matchToUpdate.teamB,
      sport: this.matchToUpdate.sport,
      odds: mappedOdds,
    };

    try {
      const token = await this.keycloakService.getToken();
      this.matchService.updateMatch(matchPayload.matchId, matchPayload, token).subscribe({
        next: () => {
          this.isUpdatePopupOpen = false;
          this.loadPaginatedMatches(); // reload matches to get fresh data from backend
        },
        error: (err) => {
          this.saveMatchErrorMessage = err.error?.error;
        }
      });
    } catch (err) {
      console.error('Failed to get token', err);
    }
  }

  closeUpdatePopup() {
    this.saveMatchErrorMessage = '';
    this.isUpdatePopupOpen = false;
    this.matchToUpdate = null;
  }

  getOddValue(spec: Specifier): number | null {
    const found = this.match.odds.find(o => o.specifier === spec);
    return found?.odd ?? null;
  }

  setOddValue(spec: Specifier, value: number): void {
    let existing = this.match.odds.find(o => o.specifier === spec);
    if (existing) {
      existing.odd = value;
    } else {
      this.match.odds.push({ specifier: spec, odd: value });
    }
  }

  paginatedMatches: MatchDTO[] = [];
  currentPage = 0;
  pageSize = 5;
  totalPages = 0;
  // New sorting state:
  sortBy: string = 'matchDate';    // default sort field
  sortDirection: string = 'desc';  // default sort direction

  loadPaginatedMatches() {
    this.matchService.getPaginatedMatches(
      this.currentPage,
      this.pageSize,
      this.sortBy,
      this.sortDirection
    ).subscribe({
      next: (response) => {
        this.getMatchesErrorMessage = "";
        this.paginatedMatches = response.content;
        this.totalPages = response.totalPages;
      },
      error: (err) => {
        if (err.status === 500) {
          this.getMatchesErrorMessage = err.error?.error
        } else {
          this.getMatchesErrorMessage = err.error?.error
        }
      }
    });
  }

  goToPage(page: number) {
    if (page >= 0 && page < this.totalPages) {
      this.currentPage = page;
      this.loadPaginatedMatches();
    }
  }

  setSorting(sortField: string) {
    if (this.sortBy === sortField) {
      // toggle direction if the same field clicked
      this.sortDirection = this.sortDirection === 'asc' ? 'desc' : 'asc';
    } else {
      this.sortBy = sortField;
      this.sortDirection = 'asc'; // reset to asc on new field
    }
    this.currentPage = 0; // reset to first page on sorting change
    this.loadPaginatedMatches();
  }

  closeAddMatchModal(): void {
    this.showAddMatchModal = false;
    this.saveMatchErrorMessage = '';
    this.successMessage = '';
    this.match = {
      matchId: '',
      description: '',
      matchDate: '',
      matchTime: '',
      teamA: '',
      teamB: '',
      sport: this.sports[0],
      odds: []
    };
  }


}
