import { Component, OnInit, ChangeDetectorRef } from '@angular/core';
import { FormBuilder, FormGroup, Validators } from '@angular/forms';
import { UsuarioService } from '../../../services/usuario.service';

@Component({
  selector: 'app-perfil',
  templateUrl: './perfil.html',
  styleUrls: ['./perfil.scss'],
  standalone: false
})
export class PerfilComponent implements OnInit {

  perfil: any = null;
  formDatos!: FormGroup;
  formPassword!: FormGroup;

  loadingPerfil  = true;
  guardandoDatos = false;
  guardandoPass  = false;
  error          = '';
  exitoso        = '';
  showPassActual = false;
  showPassNuevo  = false;

  constructor(
    private fb: FormBuilder,
    private usuarioService: UsuarioService,
    private cdr: ChangeDetectorRef
  ) {}

  ngOnInit(): void {
    this.inicializarForms();
    this.cargarPerfil();
  }

  inicializarForms(): void {
    this.formDatos = this.fb.group({
      nombre:   ['', [Validators.required, Validators.minLength(2)]],
      apellido: ['', [Validators.required, Validators.minLength(2)]],
      telefono: ['', [Validators.pattern(/^[0-9]{7,15}$/)]]
    });

    this.formPassword = this.fb.group({
      passwordActual: ['', Validators.required],
      passwordNuevo:  ['', [Validators.required, Validators.minLength(8)]],
      confirmar:      ['', Validators.required]
    }, { validators: this.passwordsIguales });
  }

  passwordsIguales(group: FormGroup) {
    const nuevo    = group.get('passwordNuevo')?.value;
    const confirmar = group.get('confirmar')?.value;
    return nuevo === confirmar ? null : { noCoinciden: true };
  }

  cargarPerfil(): void {
    this.loadingPerfil = true;
    this.usuarioService.obtenerPerfil().subscribe({
      next: (response) => {
        if (response.success) {
          this.perfil = response.data;
          this.formDatos.patchValue({
            nombre:   this.perfil.nombre,
            apellido: this.perfil.apellido,
            telefono: this.perfil.telefono
          });
        }
        this.loadingPerfil = false;
        this.cdr.detectChanges();
      },
      error: () => {
        this.error = 'Error al cargar el perfil';
        this.loadingPerfil = false;
        this.cdr.detectChanges();
      }
    });
  }

  guardarDatos(): void {
    if (this.formDatos.invalid) {
      this.formDatos.markAllAsTouched();
      return;
    }

    this.guardandoDatos = true;
    this.error = '';

    this.usuarioService.actualizarPerfil(this.formDatos.value).subscribe({
      next: (response) => {
        this.guardandoDatos = false;
        if (response.success) {
          this.exitoso = 'Datos actualizados correctamente';
          setTimeout(() => { this.exitoso = ''; this.cdr.detectChanges(); }, 3000);
        }
        this.cdr.detectChanges();
      },
      error: (err) => {
        this.guardandoDatos = false;
        this.error = err.error?.message || 'Error al actualizar';
        this.cdr.detectChanges();
      }
    });
  }

  cambiarPassword(): void {
    if (this.formPassword.invalid) {
      this.formPassword.markAllAsTouched();
      return;
    }

    this.guardandoPass = true;
    this.error = '';

    const request = {
      passwordActual: this.formPassword.value.passwordActual,
      passwordNuevo:  this.formPassword.value.passwordNuevo
    };

    this.usuarioService.actualizarPerfil(request).subscribe({
      next: (response) => {
        this.guardandoPass = false;
        if (response.success) {
          this.exitoso = '¡Contraseña actualizada correctamente!';
          this.formPassword.reset();
          setTimeout(() => { this.exitoso = ''; this.cdr.detectChanges(); }, 3000);
        } else {
          this.error = response.message;
        }
        this.cdr.detectChanges();
      },
      error: (err) => {
        this.guardandoPass = false;
        this.error = err.error?.message || 'Error al cambiar contraseña';
        this.cdr.detectChanges();
      }
    });
  }

  get passwordNoCoincide(): boolean {
    return this.formPassword.hasError('noCoinciden') &&
           this.formPassword.get('confirmar')!.touched;
  }

  getInicial(nombre: string, apellido: string): string {
    return `${nombre?.charAt(0) || ''}${apellido?.charAt(0) || ''}`.toUpperCase();
  }
}