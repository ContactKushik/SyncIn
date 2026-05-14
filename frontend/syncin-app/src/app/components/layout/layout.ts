import { Component, signal } from '@angular/core';
import { Router, RouterLink, RouterOutlet } from '@angular/router';
import { Auth } from '../../services/auth';

@Component({
  selector: 'app-layout',
  standalone: true,
  imports: [RouterOutlet, RouterLink],
  templateUrl: './layout.html',
})
export class Layout {
  role = signal('');
  sidebarOpen = signal(true);

  constructor(private auth: Auth, private router: Router) {
    this.role.set(this.auth.getUserRole() || '');
  }

  onLogout() {
    this.auth.logout();
    this.router.navigate(['/']);
  }

  toggleSidebar() {
    this.sidebarOpen.update(v => !v);
  }
}
