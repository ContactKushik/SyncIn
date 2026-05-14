import { Component, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { PocApi } from '../../services/poc-api';

@Component({
  selector: 'app-poc-dashboard',
  standalone: true,
  imports: [FormsModule],
  templateUrl: './poc-dashboard.html',
})
export class PocDashboard {
  // Cohorts
  cohorts = signal<any[]>([]);
  newCohort = { batchCode: '', trackName: '' };
  cohortMsg = signal('');
  cohortErr = signal('');

  // Selected cohort interns
  selectedCohort = signal<any>(null);
  interns = signal<any[]>([]);

  // Onboard modal
  showModal = signal(false);
  internForm = { empId: '', name: '', email: '', mobileNo: '', batchCode: '' };
  onboardMsg = signal('');
  onboardErr = signal('');
  tempPassword = signal('');

  // Promote
  promoteMsg = signal('');
  promoteErr = signal('');

  constructor(private api: PocApi) {
    this.loadCohorts();
  }

  loadCohorts() {
    this.api.getCohorts().subscribe({
      next: (data: any) => this.cohorts.set(data),
      error: () => this.cohortErr.set('Failed to load cohorts.')
    });
  }

  createCohort() {
    this.cohortMsg.set('');
    this.cohortErr.set('');
    if (!this.newCohort.batchCode.trim() || !this.newCohort.trackName.trim()) {
      this.cohortErr.set('Batch Code and Track Name are required.');
      return;
    }
    this.api.createCohort(this.newCohort.batchCode, this.newCohort.trackName).subscribe({
      next: (res: any) => {
        this.cohortMsg.set(`Cohort "${res.batchCode}" created!`);
        this.newCohort = { batchCode: '', trackName: '' };
        this.loadCohorts();
      },
      error: () => this.cohortErr.set('Failed to create cohort.')
    });
  }

  selectCohort(cohort: any) {
    this.selectedCohort.set(cohort);
    this.promoteMsg.set('');
    this.promoteErr.set('');
    this.api.getInterns(cohort.batchCode).subscribe({
      next: (data: any) => this.interns.set(data),
      error: () => this.interns.set([])
    });
  }

  openOnboardModal() {
    this.internForm = { empId: '', name: '', email: '', mobileNo: '', batchCode: this.selectedCohort()?.batchCode };
    this.onboardMsg.set('');
    this.onboardErr.set('');
    this.tempPassword.set('');
    this.showModal.set(true);
  }

  closeModal() {
    this.showModal.set(false);
  }

  onboardIntern() {
    this.onboardMsg.set('');
    this.onboardErr.set('');
    this.api.onboardIntern(this.internForm).subscribe({
      next: (res: any) => {
        this.onboardMsg.set(`Intern "${res.name}" onboarded!`);
        this.tempPassword.set(res.tempPassword);
        this.selectCohort(this.selectedCohort());
      },
      error: (err: any) => {
        this.onboardErr.set(err?.error?.error || 'Failed to onboard intern.');
      }
    });
  }

  promote(userId: number) {
    this.promoteMsg.set('');
    this.promoteErr.set('');
    this.api.promoteIntern(userId).subscribe({
      next: (res: any) => {
        this.promoteMsg.set(`${res.name} promoted to CR!`);
        this.selectCohort(this.selectedCohort());
      },
      error: (err: any) => {
        this.promoteErr.set(err?.error?.error || 'Failed to promote.');
      }
    });
  }

  demote(userId: number) {
    this.promoteMsg.set('');
    this.promoteErr.set('');
    this.api.demoteIntern(userId).subscribe({
      next: (res: any) => {
        this.promoteMsg.set(`${res.name} demoted to INTERN.`);
        this.selectCohort(this.selectedCohort());
      },
      error: (err: any) => {
        this.promoteErr.set(err?.error?.error || 'Failed to demote.');
        this.selectCohort(this.selectedCohort());
      }
    });
  }

  onRoleChange(intern: any, event: Event) {
    const select = event.target as HTMLSelectElement;
    const newRole = select.value;
    if (newRole === 'CR' && intern.role === 'INTERN') {
      const crCount = this.interns().filter(i => i.role === 'CR').length;
      if (crCount >= 2) {
        alert('Maximum 2 CRs allowed per cohort!');
        select.value = intern.role;
        return;
      }
      this.promote(intern.userId);
    } else if (newRole === 'INTERN' && intern.role === 'CR') {
      this.demote(intern.userId);
    }
  }
}


