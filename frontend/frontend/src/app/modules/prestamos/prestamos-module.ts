import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ReactiveFormsModule, FormsModule } from '@angular/forms';
import { RouterModule } from '@angular/router';
import { PrestamosRoutingModule } from './prestamos-routing-module';
import { PrestamosListComponent } from './prestamos-list/prestamos-list';
import { PrestamoFormComponent } from './prestamo-form/prestamo-form';
import { PrestamoDetalleComponent } from './prestamo-detalle/prestamo-detalle';
 
@NgModule({
  declarations: [
    PrestamosListComponent,
    PrestamoFormComponent,
    PrestamoDetalleComponent
  ],
  imports: [
    CommonModule,
    ReactiveFormsModule,
    FormsModule,
    RouterModule,
    PrestamosRoutingModule
  ]
})
export class PrestamosModule {}
 