import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable, tap, BehaviorSubject } from 'rxjs';

@Injectable({
  providedIn: 'root'
})
export class AuthService {
  private apiUrl = typeof window !== 'undefined' && window.location.hostname !== 'localhost'
    ? 'https://resumify-backend.onrender.com/api/auth'
    : 'http://localhost:8080/api/auth';

  // 🟢 BehaviorSubject to track login status
  private isLoggedInSubject = new BehaviorSubject<boolean>(this.hasToken());
  public isLoggedIn$ = this.isLoggedInSubject.asObservable();

  constructor(private http: HttpClient) { }

  // ✅ Register user
  register(userData: any): Observable<any> {
    return this.http.post(`${this.apiUrl}/register`, userData);
  }

  // ✅ Login user and store token
  login(credentials: any): Observable<any> {
    return this.http.post(`${this.apiUrl}/login`, credentials).pipe(
      tap((res: any) => {
        if (typeof window !== 'undefined') {
          localStorage.setItem('userId', res.id);
          localStorage.setItem('token', res.token);
          localStorage.setItem('username', res.username);
          this.isLoggedInSubject.next(true);
        }
      })
    );
  }

  // ✅ Logout user and clear token
  logout(): void {
    if (typeof window !== 'undefined') {
      localStorage.removeItem('userId');
      localStorage.removeItem('token');
      localStorage.removeItem('username');
      this.isLoggedInSubject.next(false);
    }
  }

  // ✅ Helper to check if user is logged in
  isLoggedIn(): boolean {
    return this.hasToken();
  }

  // ✅ Helper to get user ID
  getUserId(): string | null {
    return typeof window !== 'undefined' ? localStorage.getItem('userId') : null;
  }

  // ✅ Helper to get user role
  getRole(): string {
    return localStorage.getItem('userRole') || '';
  }

  // ✅ Private method to check token existence
  private hasToken(): boolean {
    return typeof window !== 'undefined' && !!localStorage.getItem('token');
  }

  // ✅ Send reset code to email
 sendResetCode(email: string): Observable<any> {
    return this.http.post(`${this.apiUrl}/setEmail`, { email }, { responseType: 'text' });
  }

  verifyResetCode(email: string,validationCode: string): Observable<any> {
    return this.http.post(`${this.apiUrl}/setCode`, {email,validationCode, },{ responseType: 'text' });
  }


  // ✅ Reset password
  resetPassword(email:string,password: string): Observable<any> {
    return this.http.post(`${this.apiUrl}/newpassword`, {email,password },{ responseType: 'text' });
  }
}