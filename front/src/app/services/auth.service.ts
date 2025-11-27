import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { environment } from '../../environments/environment';

@Injectable({ providedIn: 'root' })
export class AuthService {
  private tokenKey = 'auth_token';
  private userKey = 'auth_user';
  constructor(private http: HttpClient) {}

  login(email: string, password: string) {
    return this.http.post<any>(`${environment.authServiceUrl}/auth/login`, { email, password });
  }

  storeToken(token: string){ localStorage.setItem(this.tokenKey, token); }
  getToken(){ return localStorage.getItem(this.tokenKey); }
  logout(){ localStorage.removeItem(this.tokenKey); localStorage.removeItem(this.userKey); }
  isAuthenticated(){ return !!this.getToken(); }

  storeUser(user: any){ localStorage.setItem(this.userKey, JSON.stringify(user)); }
  getUser(){ const s = localStorage.getItem(this.userKey); return s ? JSON.parse(s) : null; }

  validateToken(){
    const t = this.getToken();
    if(!t) return this.http.get<any>(`${environment.authServiceUrl}/auth/validate`, { headers: { Authorization: 'Bearer ' + t } });
    return this.http.get<any>(`${environment.authServiceUrl}/auth/validate`, { headers: { Authorization: 'Bearer ' + t } });
  }
}
