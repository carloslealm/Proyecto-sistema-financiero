export interface LoginRequest {
  email: string;
  password: string;
}
 
export interface RegistroRequest {
  nombre: string;
  apellido: string;
  email: string;
  password: string;
  confirmarPassword: string;
  telefono?: string;
}
 
export interface AuthResponse {
  token: string;
  tipo: string;
  expiracionMs: number;
  usuarioId: number;
  nombre: string;
  apellido: string;
  email: string;
  roles: string[];
}
 
// ApiResponse y PageResponse van aquí — son genéricos y
// los usan todos los servicios
export interface ApiResponse<T> {
  success: boolean;
  message: string;
  data: T;
  timestamp: string;
}
 
export interface PageResponse<T> {
  content: T[];
  totalElements: number;
  totalPages: number;
  size: number;
  number: number;
  first: boolean;
  last: boolean;
}