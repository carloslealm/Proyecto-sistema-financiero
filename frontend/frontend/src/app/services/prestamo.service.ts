import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { ApiResponse, PageResponse } from '../models/auth.model';
import { Prestamo, PrestamoRequest, Cuota, Dashboard } from '../models/prestamo.model';
import { environment } from '../../environments/environment';

@Injectable({
  providedIn: 'root'
})
export class PrestamoService {

  private readonly BASE = environment.apiUrl;
private readonly API  = `${environment.apiUrl}/prestamos`;

  constructor(private http: HttpClient) {}

  listar(page = 0, size = 10): Observable<ApiResponse<PageResponse<Prestamo>>> {
    const params = new HttpParams().set('page', page).set('size', size);
    return this.http.get<ApiResponse<PageResponse<Prestamo>>>(this.API, { params });
  }

  listarPorEstado(estado: string, page = 0, size = 10): Observable<ApiResponse<PageResponse<Prestamo>>> {
    const params = new HttpParams().set('page', page).set('size', size);
    return this.http.get<ApiResponse<PageResponse<Prestamo>>>(
      `${this.API}/estado/${estado}`, { params }
    );
  }

  obtener(id: number): Observable<ApiResponse<Prestamo>> {
    return this.http.get<ApiResponse<Prestamo>>(`${this.API}/${id}`);
  }

  solicitar(request: PrestamoRequest): Observable<ApiResponse<Prestamo>> {
    return this.http.post<ApiResponse<Prestamo>>(this.API, request);
  }

  aprobar(id: number): Observable<ApiResponse<Prestamo>> {
    return this.http.patch<ApiResponse<Prestamo>>(`${this.API}/${id}/aprobar`, {});
  }

  rechazar(id: number, motivo: string): Observable<ApiResponse<Prestamo>> {
    const params = new HttpParams().set('motivo', motivo);
    return this.http.patch<ApiResponse<Prestamo>>(
      `${this.API}/${id}/rechazar`, {}, { params }
    );
  }

  desembolsar(id: number): Observable<ApiResponse<Prestamo>> {
    return this.http.patch<ApiResponse<Prestamo>>(`${this.API}/${id}/desembolsar`, {});
  }

  planDePagos(id: number): Observable<ApiResponse<Cuota[]>> {
    return this.http.get<ApiResponse<Cuota[]>>(`${this.API}/${id}/plan-pagos`);
  }

  dashboard(): Observable<ApiResponse<Dashboard>> {
    return this.http.get<ApiResponse<Dashboard>>(`${this.BASE}/dashboard`);
  }
}