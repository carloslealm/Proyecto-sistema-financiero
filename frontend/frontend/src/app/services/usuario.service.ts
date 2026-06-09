import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { ApiResponse, PageResponse } from '../models/auth.model';

@Injectable({
  providedIn: 'root'
})
export class UsuarioService {

  private readonly API = 'http://localhost:8080/api/v1/usuarios';

  constructor(private http: HttpClient) {}

  listar(page = 0, size = 10): Observable<any> {
    return this.http.get<any>(`${this.API}?page=${page}&size=${size}`);
  }

  obtener(id: number): Observable<any> {
    return this.http.get<any>(`${this.API}/${id}`);
  }

  crear(request: any): Observable<any> {
    return this.http.post<any>(this.API, request);
  }

  cambiarEstado(id: number): Observable<any> {
    return this.http.patch<any>(`${this.API}/${id}/estado`, {});
  }

  cambiarRoles(id: number, roles: string[]): Observable<any> {
    return this.http.patch<any>(`${this.API}/${id}/roles`, { roles });
  }

  obtenerPerfil(): Observable<any> {
  return this.http.get<any>('http://localhost:8080/api/v1/auth/perfil');
}

  actualizarPerfil(request: any): Observable<any> {
  return this.http.put<any>('http://localhost:8080/api/v1/auth/perfil', request);
}
}