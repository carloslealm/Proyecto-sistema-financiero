import { Component, OnInit, ChangeDetectorRef } from '@angular/core';
import { Router } from '@angular/router';
import { UsuarioService } from '../../../services/usuario.service';
 
@Component({
  selector: 'app-usuarios-list',
  templateUrl: './usuarios-list.html',
  styleUrls: ['./usuarios-list.scss'],
  standalone: false
})
export class UsuariosListComponent implements OnInit {
 
  usuarios: any[] = [];
  loading  = true;
  error    = '';
  exitoso  = '';
 
  paginaActual   = 0;
  totalPaginas   = 0;
  totalElementos = 0;
 
  usuarioADesactivar: any = null;
 
  constructor(
    private usuarioService: UsuarioService,
    private router: Router,
    private cdr: ChangeDetectorRef
  ) {}
 
  ngOnInit(): void {
    this.cargarUsuarios();
  }
 
  cargarUsuarios(): void {
    this.loading = true;
    this.usuarioService.listar(this.paginaActual).subscribe({
      next: (response) => {
        if (response.success) {
          this.usuarios       = response.data.content;
          this.totalPaginas   = response.data.totalPages;
          this.totalElementos = response.data.totalElements;
        }
        this.loading = false;
        this.cdr.detectChanges();
      },
      error: () => {
        this.error   = 'Error al cargar los usuarios';
        this.loading = false;
        this.cdr.detectChanges();
      }
    });
  }
 
  nuevo(): void {
    this.router.navigate(['/dashboard/usuarios/nuevo']);
  }
 
  confirmarCambioEstado(usuario: any): void {
    this.usuarioADesactivar = usuario;
  }
 
  cancelar(): void {
    this.usuarioADesactivar = null;
  }
 
  cambiarEstado(): void {
    if (!this.usuarioADesactivar) return;
    this.usuarioService.cambiarEstado(this.usuarioADesactivar.id).subscribe({
      next: () => {
        this.exitoso = `Usuario ${this.usuarioADesactivar.activo ? 'desactivado' : 'activado'} correctamente`;
        this.usuarioADesactivar = null;
        this.cargarUsuarios();
        setTimeout(() => { this.exitoso = ''; this.cdr.detectChanges(); }, 3000);
      },
      error: () => {
        this.error = 'Error al cambiar estado';
        this.usuarioADesactivar = null;
        this.cdr.detectChanges();
      }
    });
  }
 
  cambiarRol(usuario: any, rol: string): void {
    const rolesActuales: string[] = usuario.roles || [];
    let nuevosRoles: string[];
 
    if (rolesActuales.includes(rol)) {
      nuevosRoles = rolesActuales.filter((r: string) => r !== rol);
    } else {
      nuevosRoles = [...rolesActuales, rol];
    }
 
    if (nuevosRoles.length === 0) {
      this.error = 'El usuario debe tener al menos un rol';
      return;
    }
 
    this.usuarioService.cambiarRoles(usuario.id, nuevosRoles).subscribe({
      next: () => {
        usuario.roles = nuevosRoles;
        this.exitoso  = 'Roles actualizados correctamente';
        setTimeout(() => { this.exitoso = ''; this.cdr.detectChanges(); }, 3000);
        this.cdr.detectChanges();
      },
      error: () => {
        this.error = 'Error al actualizar roles';
        this.cdr.detectChanges();
      }
    });
  }
 
  tieneRol(usuario: any, rol: string): boolean {
    return usuario.roles?.includes(rol) ?? false;
  }
 
  irAPagina(pagina: number): void {
    if (pagina >= 0 && pagina < this.totalPaginas) {
      this.paginaActual = pagina;
      this.cargarUsuarios();
    }
  }
 
  get paginas(): number[] {
    return Array.from({ length: this.totalPaginas }, (_, i) => i);
  }
}