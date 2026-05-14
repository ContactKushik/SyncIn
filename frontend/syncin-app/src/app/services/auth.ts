import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { tap } from 'rxjs/operators';
import { Observable } from 'rxjs';
import { environment } from '../../environments/environment';

const BASE_URL = environment.apiUrl;
const TOKEN_KEY = 'jwt_token';
const ADMIN_TOKEN_KEY = 'admin_token';

@Injectable({ providedIn: 'root' })
export class Auth {
  constructor(private http: HttpClient) {}

  login(empId: string, password: string): Observable<any> {
    return this.http
      .post<any>(`${BASE_URL}/auth/login`, { empId, password })
      .pipe(tap(res => {
        localStorage.setItem(TOKEN_KEY, res.token);
        localStorage.setItem('user_role', res.role);
        localStorage.setItem('user_id', res.userId);
        localStorage.setItem('is_first_login', res.firstLogin);
      }));
  }

  adminLogin(username: string, password: string): Observable<{ token: string }> {
    return this.http
      .post<{ token: string }>(`${BASE_URL}/admin/login`, { username, password })
      .pipe(tap(res => localStorage.setItem(ADMIN_TOKEN_KEY, res.token)));
  }

  changePassword(newPassword: string): Observable<any> {
    return this.http.put(`${BASE_URL}/auth/change-password`, { newPassword });
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

  isFirstLogin(): boolean {
    return localStorage.getItem('is_first_login') === 'true';
  }

  getUserRole(): string | null {
    return localStorage.getItem('user_role');
  }

  getUserId(): string | null {
    return localStorage.getItem('user_id');
  }

  clearFirstLogin(): void {
    localStorage.setItem('is_first_login', 'false');
  }

  logout(): void {
    localStorage.removeItem(TOKEN_KEY);
    localStorage.removeItem('user_role');
    localStorage.removeItem('user_id');
    localStorage.removeItem('is_first_login');
  }

  adminLogout(): void {
    localStorage.removeItem(ADMIN_TOKEN_KEY);
  }
}
