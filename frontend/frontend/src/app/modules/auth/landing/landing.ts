import { Component } from '@angular/core';
import { Router } from '@angular/router';
import { AuthService } from '../../../services/auth.service';
 
@Component({
  selector: 'app-landing',
  templateUrl: './landing.html',
  styleUrls: ['./landing.scss'],
  standalone: false
})
export class LandingComponent {
 
  constructor(
    private router: Router,
    private authService: AuthService
  ) {}
 
  ngOnInit(): void {
    // Si ya está autenticado, ir al dashboard
    if (this.authService.isAuthenticated()) {
      this.router.navigate(['/dashboard']);
    }
  }
 
  irALogin(): void {
    this.router.navigate(['/auth/login']);
  }
 
  irARegistro(): void {
    this.router.navigate(['/auth/registro']);
  }
}