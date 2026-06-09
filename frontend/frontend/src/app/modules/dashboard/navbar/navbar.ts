import { Component, EventEmitter, OnInit, Output } from '@angular/core';
import { AuthService } from '../../../services/auth.service';
import { AuthResponse } from '../../../models/auth.model';
 
@Component({
  selector: 'app-navbar',
  templateUrl: './navbar.html',
  styleUrls: ['./navbar.scss'],
  standalone: false
})
export class NavbarComponent implements OnInit {
 
  @Output() toggleSidebar = new EventEmitter<void>();
 
  currentUser: AuthResponse | null = null;
 
  constructor(private authService: AuthService) {}
 
  ngOnInit(): void {
    this.currentUser = this.authService.getCurrentUser();
  }
 
  onToggleSidebar(): void {
    this.toggleSidebar.emit();
  }
 
  onLogout(): void {
    this.authService.logout();
  }
 
  get userInitials(): string {
    if (!this.currentUser) return 'U';
    return `${this.currentUser.nombre.charAt(0)}${this.currentUser.apellido.charAt(0)}`.toUpperCase();
  }
 
  get isAdmin(): boolean {
    return this.authService.isAdmin();
  }
}