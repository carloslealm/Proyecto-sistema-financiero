import { Component, OnInit, ChangeDetectorRef } from '@angular/core';
import { Router } from '@angular/router';
import { PrestamoService } from '../../../services/prestamo.service';
import { Prestamo, EstadoPrestamo } from '../../../models/prestamo.model';
 
@Component({
  selector: 'app-prestamos-list',
  templateUrl: './prestamos-list.html',
  styleUrls: ['./prestamos-list.scss'],
  standalone: false
})
export class PrestamosListComponent implements OnInit {
 
  prestamos: Prestamo[] = [];
  loading = true;
  error = '';
 
  paginaActual   = 0;
  totalPaginas   = 0;
  totalElementos = 0;
  tamanioPagina  = 10;
 
  estadoFiltro = '';
  estados: EstadoPrestamo[] = [
    'SOLICITADO','EN_REVISION','APROBADO','RECHAZADO',
    'DESEMBOLSADO','AL_DIA','EN_MORA','CANCELADO','CASTIGADO'
  ];
 
  constructor(
    private prestamoService: PrestamoService,
    private router: Router,
    private cdr: ChangeDetectorRef
  ) {}
 
  ngOnInit(): void {
    this.cargarPrestamos();
  }
 
  cargarPrestamos(): void {
    this.loading = true;
    const obs = this.estadoFiltro
      ? this.prestamoService.listarPorEstado(this.estadoFiltro, this.paginaActual, this.tamanioPagina)
      : this.prestamoService.listar(this.paginaActual, this.tamanioPagina);
 
    obs.subscribe({
      next: (response) => {
        if (response.success) {
          this.prestamos      = response.data.content;
          this.totalPaginas   = response.data.totalPages;
          this.totalElementos = response.data.totalElements;
        }
        this.loading = false;
        this.cdr.detectChanges();
      },
      error: () => {
        this.error   = 'Error al cargar los préstamos';
        this.loading = false;
        this.cdr.detectChanges();
      }
    });
  }
 
  filtrarPorEstado(): void {
    this.paginaActual = 0;
    this.cargarPrestamos();
  }
 
  limpiarFiltro(): void {
    this.estadoFiltro = '';
    this.paginaActual = 0;
    this.cargarPrestamos();
  }
 
  irAPagina(pagina: number): void {
    if (pagina >= 0 && pagina < this.totalPaginas) {
      this.paginaActual = pagina;
      this.cargarPrestamos();
    }
  }
 
  nuevo(): void {
    this.router.navigate(['/dashboard/prestamos/nuevo']);
  }
 
  verDetalle(id: number): void {
    this.router.navigate([`/dashboard/prestamos/${id}`]);
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
      'CANCELADO':   'bg-secondary',
      'CASTIGADO':   'bg-dark'
    };
    return clases[estado] || 'bg-secondary';
  }
 
  formatCurrency(value: number): string {
    if (!value) return '$0';
    return new Intl.NumberFormat('es-CO', {
      style: 'currency',
      currency: 'COP',
      minimumFractionDigits: 0
    }).format(value);
  }
 
  get paginas(): number[] {
    const rango = 2;
    const inicio = Math.max(0, this.paginaActual - rango);
    const fin = Math.min(this.totalPaginas - 1, this.paginaActual + rango);
    return Array.from({ length: fin - inicio + 1 }, (_, i) => inicio + i);
  }
}