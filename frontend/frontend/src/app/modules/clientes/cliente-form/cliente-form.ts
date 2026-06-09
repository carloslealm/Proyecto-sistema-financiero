// ═══════════════════════════════════════════════════════════════
// src/app/modules/clientes/cliente-form/cliente-form.component.ts
// ═══════════════════════════════════════════════════════════════
import { Component, OnInit, ChangeDetectorRef } from '@angular/core';
import { FormBuilder, FormGroup, Validators } from '@angular/forms';
import { ActivatedRoute, Router } from '@angular/router';
import { ClienteService } from '../../../services/cliente.service';

@Component({
  selector: 'app-cliente-form',
  templateUrl: './cliente-form.html',
  styleUrls: ['./cliente-form.scss'],
  standalone: false
})
export class ClienteFormComponent implements OnInit {

  form!: FormGroup;
  loading    = false;
  guardando  = false;
  error      = '';
  exitoso    = '';
  esEdicion  = false;
  clienteId  = 0;

  constructor(
    private fb: FormBuilder,
    private clienteService: ClienteService,
    private router: Router,
    private route: ActivatedRoute,
    private cdr: ChangeDetectorRef
  ) {}

  ngOnInit(): void {
    this.clienteId = Number(this.route.snapshot.paramMap.get('id'));
    this.esEdicion = this.clienteId > 0;
    this.inicializarFormulario();

    if (this.esEdicion) {
      this.cargarCliente();
    }
  }

  inicializarFormulario(): void {
    this.form = this.fb.group({
      cedula:          ['', [Validators.required, Validators.minLength(6)]],
      nombre:          ['', [Validators.required, Validators.minLength(2)]],
      apellido:        ['', [Validators.required, Validators.minLength(2)]],
      email:           ['', [Validators.email]],
      telefono:        ['', [Validators.required, Validators.pattern(/^[0-9]{7,15}$/)]],
      direccion:       [''],
      ciudad:          [''],
      fechaNacimiento: [''],
      ingresoMensual:  ['', [Validators.min(0)]]
    });
  }

  cargarCliente(): void {
    this.loading = true;
    this.clienteService.obtener(this.clienteId).subscribe({
      next: (response) => {
        if (response.success) {
          const c = response.data;
          this.form.patchValue({
            cedula:          c.cedula,
            nombre:          c.nombre,
            apellido:        c.apellido,
            email:           c.email || '',
            telefono:        c.telefono,
            direccion:       c.direccion || '',
            ciudad:          c.ciudad || '',
            fechaNacimiento: c.fechaNacimiento || '',
            ingresoMensual:  c.ingresoMensual || ''
          });
        }
        this.loading = false;
        this.cdr.detectChanges();
      },
      error: () => {
        this.error   = 'Error al cargar el cliente';
        this.loading = false;
        this.cdr.detectChanges();
      }
    });
  }

  guardar(): void {
    if (this.form.invalid) {
      this.form.markAllAsTouched();
      return;
    }

    this.guardando = true;
    this.error     = '';
    this.exitoso   = '';

    const datos = this.form.value;

    const operacion = this.esEdicion
      ? this.clienteService.actualizar(this.clienteId, datos)
      : this.clienteService.crear(datos);

    operacion.subscribe({
      next: (response) => {
        this.guardando = false;
        if (response.success) {
          this.exitoso = this.esEdicion
            ? 'Cliente actualizado exitosamente'
            : 'Cliente creado exitosamente';
          setTimeout(() => this.router.navigate(['/clientes']), 1500);
        }
        this.cdr.detectChanges();
      },
      error: (err) => {
        this.guardando = false;
        this.error     = err.error?.message || 'Error al guardar el cliente';
        this.cdr.detectChanges();
      }
    });
  }

  cancelar(): void {
    this.router.navigate(['/clientes']);
  }

  // Getters para validación en el template
  get cedula()    { return this.form.get('cedula')!; }
  get nombre()    { return this.form.get('nombre')!; }
  get apellido()  { return this.form.get('apellido')!; }
  get email()     { return this.form.get('email')!; }
  get telefono()  { return this.form.get('telefono')!; }
}