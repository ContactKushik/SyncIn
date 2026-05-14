import { Routes } from '@angular/router';
import { AdminSudo } from './components/admin-sudo/admin-sudo';
import { UserLogin } from './components/user-login/user-login';

export const routes: Routes = [
  { path: 'admin', component: AdminSudo },
  { path: '', component: UserLogin }
];
