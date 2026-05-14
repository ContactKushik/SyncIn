import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { tap } from 'rxjs/operators';
import { Observable } from 'rxjs';

const BASE_URL = 'http://localhost:8081';
const TOKEN_KEY = 'jwt_token';
const ADMIN_TOKEN_KEY = 'admin_token';

@Injectable({ providedIn: 'root' })
export class Auth {
  constructor(private http: HttpClient) {}

  login(empId: string, password: string): Observable<{ token: string }> {
    return this.http
      .post<{ token: string }>(`${BASE_URL}/auth/login`, { empId, password })
      .pipe(tap(res => localStorage.setItem(TOKEN_KEY, res.token)));
  }

  adminLogin(username: string, password: string): Observable<{ token: string }> {
    return this.http
      .post<{ token: string }>(`${BASE_URL}/admin/login`, { username, password })
      .pipe(tap(res => localStorage.setItem(ADMIN_TOKEN_KEY, res.token)));
  }

  getToken(): string | null {
    return localStorage.getItem(TOKEN_KEY);
  }

  getAdminToken(): string | null {
    return localStorage.getItem(ADMIN_TOKEN_KEY);
  }

  isLoggedIn(): boolean {
    return !!this.getToken();
  }

  isAdminLoggedIn(): boolean {
    return !!this.getAdminToken();
  }

  getRole(): string | null {
    const token = this.getToken();
    if (!token) return null;
    try {
      const payload = JSON.parse(atob(token.split('.')[1]));
      return payload.role || null;
    } catch {
      return null;
    }
  }

  logout(): void {
    localStorage.removeItem(TOKEN_KEY);
  }

  adminLogout(): void {
    localStorage.removeItem(ADMIN_TOKEN_KEY);
  }
}
