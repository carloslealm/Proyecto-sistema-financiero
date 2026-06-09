import { Component, Input } from '@angular/core';
import { AuthService } from '../../../services/auth.service';
 
@Component({
  selector: 'app-sidebar',
  templateUrl: './sidebar.html',
  styleUrls: ['./sidebar.scss'],
  standalone: false
})
export class SidebarComponent {
  @Input() collapsed = false;
 
  constructor(private authService: AuthService) {}
 
  get isAdmin(): boolean {
    return this.authService.isAdmin();
  }
}