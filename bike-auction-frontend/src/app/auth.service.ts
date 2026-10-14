
import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable, tap } from 'rxjs';

export interface AuthResponse {
  token: string;
  tokenType: string;
  expiresIn: number;
  id: string;
  displayName: string;
  email: string;
  role: string;
}

export interface RegisterRequest {
  displayName: string;
  email: string;
  password: string;
}

export interface LoginRequest {
  email: string;
  password: string;
}

export interface AuthUser {
  id: string;
  displayName: string;
  email: string;
  role: string;
}

@Injectable({
  providedIn: 'root'
})
export class AuthService {
  private readonly authUrl = 'http://localhost:8085/api/auth';
  private readonly tokenKey = 'bike_auction_token';
  private readonly userKey = 'bike_auction_user';

  constructor(private readonly http: HttpClient) {}

  register(request: RegisterRequest): Observable<unknown> {
    return this.http.post(`${this.authUrl}/register`, request);
  }

  login(request: LoginRequest): Observable<AuthResponse> {
    return this.http.post<AuthResponse>(
      `${this.authUrl}/login`,
      request
    ).pipe(
      tap(response => {
        localStorage.setItem(this.tokenKey, response.token);

        const user: AuthUser = {
          id: response.id,
          displayName: response.displayName,
          email: response.email,
          role: response.role
        };

        localStorage.setItem(this.userKey, JSON.stringify(user));
      })
    );
  }

  getToken(): string | null {
    return localStorage.getItem(this.tokenKey);
  }

  getUser(): AuthUser | null {
    const savedUser = localStorage.getItem(this.userKey);

    if (!savedUser) {
      return null;
    }

    try {
      return JSON.parse(savedUser) as AuthUser;
    } catch {
      this.logout();
      return null;
    }
  }

  isLoggedIn(): boolean {
    return this.getToken() !== null;
  }

  logout(): void {
    localStorage.removeItem(this.tokenKey);
    localStorage.removeItem(this.userKey);
  }
}
