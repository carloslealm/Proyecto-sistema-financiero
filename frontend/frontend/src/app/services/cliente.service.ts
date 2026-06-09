import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../environments/environment';
import { ApiResponse, PageResponse } from '../models/auth.model';
import { Cliente, ClienteRequest } from '../models/cliente.model';
 
@Injectable({
  providedIn: 'root'
})
export class ClienteService {
 
  private readonly API = `${environment.apiUrl}/clientes`;
 
  constructor(private http: HttpClient) {}
 
  listar(page = 0, size = 10, buscar = ''): Observable<ApiResponse<PageResponse<Cliente>>> {
    let params = new HttpParams()
      .set('page', page)
      .set('size', size);
 
    if (buscar.trim()) {
      params = params.set('buscar', buscar.trim());
    }
 
    return this.http.get<ApiResponse<PageResponse<Cliente>>>(this.API, { params });
  }
 
  obtener(id: number): Observable<ApiResponse<Cliente>> {
    return this.http.get<ApiResponse<Cliente>>(`${this.API}/${id}`);
  }
 
  crear(request: ClienteRequest): Observable<ApiResponse<Cliente>> {
    return this.http.post<ApiResponse<Cliente>>(this.API, request);
  }
 
  actualizar(id: number, request: ClienteRequest): Observable<ApiResponse<Cliente>> {
    return this.http.put<ApiResponse<Cliente>>(`${this.API}/${id}`, request);
  }
 
  desactivar(id: number): Observable<ApiResponse<void>> {
    return this.http.delete<ApiResponse<void>>(`${this.API}/${id}`);
  }
}