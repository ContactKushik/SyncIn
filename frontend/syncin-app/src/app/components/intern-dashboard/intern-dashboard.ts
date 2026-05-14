import { Component, signal, OnDestroy, ElementRef, ViewChild, AfterViewInit } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { AttendanceApi } from '../../services/attendance-api';
import { Html5Qrcode } from 'html5-qrcode';

@Component({
  selector: 'app-intern-dashboard',
  standalone: true,
  imports: [FormsModule],
  templateUrl: './intern-dashboard.html',
})
export class InternDashboard implements OnDestroy {
  status = signal('NOT_MARKED');
  punchedIn = signal(false);
  todayDate = signal('');
  msg = signal('');
  err = signal('');

  // Manual token input
  qrToken = '';

  // Scanner
  scannerActive = signal(false);
  private html5Qrcode: Html5Qrcode | null = null;

  constructor(private api: AttendanceApi) {
    this.loadStatus();
  }

  loadStatus() {
    this.api.getTodayStatus().subscribe({
      next: (res: any) => {
        this.status.set(res.status);
        this.punchedIn.set(res.punchedIn);
        this.todayDate.set(res.date);
      }
    });
  }

  startScanner() {
    this.err.set('');
    this.msg.set('');
    this.scannerActive.set(true);

    setTimeout(() => {
      this.html5Qrcode = new Html5Qrcode('qr-reader');
      this.html5Qrcode.start(
        { facingMode: 'environment' },
        { fps: 10, qrbox: { width: 200, height: 200 } },
        (decodedText) => {
          this.qrToken = decodedText;
          this.stopScanner();
          this.punchIn();
        },
        () => {}
      ).catch((e: any) => {
        this.err.set('Camera access denied or not available.');
        this.scannerActive.set(false);
      });
    }, 100);
  }

  stopScanner() {
    if (this.html5Qrcode) {
      this.html5Qrcode.stop().then(() => {
        this.html5Qrcode?.clear();
        this.scannerActive.set(false);
      }).catch(() => {
        this.scannerActive.set(false);
      });
    }
  }

  punchIn() {
    this.msg.set('');
    this.err.set('');
    if (!this.qrToken.trim()) {
      this.err.set('Please scan QR or enter the token manually.');
      return;
    }
    this.api.punchIn(this.qrToken.trim()).subscribe({
      next: (res: any) => {
        this.msg.set(res.message);
        this.status.set(res.status);
        this.punchedIn.set(true);
      },
      error: (e: any) => {
        this.err.set(e?.error?.error || 'Punch-in failed.');
      }
    });
  }

  ngOnDestroy() {
    this.stopScanner();
  }
}
