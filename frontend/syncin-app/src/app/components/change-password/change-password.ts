import { Component, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { Router } from '@angular/router';
import { Auth } from '../../services/auth';

@Component({
  selector: 'app-change-password',
  standalone: true,
  imports: [FormsModule],
  templateUrl: './change-password.html',
})
export class ChangePassword {
  newPassword = '';
  confirmPassword = '';
  error = signal('');
  success = signal('');

  constructor(private auth: Auth, private router: Router) {}

  onSubmit() {
    this.error.set('');
    this.success.set('');

    if (!this.newPassword || this.newPassword.length < 6) {
      this.error.set('Password must be at least 6 characters.');
      return;
    }
    if (this.newPassword !== this.confirmPassword) {
      this.error.set('Passwords do not match.');
      return;
    }

    this.auth.changePassword(this.newPassword).subscribe({
      next: () => {
        this.auth.clearFirstLogin();
        this.success.set('Password changed! Redirecting...');
        setTimeout(() => {
          const role = this.auth.getUserRole();
          if (role === 'POC') this.router.navigate(['/dashboard']);
          else if (role === 'CR') this.router.navigate(['/dashboard/cr']);
          else if (role === 'INTERN') this.router.navigate(['/dashboard/intern']);
          else this.router.navigate(['/']);
        }, 1000);
      },
      error: () => {
        this.error.set('Failed to change password. Try again.');
      }
    });
  }
}
