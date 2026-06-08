// El Guard protege las rutas. Si el usuario no está autenticado
// y trata de acceder a /dashboard, lo redirige a /auth/login.
// ═══════════════════════════════════════════════════════════════
import { Injectable } from '@angular/core';
import { CanActivate, Router, UrlTree } from '@angular/router';
import { AuthService } from '../services/auth.service';
 
@Injectable({
  providedIn: 'root'
})
export class AuthGuard implements CanActivate {
 
  constructor(
    private authService: AuthService,
    private router: Router
  ) {}
 
  canActivate(): boolean | UrlTree {
    if (this.authService.isAuthenticated()) {
      return true;
    }
    // Si no está autenticado, redirigir al login
    return this.router.createUrlTree(['/auth/login']);
  }
}