import { Component, OnInit, ChangeDetectorRef } from '@angular/core';
import { Router } from '@angular/router';
import { ClienteService } from '../../../services/cliente.service';
import { Cliente } from '../../../models/cliente.model';

@Component({
  selector: 'app-clientes-list',
  templateUrl: './clientes-list.html',
  styleUrls: ['./clientes-list.scss'],
  standalone: false
})
export class ClientesListComponent implements OnInit {

  clientes: Cliente[] = [];
  loading = true;
  error = '';
  paginaActual = 0;
  totalPaginas = 0;
  totalElementos = 0;
  tamanioPagina = 10;
  terminoBusqueda = '';
  buscando = false;

  // Confirmación de eliminación
  clienteAEliminar: Cliente | null = null;

  constructor(
    private clienteService: ClienteService,
    private router: Router,
    private cdr: ChangeDetectorRef
  ) {}

  ngOnInit(): void {
    this.cargarClientes();
  }

  cargarClientes(): void {
    this.loading = true;
    this.clienteService.listar(
      this.paginaActual,
      this.tamanioPagina,
      this.terminoBusqueda
    ).subscribe({
      next: (response) => {
        if (response.success) {
          this.clientes        = response.data.content;
          this.totalPaginas    = response.data.totalPages;
          this.totalElementos  = response.data.totalElements;
        }
        this.loading = false;
        this.cdr.detectChanges();
      },
      error: () => {
        this.error   = 'Error al cargar los clientes';
        this.loading = false;
        this.cdr.detectChanges();
      }
    });
  }

  buscar(): void {
    this.paginaActual = 0;
    this.cargarClientes();
  }

  limpiarBusqueda(): void {
    this.terminoBusqueda = '';
    this.paginaActual    = 0;
    this.cargarClientes();
  }

  irAPagina(pagina: number): void {
    if (pagina >= 0 && pagina < this.totalPaginas) {
      this.paginaActual = pagina;
      this.cargarClientes();
    }
  }

  nuevo(): void {
  this.router.navigate(['/dashboard/clientes/nuevo']);
  }

  editar(id: number): void {
    this.router.navigate([`/clientes/${id}/editar`]);
  }

  confirmarEliminar(cliente: Cliente): void {
    this.clienteAEliminar = cliente;
  }

  cancelarEliminar(): void {
    this.clienteAEliminar = null;
  }

  eliminar(): void {
    if (!this.clienteAEliminar) return;
    this.clienteService.desactivar(this.clienteAEliminar.id).subscribe({
      next: () => {
        this.clienteAEliminar = null;
        this.cargarClientes();
      },
      error: () => {
        this.error = 'Error al desactivar el cliente';
        this.clienteAEliminar = null;
      }
    });
  }

  get paginas(): number[] {
    return Array.from({ length: this.totalPaginas }, (_, i) => i);
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