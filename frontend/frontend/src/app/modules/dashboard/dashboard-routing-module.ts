import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';
import { LayoutComponent } from './layout/layout';
import { DashboardHomeComponent } from './dashboard-home/dashboard-home';
import { PerfilComponent } from './perfil/perfil';

const routes: Routes = [
  {
    path: '',
    component: LayoutComponent,
    children: [
      { path: '', component: DashboardHomeComponent },
      { path: 'perfil',  component: PerfilComponent },
      {
        path: 'clientes',
        loadChildren: () =>
          import('../clientes/clientes-module').then(m => m.ClientesModule)
      },
      {
        path: 'prestamos',
        loadChildren: () =>
          import('../prestamos/prestamos-module').then(m => m.PrestamosModule)
      },
      {
        path: 'usuarios',
        loadChildren: () =>
          import('../usuarios/usuarios-module').then(m => m.UsuariosModule)
      }
    ]
  }
];

@NgModule({
  imports: [RouterModule.forChild(routes)],
  exports: [RouterModule]
})
export class DashboardRoutingModule {}