import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';
import { ClientesListComponent } from './clientes-list/clientes-list';
import { ClienteFormComponent } from './cliente-form/cliente-form';

const routes: Routes = [
  { path: '',           component: ClientesListComponent },
  { path: 'nuevo',      component: ClienteFormComponent },
  { path: ':id/editar', component: ClienteFormComponent }
];

@NgModule({
  imports: [RouterModule.forChild(routes)],
  exports: [RouterModule]
})
export class ClientesRoutingModule {}