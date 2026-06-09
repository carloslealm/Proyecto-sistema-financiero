import { Component, OnInit, ChangeDetectorRef } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import { PrestamoService } from '../../../services/prestamo.service';
import { Prestamo, Cuota } from '../../../models/prestamo.model';
 
@Component({
  selector: 'app-prestamo-detalle',
  templateUrl: './prestamo-detalle.html',
  styleUrls: ['./prestamo-detalle.scss'],
  standalone: false
})
export class PrestamoDetalleComponent implements OnInit {
 
  prestamo!: Prestamo;
  cuotas: Cuota[] = [];
  loading     = true;
  loadingAcc  = false;
  error       = '';
  exitoso     = '';
  motivoRechazo = '';
  mostrarRechazo = false;
 
  constructor(
    private route: ActivatedRoute,
    private router: Router,
    private prestamoService: PrestamoService,
    private cdr: ChangeDetectorRef
  ) {}
 
  ngOnInit(): void {
    const id = Number(this.route.snapshot.paramMap.get('id'));
    this.cargarPrestamo(id);
  }
 
  cargarPrestamo(id: number): void {
    this.loading = true;
    this.prestamoService.obtener(id).subscribe({
      next: (response) => {
        if (response.success) {
          this.prestamo = response.data;
          if (this.prestamo.estado === 'AL_DIA' ||
              this.prestamo.estado === 'EN_MORA' ||
              this.prestamo.estado === 'DESEMBOLSADO') {
            this.cargarPlanDePagos(id);
          }
        }
        this.loading = false;
        this.cdr.detectChanges();
      },
      error: () => {
        this.error   = 'Error al cargar el préstamo';
        this.loading = false;
        this.cdr.detectChanges();
      }
    });
  }
 
  cargarPlanDePagos(id: number): void {
    this.prestamoService.planDePagos(id).subscribe({
      next: (response) => {
        if (response.success) {
          this.cuotas = response.data;
        }
        this.cdr.detectChanges();
      }
    });
  }
 
  aprobar(): void {
    this.loadingAcc = true;
    this.prestamoService.aprobar(this.prestamo.id).subscribe({
      next: (response) => {
        if (response.success) {
          this.prestamo = response.data;
          this.exitoso  = 'Préstamo aprobado exitosamente';
        }
        this.loadingAcc = false;
        this.cdr.detectChanges();
      },
      error: (err) => {
        this.error      = err.error?.message || 'Error al aprobar';
        this.loadingAcc = false;
        this.cdr.detectChanges();
      }
    });
  }
 
  rechazar(): void {
    if (!this.motivoRechazo.trim()) return;
    this.loadingAcc = true;
    this.prestamoService.rechazar(this.prestamo.id, this.motivoRechazo).subscribe({
      next: (response) => {
        if (response.success) {
          this.prestamo      = response.data;
          this.exitoso       = 'Préstamo rechazado';
          this.mostrarRechazo = false;
        }
        this.loadingAcc = false;
        this.cdr.detectChanges();
      },
      error: (err) => {
        this.error      = err.error?.message || 'Error al rechazar';
        this.loadingAcc = false;
        this.cdr.detectChanges();
      }
    });
  }
 
  desembolsar(): void {
    this.loadingAcc = true;
    this.prestamoService.desembolsar(this.prestamo.id).subscribe({
      next: (response) => {
        if (response.success) {
          this.prestamo = response.data;
          this.exitoso  = 'Préstamo desembolsado — Plan de pagos generado';
          this.cargarPlanDePagos(this.prestamo.id);
        }
        this.loadingAcc = false;
        this.cdr.detectChanges();
      },
      error: (err) => {
        this.error      = err.error?.message || 'Error al desembolsar';
        this.loadingAcc = false;
        this.cdr.detectChanges();
      }
    });
  }
 
  volver(): void {
    this.router.navigate(['/dashboard/prestamos']);
  }
 
  getBadgeClass(estado: string): string {
    const clases: { [key: string]: string } = {
      'AL_DIA':      'bg-success',
      'EN_MORA':     'bg-danger',
      'SOLICITADO':  'bg-warning text-dark',
      'EN_REVISION': 'bg-info text-dark',
      'APROBADO':    'bg-primary',
      'DESEMBOLSADO':'bg-primary',
      'RECHAZADO':   'bg-secondary',
      'CANCELADO':   'bg-secondary'
    };
    return clases[estado] || 'bg-secondary';
  }
 
  getCuotaClass(estado: string): string {
    const clases: { [key: string]: string } = {
      'PAGADA':        'table-success',
      'EN_MORA':       'table-danger',
      'PAGADA_PARCIAL':'table-warning',
      'PENDIENTE':     '',
      'CONDONADA':     'table-secondary'
    };
    return clases[estado] || '';
  }
 
  formatCurrency(value: number): string {
    if (!value) return '$0';
    return new Intl.NumberFormat('es-CO', {
      style: 'currency',
      currency: 'COP',
      minimumFractionDigits: 0
    }).format(value);
  }
 
  get puedeAprobar(): boolean {
    return this.prestamo?.estado === 'SOLICITADO' ||
           this.prestamo?.estado === 'EN_REVISION';
  }
 
  get puedeRechazar(): boolean {
    return this.prestamo?.estado === 'SOLICITADO' ||
           this.prestamo?.estado === 'EN_REVISION' ||
           this.prestamo?.estado === 'APROBADO';
  }
 
  get puedeDesembolsar(): boolean {
    return this.prestamo?.estado === 'APROBADO';
  }
}