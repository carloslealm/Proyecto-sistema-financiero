// El interceptor agrega automáticamente el token JWT a CADA
// petición HTTP que salga de la app. El componente no necesita
// saber nada de tokens — el interceptor lo hace solo.
// ═══════════════════════════════════════════════════════════════
import { Injectable } from '@angular/core';
import {
  HttpRequest, HttpHandler, HttpEvent,
  HttpInterceptor, HttpErrorResponse
} from '@angular/common/http';
import { Observable, throwError } from 'rxjs';
import { catchError } from 'rxjs/operators';
import { AuthService } from '../services/auth.service';
import { Router } from '@angular/router';
 
@Injectable()
export class JwtInterceptor implements HttpInterceptor {
 
  constructor(
    private authService: AuthService,
    private router: Router
  ) {}
 
  intercept(request: HttpRequest<any>, next: HttpHandler): Observable<HttpEvent<any>> {
    const token = this.authService.getToken();
 
    // Si hay token, clonar el request y agregar el header.
    // Los requests HTTP son inmutables — por eso clonamos.
    if (token) {
      request = request.clone({
        setHeaders: {
          Authorization: `Bearer ${token}`
        }
      });
    }
 
    return next.handle(request).pipe(
      catchError((error: HttpErrorResponse) => {
        // Si el servidor responde 401, el token expiró o es inválido.
        // Redirigimos al login automáticamente.
        if (error.status === 401) {
          this.authService.logout();
          this.router.navigate(['/auth/login']);
        }
        return throwError(() => error);
      })
    );
  }
}