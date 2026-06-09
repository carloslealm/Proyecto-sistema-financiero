import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ReactiveFormsModule, FormsModule } from '@angular/forms';
import { RouterModule } from '@angular/router';
import { UsuariosRoutingModule } from './usuarios-routing-module';
import { UsuariosListComponent } from './usuarios-list/usuarios-list';
import { UsuarioFormComponent } from './usuario-form/usuario-form';
 
@NgModule({
  declarations: [
    UsuariosListComponent,
    UsuarioFormComponent
  ],
  imports: [
    CommonModule,
    ReactiveFormsModule,
    FormsModule,
    RouterModule,
    UsuariosRoutingModule
  ]
})
export class UsuariosModule {}