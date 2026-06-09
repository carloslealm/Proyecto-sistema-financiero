import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ReactiveFormsModule, FormsModule } from '@angular/forms';
import { RouterModule } from '@angular/router';
import { ClientesRoutingModule } from './clientes-routing-module';
import { ClientesListComponent } from './clientes-list/clientes-list';
import { ClienteFormComponent } from './cliente-form/cliente-form';

@NgModule({
  declarations: [
    ClientesListComponent,
    ClienteFormComponent
  ],
  imports: [
    CommonModule,
    ReactiveFormsModule,
    FormsModule,
    RouterModule,
    ClientesRoutingModule
  ]
})
export class ClientesModule {}