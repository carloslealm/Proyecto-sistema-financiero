import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';
import { LayoutComponent } from './layout/layout';
import { DashboardHomeComponent } from './dashboard-home/dashboard-home';

const routes: Routes = [
  {
    path: '',
    component: LayoutComponent,
    children: [
      { path: '', component: DashboardHomeComponent },
      {
        path: 'clientes',
        loadChildren: () =>
          import('../clientes/clientes-module').then(m => m.ClientesModule)
      },
      {
        path: 'prestamos',
        loadChildren: () =>
          import('../prestamos/prestamos-module').then(m => m.PrestamosModule)
      }
    ]
  }
];

@NgModule({
  imports: [RouterModule.forChild(routes)],
  exports: [RouterModule]
})
export class DashboardRoutingModule {}