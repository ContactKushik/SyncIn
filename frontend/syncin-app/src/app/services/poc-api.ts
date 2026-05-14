import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';

const BASE_URL = 'http://localhost:8081/poc';

@Injectable({ providedIn: 'root' })
export class PocApi {
  constructor(private http: HttpClient) {}

  getCohorts(): Observable<any> {
    return this.http.get(`${BASE_URL}/cohorts`);
  }

  createCohort(batchCode: string, trackName: string): Observable<any> {
    return this.http.post(`${BASE_URL}/cohorts`, { batchCode, trackName });
  }

  getInterns(batchCode: string): Observable<any> {
    return this.http.get(`${BASE_URL}/cohorts/${batchCode}/interns`);
  }

  onboardIntern(data: any): Observable<any> {
    return this.http.post(`${BASE_URL}/interns`, data);
  }

  promoteIntern(userId: number): Observable<any> {
    return this.http.put(`${BASE_URL}/interns/${userId}/promote`, {});
  }

  demoteIntern(userId: number): Observable<any> {
    return this.http.put(`${BASE_URL}/interns/${userId}/demote`, {});
  }
}
