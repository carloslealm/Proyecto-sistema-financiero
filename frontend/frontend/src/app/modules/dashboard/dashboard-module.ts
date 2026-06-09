import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule } from '@angular/router';
import { DashboardRoutingModule } from './dashboard-routing-module';
import { LayoutComponent } from './layout/layout';
import { NavbarComponent } from './navbar/navbar';
import { SidebarComponent } from './sidebar/sidebar';
import { DashboardHomeComponent } from './dashboard-home/dashboard-home';
import { PerfilComponent } from './perfil/perfil';
import { ReactiveFormsModule } from '@angular/forms';


@NgModule({
  declarations: [
    LayoutComponent,
    NavbarComponent,
    SidebarComponent,
    DashboardHomeComponent,
    PerfilComponent,
  ],
  imports: [CommonModule, RouterModule, DashboardRoutingModule, ReactiveFormsModule],
})
export class DashboardModule {}
