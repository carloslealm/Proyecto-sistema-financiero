import { NgModule } from '@angular/core';
import { BrowserModule } from '@angular/platform-browser';
import { HTTP_INTERCEPTORS, HttpClientModule } from '@angular/common/http';
import { App } from './app';
import { JwtInterceptor } from './interceptors/jwt.interceptor';
import { AppRoutingModule } from './app-routing-module';
@NgModule({
  declarations: [
    App
  ],
  imports: [
    BrowserModule,
    HttpClientModule,
    AppRoutingModule
  ],
  providers: [
    {
      provide: HTTP_INTERCEPTORS,
      useClass: JwtInterceptor,
      multi: true
    }
  ],
  bootstrap: [App]
})
export class AppModule {}