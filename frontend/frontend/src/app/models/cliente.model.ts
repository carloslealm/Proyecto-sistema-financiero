export interface Cliente {
  id: number;
  cedula: string;
  nombre: string;
  apellido: string;
  nombreCompleto: string;
  email?: string;
  telefono: string;
  direccion?: string;
  ciudad?: string;
  fechaNacimiento?: string;
  ingresoMensual?: number;
  activo: boolean;
  totalPrestamos: number;
  createdAt: string;
}
 
export interface ClienteRequest {
  cedula: string;
  nombre: string;
  apellido: string;
  email?: string;
  telefono: string;
  direccion?: string;
  ciudad?: string;
  fechaNacimiento?: string;
  ingresoMensual?: number;
}
 
 