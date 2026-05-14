import { Component, signal } from '@angular/core';
import { QRCodeComponent } from 'angularx-qrcode';
import { AttendanceApi } from '../../services/attendance-api';

@Component({
  selector: 'app-cr-dashboard',
  standalone: true,
  imports: [QRCodeComponent],
  templateUrl: './cr-dashboard.html',
})
export class CrDashboard {
  token = signal('');
  batchCode = signal('');
  todayDate = signal('');
  msg = signal('');
  err = signal('');

  presentList = signal<any[]>([]);
  defaulterMsg = signal('');
  defaulterErr = signal('');

  constructor(private api: AttendanceApi) {
    this.loadToken();
    this.loadPresent();
  }

  loadToken() {
    this.api.getTodayToken().subscribe({
      next: (res: any) => {
        this.token.set(res.token || '');
        this.batchCode.set(res.batchCode || '');
        this.todayDate.set(res.date || '');
      }
    });
  }

  generateToken() {
    this.msg.set('');
    this.err.set('');
    this.api.generateToken().subscribe({
      next: (res: any) => {
        this.token.set(res.token);
        this.batchCode.set(res.batchCode);
        this.todayDate.set(res.date);
        this.msg.set(res.message || 'Token generated!');
      },
      error: (e: any) => this.err.set(e?.error?.error || 'Failed to generate token.')
    });
  }

  loadPresent() {
    this.api.getPresentToday().subscribe({
      next: (data: any) => this.presentList.set(data),
      error: () => this.presentList.set([])
    });
  }

  markDefaulter(attendanceId: number) {
    this.defaulterMsg.set('');
    this.defaulterErr.set('');
    this.api.markDefaulter(attendanceId).subscribe({
      next: (res: any) => {
        this.defaulterMsg.set(`${res.name} marked as defaulter.`);
        this.loadPresent();
      },
      error: (e: any) => {
        this.defaulterErr.set(e?.error?.error || 'Failed to mark defaulter.');
      }
    });
  }
}
