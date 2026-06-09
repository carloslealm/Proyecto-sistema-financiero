import { Component, OnInit, ChangeDetectorRef } from '@angular/core';
import { FormBuilder, FormGroup, Validators } from '@angular/forms';
import { Router } from '@angular/router';
import { UsuarioService } from '../../../services/usuario.service';
 
@Component({
  selector: 'app-usuario-form',
  templateUrl: './usuario-form.html',
  styleUrls: ['./usuario-form.scss'],
  standalone: false
})
export class UsuarioFormComponent implements OnInit {
 
  form!: FormGroup;
  guardando = false;
  error     = '';
  exitoso   = '';
  showPass  = false;
 
  constructor(
    private fb: FormBuilder,
    private usuarioService: UsuarioService,
    private router: Router,
    private cdr: ChangeDetectorRef
  ) {}
 
  ngOnInit(): void {
    this.form = this.fb.group({
      nombre:   ['', [Validators.required, Validators.minLength(2)]],
      apellido: ['', [Validators.required, Validators.minLength(2)]],
      email:    ['', [Validators.required, Validators.email]],
      telefono: ['', [Validators.pattern(/^[0-9]{7,15}$/)]],
      password: ['', [Validators.required, Validators.minLength(8)]],
      esAdmin:  [false],
      esAsesor: [true]
    });
  }
 
  guardar(): void {
    if (this.form.invalid) {
      this.form.markAllAsTouched();
      return;
    }
 
    const roles: string[] = [];
    if (this.form.value.esAdmin)  roles.push('ADMIN');
    if (this.form.value.esAsesor) roles.push('ASESOR');
    if (roles.length === 0)       roles.push('ASESOR');
 
    const request = {
      nombre:   this.form.value.nombre,
      apellido: this.form.value.apellido,
      email:    this.form.value.email,
      telefono: this.form.value.telefono,
      password: this.form.value.password,
      roles
    };
 
    this.guardando = true;
    this.usuarioService.crear(request).subscribe({
      next: (response) => {
        this.guardando = false;
        if (response.success) {
          this.exitoso = 'Usuario creado exitosamente';
          setTimeout(() => this.router.navigate(['/dashboard/usuarios']), 1500);
        } else {
          this.error = response.message || 'Error al crear el usuario';
        }
        this.cdr.detectChanges();
      },
      error: (err) => {
        this.guardando = false;
        this.error = err.error?.message || 'Error al crear el usuario';
        this.cdr.detectChanges();
      }
    });
  }
 
  cancelar(): void {
    this.router.navigate(['/dashboard/usuarios']);
  }
 
  get nombre()   { return this.form.get('nombre')!; }
  get apellido() { return this.form.get('apellido')!; }
  get email()    { return this.form.get('email')!; }
  get password() { return this.form.get('password')!; }
}