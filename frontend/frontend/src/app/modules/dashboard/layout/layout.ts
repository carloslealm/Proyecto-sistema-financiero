import { Component } from '@angular/core';
 
@Component({
  selector: 'app-layout',
  templateUrl: './layout.html',
  styleUrls: ['./layout.scss'],
  standalone: false
})
export class LayoutComponent {
  sidebarCollapsed = false;
 
  toggleSidebar(): void {
    this.sidebarCollapsed = !this.sidebarCollapsed;
  }
}