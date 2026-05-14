import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../environments/environment';

const BASE_URL = environment.apiUrl;

@Injectable({ providedIn: 'root' })
export class AttendanceApi {
  constructor(private http: HttpClient) {}

  // CR endpoints
  generateToken(): Observable<any> {
    return this.http.post(`${BASE_URL}/cr/generate-token`, {});
  }

  getTodayToken(): Observable<any> {
    return this.http.get(`${BASE_URL}/cr/today-token`);
  }

  getPresentToday(): Observable<any> {
    return this.http.get(`${BASE_URL}/cr/present-today`);
  }

  markDefaulter(attendanceId: number): Observable<any> {
    return this.http.put(`${BASE_URL}/cr/mark-defaulter/${attendanceId}`, {});
  }

  // Intern endpoints
  punchIn(token: string): Observable<any> {
    return this.http.post(`${BASE_URL}/intern/punch-in`, { token });
  }

  getTodayStatus(): Observable<any> {
    return this.http.get(`${BASE_URL}/intern/today-status`);
  }
}
