import { Component, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { Router } from '@angular/router';
import { Auth } from '../../services/auth';

@Component({
  selector: 'app-user-login',
  standalone: true,
  imports: [FormsModule],
  templateUrl: './user-login.html',
})
export class UserLogin {
  empId = '';
  password = '';
  error = signal('');

  constructor(private authService: Auth, private router: Router) {
    if (this.authService.isAdminLoggedIn()) {
      this.router.navigate(['/admin']);
      return;
    }
    if (this.authService.isLoggedIn()) {
      this.redirectByRole();
    }
  }

  onLogin() {
    this.error.set('');
    this.authService.login(this.empId, this.password).subscribe({
      next: (res: any) => {
        if (res.firstLogin) {
          this.router.navigate(['/change-password']);
        } else {
          this.redirectByRole();
        }
      },
      error: () => {
        this.error.set('Invalid Employee ID or Password.');
      }
    });
  }

  private redirectByRole() {
    const role = this.authService.getUserRole();
    if (role === 'POC') this.router.navigate(['/dashboard']);
    else if (role === 'CR') this.router.navigate(['/dashboard/cr']);
    else if (role === 'INTERN') this.router.navigate(['/dashboard/intern']);
    else this.router.navigate(['/']);
  }
}

