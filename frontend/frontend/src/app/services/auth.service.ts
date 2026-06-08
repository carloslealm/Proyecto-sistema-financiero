import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { BehaviorSubject, Observable, tap } from 'rxjs';
import { Router } from '@angular/router';
import { environment } from '../../environments/environment';
import { AuthResponse, LoginRequest, ApiResponse, RegistroRequest } from '../models/auth.model';
 
@Injectable({
  providedIn: 'root'
})
export class AuthService {
 
  private readonly API = 'http://localhost:8080/api/v1/auth';
  private readonly TOKEN_KEY = 'lm_token';
  private readonly USER_KEY  = 'lm_user';
 
  // BehaviorSubject mantiene el estado del usuario actual.
  // Cualquier componente puede suscribirse para saber
  // si hay un usuario autenticado y quién es.
  private currentUserSubject = new BehaviorSubject<AuthResponse | null>(
    this.getUserFromStorage()
  );
 
  // Observable público — los componentes se suscriben a este,
  // no al Subject directamente (encapsulamiento).
  currentUser$ = this.currentUserSubject.asObservable();
 
  constructor(
    private http: HttpClient,
    private router: Router
  ) {}
 
  login(request: LoginRequest): Observable<ApiResponse<AuthResponse>> {
    return this.http.post<ApiResponse<AuthResponse>>(
      `${this.API}/login`, request
    ).pipe(
      // tap ejecuta un efecto secundario sin modificar el stream.
      // Aquí guardamos el token y el usuario en localStorage.
      tap(response => {
        if (response.success) {
          this.guardarSesion(response.data);
        }
      })
    );
  }
 
  registro(request: RegistroRequest): Observable<ApiResponse<AuthResponse>> {
    return this.http.post<ApiResponse<AuthResponse>>(
      `${this.API}/registro`, request
    ).pipe(
      tap(response => {
        if (response.success) {
          this.guardarSesion(response.data);
        }
      })
    );
  }
 
  logout(): void {
    localStorage.removeItem(this.TOKEN_KEY);
    localStorage.removeItem(this.USER_KEY);
    this.currentUserSubject.next(null);
    this.router.navigate(['/auth/login']);
  }
 
  getToken(): string | null {
    return localStorage.getItem(this.TOKEN_KEY);
  }
 
  isAuthenticated(): boolean {
    const token = this.getToken();
    if (!token) return false;
 
    // Verificar si el token ha expirado decodificando el payload
    try {
      const payload = JSON.parse(atob(token.split('.')[1]));
      return payload.exp * 1000 > Date.now();
    } catch {
      return false;
    }
  }
 
  getCurrentUser(): AuthResponse | null {
    return this.currentUserSubject.value;
  }
 
  hasRole(role: string): boolean {
    const user = this.getCurrentUser();
    return user?.roles.includes(`ROLE_${role}`) ?? false;
  }
 
  isAdmin(): boolean {
    return this.hasRole('ADMIN');
  }
 
  private guardarSesion(authData: AuthResponse): void {
    localStorage.setItem(this.TOKEN_KEY, authData.token);
    localStorage.setItem(this.USER_KEY, JSON.stringify(authData));
    this.currentUserSubject.next(authData);
  }
 
  private getUserFromStorage(): AuthResponse | null {
    const user = localStorage.getItem(this.USER_KEY);
    return user ? JSON.parse(user) : null;
  }
}
 