import { Component, OnInit, ChangeDetectorRef } from '@angular/core';
import { PrestamoService } from '../../../services/prestamo.service';
import { AuthService } from '../../../services/auth.service';
import { Dashboard } from '../../../models/prestamo.model';

@Component({
  selector: 'app-dashboard-home',
  templateUrl: './dashboard-home.html',
  styleUrls: ['./dashboard-home.scss'],
  standalone: false
})
export class DashboardHomeComponent implements OnInit {

  dashboard!: Dashboard;
  loading = true;
  error = '';

  constructor(
    private prestamoService: PrestamoService,
    private authService: AuthService,
    private cdr: ChangeDetectorRef  // ← agregar esto
  ) {}

  ngOnInit(): void {
    this.cargarDashboard();
  }

  cargarDashboard(): void {
    this.loading = true;
    this.error = '';
    this.prestamoService.dashboard().subscribe({
      next: (response) => {
        this.dashboard = response.data;
        this.loading = false;
        this.cdr.detectChanges();  // ← forzar actualización de la vista
      },
      error: (err) => {
        this.loading = false;
        this.error = 'Error al cargar el dashboard';
        this.cdr.detectChanges();  // ← forzar actualización de la vista
      }
    });
  }

  formatCurrency(value: number): string {
    if (!value) return '$0';
    return new Intl.NumberFormat('es-CO', {
      style: 'currency',
      currency: 'COP',
      minimumFractionDigits: 0
    }).format(value);
  }
}