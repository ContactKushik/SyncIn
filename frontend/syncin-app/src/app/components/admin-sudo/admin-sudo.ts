import { Component, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { HttpClient } from '@angular/common/http';
import { Auth } from '../../services/auth';
import { environment } from '../../../environments/environment';

@Component({
  selector: 'app-admin-sudo',
  standalone: true,
  imports: [FormsModule],
  templateUrl: './admin-sudo.html',
  styles: ``,
})
export class AdminSudo {
  // Login
  loginUsername = '';
  loginPassword = '';
  loginError = signal('');
  isLoggedIn = signal(false);

  // Create POC
  poc = { empId: '', name: '', email: '', mobileNo: '', passwordHash: '' };
  successMsg = signal('');
  errorMsg = signal('');

  constructor(private authService: Auth, private http: HttpClient) {
    this.isLoggedIn.set(this.authService.isLoggedIn());
  }

  onLogin(): void {
    this.loginError.set('');
    this.authService.adminLogin(this.loginUsername, this.loginPassword).subscribe({
      next: () => { this.isLoggedIn.set(true); },
      error: () => { this.loginError.set('Invalid credentials. Please try again.'); }
    });
  }

  onCreatePoc(): void {
    this.successMsg.set('');
    this.errorMsg.set('');
    this.http.post(`${environment.apiUrl}/admin/create-poc`, this.poc).subscribe({
      next: (res: any) => {
        this.successMsg.set(`POC created: ${res.name} (${res.empId})`);
        this.poc = { empId: '', name: '', email: '', mobileNo: '', passwordHash: '' };
      },
      error: (err: any) => {
        this.errorMsg.set(err?.error?.error || 'Failed to create POC.');
      }
    });
  }

  onLogout(): void {
    this.authService.logout();
    this.isLoggedIn.set(false);
  }
}
