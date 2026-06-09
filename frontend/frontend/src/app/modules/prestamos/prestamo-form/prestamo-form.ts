import { Component, OnInit, ChangeDetectorRef } from '@angular/core';
import { FormBuilder, FormGroup, Validators } from '@angular/forms';
import { Router } from '@angular/router';
import { PrestamoService } from '../../../services/prestamo.service';
import { ClienteService } from '../../../services/cliente.service';
import { Cliente } from '../../../models/cliente.model';
 
@Component({
  selector: 'app-prestamo-form',
  templateUrl: './prestamo-form.html',
  styleUrls: ['./prestamo-form.scss'],
  standalone: false
})
export class PrestamoFormComponent implements OnInit {
 
  form!: FormGroup;
  clientes: Cliente[] = [];
  guardando = false;
  error     = '';
  exitoso   = '';
 
  // Vista previa de la cuota calculada
  cuotaEstimada = 0;
 
  constructor(
    private fb: FormBuilder,
    private prestamoService: PrestamoService,
    private clienteService: ClienteService,
    private router: Router,
    private cdr: ChangeDetectorRef
  ) {}
 
  ngOnInit(): void {
    this.inicializarFormulario();
    this.cargarClientes();
  }
 
  inicializarFormulario(): void {
    this.form = this.fb.group({
      clienteId:           ['', Validators.required],
      monto:               ['', [Validators.required, Validators.min(100000)]],
      tasaInteres:         ['0.0245', [Validators.required, Validators.min(0.001)]],
      plazoMeses:          ['12', [Validators.required, Validators.min(1), Validators.max(120)]],
      sistemaAmortizacion: ['FRANCES'],
      observaciones:       ['']
    });
 
    // Calcular cuota en tiempo real cuando cambian los valores
    this.form.valueChanges.subscribe(() => this.calcularCuota());
  }
 
  cargarClientes(): void {
    this.clienteService.listar(0, 100).subscribe({
      next: (response) => {
        if (response.success) {
          this.clientes = response.data.content
            .filter(c => c.activo);
        }
        this.cdr.detectChanges();
      }
    });
  }
 
  calcularCuota(): void {
    const monto = Number(this.form.get('monto')?.value);
    const tasa  = Number(this.form.get('tasaInteres')?.value);
    const plazo = Number(this.form.get('plazoMeses')?.value);
 
    if (monto > 0 && tasa > 0 && plazo > 0) {
      // Fórmula Sistema Francés: C = P * i * (1+i)^n / ((1+i)^n - 1)
      const factor = Math.pow(1 + tasa, plazo);
      this.cuotaEstimada = (monto * tasa * factor) / (factor - 1);
    } else {
      this.cuotaEstimada = 0;
    }
  }
 
  guardar(): void {
    if (this.form.invalid) {
      this.form.markAllAsTouched();
      return;
    }
 
    this.guardando = true;
    this.error     = '';
 
    const datos = {
      ...this.form.value,
      clienteId:   Number(this.form.value.clienteId),
      monto:       Number(this.form.value.monto),
      tasaInteres: Number(this.form.value.tasaInteres),
      plazoMeses:  Number(this.form.value.plazoMeses)
    };
 
    this.prestamoService.solicitar(datos).subscribe({
      next: (response) => {
        this.guardando = false;
        if (response.success) {
          this.exitoso = 'Préstamo solicitado exitosamente';
          setTimeout(() => {
            this.router.navigate([`/dashboard/prestamos/${response.data.id}`]);
          }, 1500);
        }
        this.cdr.detectChanges();
      },
      error: (err) => {
        this.guardando = false;
        this.error = err.error?.message || 'Error al solicitar el préstamo';
        this.cdr.detectChanges();
      }
    });
  }
 
  cancelar(): void {
    this.router.navigate(['/dashboard/prestamos']);
  }
 
  formatCurrency(value: number): string {
    if (!value) return '$0';
    return new Intl.NumberFormat('es-CO', {
      style: 'currency',
      currency: 'COP',
      minimumFractionDigits: 0
    }).format(value);
  }
 
  get clienteId()   { return this.form.get('clienteId')!; }
  get monto()       { return this.form.get('monto')!; }
  get tasaInteres() { return this.form.get('tasaInteres')!; }
  get plazoMeses()  { return this.form.get('plazoMeses')!; }
}