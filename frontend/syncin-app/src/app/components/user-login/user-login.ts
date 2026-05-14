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
    }
  }

  onLogin() {
    this.error.set('');
    this.authService.login(this.empId, this.password).subscribe({
      next: (res: any) => {
        // TODO: navigate based on role (POC / INTERN)
        alert(`Login successful! Role: ${res.role}`);
      },
      error: () => {
        this.error.set('Invalid Employee ID or Password.');
      }
    });
  }
}



