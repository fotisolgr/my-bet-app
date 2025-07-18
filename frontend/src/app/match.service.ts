// src/app/match.service.ts
import { Injectable } from '@angular/core';
import {HttpClient, HttpParams} from '@angular/common/http';
import { Observable } from 'rxjs';

export enum Sport {
  FOOTBALL = 'FOOTBALL',
  BASKETBALL = 'BASKETBALL'
}

export enum Specifier {
  WIN = 'WIN',
  DRAW = 'DRAW',
  LOSE = 'LOSE'
}

export interface MatchOdds {
  specifier: Specifier;
  odd: number;
}

export interface Odd {
  specifier: string;
  odd: number;
}

export interface Match {
  matchId: string;
  description: string;
  matchDate: string;     // ISO date string (e.g., '2025-07-17')
  matchTime: string;     // e.g., '14:00'
  teamA: string;
  teamB: string;
  sport: Sport;
  odds: Odd[];
  matchOwner: string;
}

export interface MatchDTO {
  matchId: string;
  matchOwner: string;
  description: string;
  matchDate: string;   // ISO string format (e.g., "2025-07-17")
  matchTime: string;   // ISO string format (e.g., "14:30:00")
  teamA: string;
  teamB: string;
  sport: Sport;
  odds: MatchOdds[];
}

export interface SaveMatchRequest {
  matchId: string;
  description?: string; // optional since no @NotBlank
  matchDate: string;    // format: 'YYYY-MM-DD'
  matchTime: string;    // format: 'HH:mm:ss'
  teamA: string;
  teamB: string;
  sport: Sport;
  odds: MatchOdds[];
}

@Injectable({
  providedIn: 'root',
})
export class MatchService {
  private getPaginatedMatchesApiUrl = 'http://localhost:8888/bet/matches';
  private saveMatchApiUrl = 'http://localhost:8888/bet/saveMatch';
  private updateMatchApiUrl = 'http://localhost:8888/bet/updateMatch';
  private deleteMatchApiUrl = 'http://localhost:8888/bet/matches';

  constructor(private http: HttpClient) {}

  getPaginatedMatches(
    page: number,
    size: number,
    sortBy: string = 'dateTime',
    direction: string = 'desc'
  ): Observable<any> {
    const params = new HttpParams()
      .set('page', page)
      .set('size', size)
      .set('sortBy', sortBy)
      .set('direction', direction);

    return this.http.get<any>(`${this.getPaginatedMatchesApiUrl}`, { params: params });
  }

  saveMatch(match: SaveMatchRequest, token: string) {
    return this.http.post<MatchDTO>(this.saveMatchApiUrl, match, {
      headers: {
        Authorization: `Bearer ${token}`,
        'Content-Type': 'application/json',
      },
    });
  }

  updateMatch(matchId: string, match: SaveMatchRequest, token: string) {
    return this.http.put<MatchDTO>(`${this.updateMatchApiUrl}/${matchId}`, match, {
      headers: {
        Authorization: `Bearer ${token}`,
        'Content-Type': 'application/json',
      },
    });
  }

  deleteMatch(matchId: string, token: string) {
    return this.http.delete(`${this.deleteMatchApiUrl}/${matchId}`, {
      headers: {
        Authorization: `Bearer ${token}`,
      }
    });
  }

}
