import { Component, OnInit, OnDestroy } from '@angular/core';
import { ActivatedRoute } from '@angular/router';
import { UploadService } from '../services/upload.service';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';

@Component({
  selector: 'app-profileview',
  standalone: true,
  templateUrl: './profileview.component.html',
  styleUrls: ['./profileview.component.scss'],
  imports: [CommonModule, FormsModule]
})
export class ProfileviewComponent implements OnInit, OnDestroy {

  resume: any = null;
  resumeId!: number;
  userId!: number;
  imageUrl: string = '';

  // Tab State
  activeTab: 'details' | 'ats' | 'enhance' = 'details';

  // ATS Checker State
  jobDescription: string = '';
  isAtsScanning: boolean = false;
  atsResult: any = null;

  // AI Enhancer State
  targetRole: string = 'Software Engineer';
  customInstructions: string = '';
  isEnhancing: boolean = false;
  enhancementResult: any = null;

  roleOptions: string[] = [
    'Software Engineer',
    'Frontend Developer',
    'Backend Developer',
    'Full Stack Developer',
    'UI/UX Designer',
    'Java Developer',
    'Data Scientist',
    'DevOps Engineer'
  ];

  constructor(
    private route: ActivatedRoute,
    private uploadService: UploadService
  ) {}

  ngOnInit(): void {
    this.route.paramMap.subscribe(params => {
      const idParam = params.get('id');
      const userIdStr = localStorage.getItem('userId');

      if (!idParam || !userIdStr) return;

      this.resumeId = parseInt(idParam, 10);
      this.userId = parseInt(userIdStr, 10);

      this.loadResumeData();
      this.loadResumeImage();
    });
  }

  ngOnDestroy(): void {
    if (this.imageUrl) {
      URL.revokeObjectURL(this.imageUrl);
    }
  }

  loadResumeData(): void {
    this.uploadService.getResumePreview(this.userId, this.resumeId).subscribe({
      next: (res: any) => {
        this.resume = res;
        if (this.resume?.skills && typeof this.resume.skills === 'string') {
          this.resume.skills = this.resume.skills.split(',').map((s: string) => s.trim());
        }
      },
      error: (err: any) => {
        console.error('Failed to load resume:', err);
      }
    });
  }

  loadResumeImage(): void {
    this.uploadService.getResumeImage(this.resumeId).subscribe({
      next: (blob) => {
        const url = URL.createObjectURL(blob);
        this.imageUrl = url;
      },
      error: () => {
        this.imageUrl = '';
      }
    });
  }

  switchTab(tab: 'details' | 'ats' | 'enhance'): void {
    this.activeTab = tab;
  }

  runAtsScan(): void {
    if (!this.jobDescription.trim()) {
      alert('Please enter a job description to scan.');
      return;
    }

    this.isAtsScanning = true;
    this.atsResult = null;

    this.uploadService.checkAtsScore(this.resumeId, this.jobDescription).subscribe({
      next: (res: any) => {
        this.atsResult = res;
        this.isAtsScanning = false;
      },
      error: (err: any) => {
        console.error('ATS scan failed:', err);
        alert('Failed to scan resume against job description.');
        this.isAtsScanning = false;
      }
    });
  }

  runEnhance(): void {
    this.isEnhancing = true;
    this.enhancementResult = null;

    // Build instruction combining targetRole and optional job description/custom notes
    const combinedInstructions = this.customInstructions.trim() 
      ? `Job description / Notes: ${this.customInstructions}` 
      : '';

    this.uploadService.enhanceResume(this.resumeId, this.targetRole, combinedInstructions).subscribe({
      next: (res: any) => {
        this.enhancementResult = res;
        this.isEnhancing = false;
      },
      error: (err: any) => {
        console.error('Enhancement failed:', err);
        alert('Failed to enhance resume.');
        this.isEnhancing = false;
      }
    });
  }
}

