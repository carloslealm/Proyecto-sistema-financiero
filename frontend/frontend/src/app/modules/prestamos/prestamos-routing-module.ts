import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';
import { PrestamosListComponent } from './prestamos-list/prestamos-list';
import { PrestamoFormComponent } from './prestamo-form/prestamo-form';
import { PrestamoDetalleComponent } from './prestamo-detalle/prestamo-detalle';
 
const routes: Routes = [
  { path: '',          component: PrestamosListComponent },
  { path: 'nuevo',     component: PrestamoFormComponent },
  { path: ':id',       component: PrestamoDetalleComponent }
];
 
@NgModule({
  imports: [RouterModule.forChild(routes)],
  exports: [RouterModule]
})
export class PrestamosRoutingModule {}