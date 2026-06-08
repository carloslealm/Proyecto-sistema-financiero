// Guard de roles — verifica que el usuario tenga el rol requerido.
// Uso en rutas: { canActivate: [RoleGuard], data: { role: 'ADMIN' } }
// ═══════════════════════════════════════════════════════════════
import { Injectable } from '@angular/core';
import { ActivatedRouteSnapshot, CanActivate, Router, UrlTree } from '@angular/router';
import { AuthService } from '../services/auth.service';
 
@Injectable({
  providedIn: 'root'
})
export class RoleGuard implements CanActivate {
 
  constructor(
    private authService: AuthService,
    private router: Router
  ) {}
 
  canActivate(route: ActivatedRouteSnapshot): boolean | UrlTree {
    const requiredRole = route.data['role'] as string;
 
    if (this.authService.hasRole(requiredRole)) {
      return true;
    }
 
    // Tiene sesión pero no tiene el rol — mostrar página de acceso denegado
    return this.router.createUrlTree(['/acceso-denegado']);
  }
}
 