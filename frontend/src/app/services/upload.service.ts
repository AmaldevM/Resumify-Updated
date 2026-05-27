import { HttpClient, HttpParams } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';

@Injectable({
  providedIn: 'root'
})
export class UploadService {

  private baseUrl = typeof window !== 'undefined' && window.location.hostname !== 'localhost'
    ? 'https://resumify-ai-resumeparsing.onrender.com'
    : 'http://localhost:8080';

  constructor(private http: HttpClient) { }

  // 📤 Upload a resume
  uploadResume(userId: number, formData: FormData): Observable<any> {
    return this.http.post(`${this.baseUrl}/resumes/upload/${userId}`, formData, {
      responseType: 'text'
    });
  }

  // 📥 Get resumes by user
  getUserResumes(userId: number): Observable<any[]> {
    return this.http.get<any[]>(`${this.baseUrl}/resumes/resumesByUserId/${userId}`);
  }


  // 🧠 Filter by Skills only
  getResumesBySkills(userId: number, skills: string[]): Observable<any[]> {
    const params = new HttpParams()
      .set('skills', skills.join(','))
      .set('userId', userId.toString());

    return this.http.get<any[]>(`${this.baseUrl}/resumes/by-skills`, { params });
  }

  // 🎯 Filter by Skills + Experience
  getResumesBySkillsAndExperience(userId: number, skills: string[], startExp: number, endExp: number): Observable<any[]> {
    const params = new HttpParams()
      .set('userId', userId.toString())
      .set('skills', skills.join(','))
      .set('start', startExp.toString())
      .set('end', endExp.toString());

    return this.http.get<any[]>(`${this.baseUrl}/resumes/by-skills-experience-range`, { params });
  }

  deleteAllResumes(userId: number): Observable<any> {
    return this.http.delete(`${this.baseUrl}/resumes/delete/user/${userId}`, { responseType: 'text' });
  }
  deleteSingleResume(userId: number, resumeId: number): Observable<any> {
    return this.http.delete(`${this.baseUrl}/resumes/delete/user/${userId}/resume/${resumeId}`, { responseType: 'text' });
  }
  getResumeImage(resumeId: number): Observable<Blob> {
    return this.http.get(`${this.baseUrl}/resumes/image/${resumeId}`, {
      responseType: 'blob', // Important to get binary data as Blob
    });
  }
  getResumePreview(userId: number, resumeId: number): Observable<any> {
    return this.http.get<any>(`${this.baseUrl}/resumes/preview/${userId}/${resumeId}`);
  }

  // 📈 Check ATS Score
  checkAtsScore(resumeId: number, jobDescription: string): Observable<any> {
    return this.http.post<any>(`${this.baseUrl}/resumes/ats-score`, { resumeId, jobDescription });
  }

  // 🧠 Enhance Resume via AI
  enhanceResume(resumeId: number, targetRole: string, jobDescription: string): Observable<any> {
    return this.http.post<any>(`${this.baseUrl}/resumes/enhance`, { resumeId, targetRole, jobDescription });
  }
}

