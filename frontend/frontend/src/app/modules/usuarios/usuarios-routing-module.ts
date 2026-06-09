import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';
import { UsuariosListComponent } from './usuarios-list/usuarios-list';
import { UsuarioFormComponent } from './usuario-form/usuario-form';
 
const routes: Routes = [
  { path: '',       component: UsuariosListComponent },
  { path: 'nuevo',  component: UsuarioFormComponent }
];
 
@NgModule({
  imports: [RouterModule.forChild(routes)],
  exports: [RouterModule]
})
export class UsuariosRoutingModule {}